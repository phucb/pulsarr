package ai.platon.pulsar.skeleton.crawl.impl

import ai.platon.pulsar.common.AppContext
import ai.platon.pulsar.common.collect.UrlFeeder
import ai.platon.pulsar.common.config.CapabilityTypes.CRAWL_ENABLE_DEFAULT_DATA_COLLECTORS
import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.common.warnForClose
import ai.platon.pulsar.skeleton.context.PulsarContexts
import ai.platon.pulsar.skeleton.context.support.AbstractPulsarContext
import ai.platon.pulsar.skeleton.crawl.Crawler
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CountDownLatch

open class StreamingCrawlLoop(
    /**
     * The unmodified configuration load from file
     * */
    unmodifiedConfig: ImmutableConfig,
    /**
     * The loop name
     * */
    name: String = "StreamingCrawlLoop"

) : AbstractCrawlLoop(name, unmodifiedConfig) {
    private val logger = LoggerFactory.getLogger(StreamingCrawlLoop::class.java)

    private val scope = CoroutineScope(Dispatchers.Default) + CoroutineName("sc")
    private var crawlJob: Job? = null
    private val started = CountDownLatch(1)

    private lateinit var _crawler: StreamingCrawler
    override val crawler: Crawler get() = _crawler

    private val urlFeeders = ConcurrentSkipListMap<String, UrlFeeder>()

    /**
     * A UrlFeeder is a wrapper to globalCache.urlPool
     * */
    override val urlFeeder: UrlFeeder get() = getOrCreateUrlFeeder()

    private val context get() = PulsarContexts.create()

    init {
        // logger.info("Crawl loop is created | #{} | {}@{}", id, this::class.simpleName, hashCode())
    }
    
    override val abstract: String get() {
        return if (!isRunning) {
            "[stopped] crawler: ${crawler.name}#${crawler.id}, urlPool: ${urlFeeder.urlPool} \n${urlFeeder.abstract}"
        } else {
            urlFeeder.abstract
        }
    }
    
    override val report: String get() {
        return if (!isRunning) {
            return "[stopped] crawler: ${crawler.name}#${crawler.id}, urlPool: ${urlFeeder.urlPool} \n${urlFeeder.report}"
        } else {
            urlFeeder.report
        }
    }
    
    @Synchronized
    override fun start() {
        if (isRunning) {
            // issue a warning for debug
            logger.warn("Crawl loop {} is already running", display)
        }

        if (running.compareAndSet(false, true)) {
            start0()
            logger.info("Crawl loop is started with {} link collectors | #{} | {}", urlFeeder.collectors.size, id, this)
        }
    }

    @Synchronized
    override fun stop() {
        if (running.compareAndSet(true, false)) {
            _crawler.close()

            // url feeder should be shared by all crawlers, so we should not clear it
            // _urlFeeder.clear()

            kotlin.runCatching { runBlocking { crawlJob?.cancelAndJoin() } }
                .onFailure {
                    if (AppContext.isActive) {
                        warnForClose(it, it, "Crawl loop #${id} is stopped with exception")
                    } else {
                        // it's expected that there are some uncaught exceptions if the system is shutting down,
                        // ignore them.
                        if (logger.isDebugEnabled) {
                            logger.debug("Crawl loop #${id} is stopped with exception", it)
                        }
                    }
                }

            crawlJob = null
            logger.info("Crawl loop is stopped | #{} | {}", id, this)
        }
    }

    /**
     * Wait until the loop is started and all tasks are done.
     * */
    @Throws(InterruptedException::class)
    override fun await() {
        started.await()
        crawler.await()
    }

    private fun start0() {
        val cx = context
        require(cx is AbstractPulsarContext) { "Expect context is AbstractPulsarContext, actual ${cx::class}#${cx.id}" }
        val applicationContext = cx.applicationContext
        require(applicationContext.isActive) { "Expect context is active | ${applicationContext.id}" }
        require(cx.isActive) { "Expect context is active | ${cx.id}" }

        val session = cx.getOrCreateSession()
        require(session.isActive) { "Expect session is active, actual ${session::class}#${session.id}" }

        // clear the global illegal states, so the newly created crawler can work properly
        StreamingCrawler.clearIllegalState()

        val urls = urlFeeder.asSequence()
        _crawler = StreamingCrawler(urls, session, autoClose = false)

        crawlJob = scope.launch {
            supervisorScope {
                started.countDown()
                _crawler.run(this)
            }
        }
    }

    private fun getOrCreateUrlFeeder(): UrlFeeder {
        val feeder = urlFeeders.values.firstOrNull() ?: createUrlFeeder()
        urlFeeders[feeder.id] = feeder

        // check if the feeder is upgraded
        if (feeder.urlPool.id == context.globalCache.urlPool.id) {
            return feeder
        }

        if (feeder.isNotEmpty()) {
            logger.warn("The url feeder is abundant, but not empty | #{} | size: {}", feeder.id, feeder.size)
            logger.warn(feeder.report)
        }

        // the feeder is upgraded, so we need to create a new one
        val upgradedFeeder = createUrlFeeder()
        urlFeeders.remove(feeder.id)
        urlFeeders[upgradedFeeder.id] = upgradedFeeder

        logger.warn("The url feeder is upgraded, use the new one instead | #{} <- #{} | {}", upgradedFeeder.id, feeder.id, this)

        return upgradedFeeder
    }

    /**
     * Create a UrlFeeder with default data collectors.
     * A UrlFeeder is a wrapper to globalCache.urlPool, to clear all the urls that waiting for fetching,
     * use globalCache.urlPool.clear()
     * */
    private fun createUrlFeeder(): UrlFeeder {
        val enableDefaults = config.getBoolean(CRAWL_ENABLE_DEFAULT_DATA_COLLECTORS, true)
        return UrlFeeder(context.globalCache.urlPool, enableDefaults = enableDefaults)
    }
}

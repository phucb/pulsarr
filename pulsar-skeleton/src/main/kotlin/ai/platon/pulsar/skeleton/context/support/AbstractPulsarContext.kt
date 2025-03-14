package ai.platon.pulsar.skeleton.context.support

import ai.platon.pulsar.common.*
import ai.platon.pulsar.common.collect.UrlPool
import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.common.urls.*
import ai.platon.pulsar.dom.FeaturedDocument
import ai.platon.pulsar.external.ChatModelFactory
import ai.platon.pulsar.external.ModelResponse
import ai.platon.pulsar.persist.WebDBException
import ai.platon.pulsar.persist.WebDb
import ai.platon.pulsar.persist.WebPage
import ai.platon.pulsar.persist.gora.generated.GWebPage
import ai.platon.pulsar.persist.impl.GoraBackendWebPage
import ai.platon.pulsar.skeleton.common.options.LoadOptions
import ai.platon.pulsar.skeleton.common.urls.CombinedUrlNormalizer
import ai.platon.pulsar.skeleton.common.urls.NormURL
import ai.platon.pulsar.skeleton.context.PulsarContext
import ai.platon.pulsar.skeleton.crawl.CrawlLoops
import ai.platon.pulsar.skeleton.crawl.common.FetchState
import ai.platon.pulsar.skeleton.crawl.common.GlobalCache
import ai.platon.pulsar.skeleton.crawl.common.GlobalCacheFactory
import ai.platon.pulsar.skeleton.crawl.component.*
import ai.platon.pulsar.skeleton.crawl.fetch.driver.WebDriver
import ai.platon.pulsar.skeleton.crawl.filter.ChainedUrlNormalizer
import ai.platon.pulsar.skeleton.session.AbstractPulsarSession
import ai.platon.pulsar.skeleton.session.PulsarEnvironment
import ai.platon.pulsar.skeleton.session.PulsarSession
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanCreationException
import org.springframework.context.support.AbstractApplicationContext
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

abstract class AbstractPulsarContext(
    val applicationContext: AbstractApplicationContext,
    val pulsarEnvironment: PulsarEnvironment = PulsarEnvironment()
) : PulsarContext, AutoCloseable {
    
    companion object {
        val instanceSequencer = AtomicInteger()
    }
    
    private val logger = LoggerFactory.getLogger(AbstractPulsarContext::class.java)
    
    /**
     * Registered closable objects, will be closed by Pulsar object
     * */
    private val closableObjects = ConcurrentSkipListSet<PrioriClosable>()
    
    /** Flag that indicates whether this context has been closed already. */
    private val closed = AtomicBoolean()
    
    /** Synchronization monitor for the "refresh" and "destroy" */
    private val startupShutdownMonitor = Any()
    
    /** Reference to the JVM shutdown hook, if registered */
    private var shutdownHook: Thread? = null
    
    private val beanCreationFailures = AtomicInteger()
    
    private val webDbOrNull: WebDb?
        get() = when {
            isActive -> webDb
            else -> null
        }
    
    private val loadComponentOrNull: LoadComponent?
        get() = when {
            beanCreationFailures.get() > 0 -> null
            isActive -> {
                try {
                    loadComponent
                } catch (e: BeanCreationException) {
                    if (beanCreationFailures.compareAndSet(0, 1)) {
                        logger.error("Failed to create LoadComponent bean", e)
                    } else {
                        beanCreationFailures.incrementAndGet()
                    }
                    null
                }
            }
            
            else -> null
        }
    
    /**
     * Return null if everything is OK, or return NIL if something wrong
     * */
    private val abnormalPage
        get() = when {
            loadComponentOrNull != null -> null // everything is OK
            else -> GoraBackendWebPage.NIL
        }
    
    /**
     * Return null if everything is OK, or return a empty list if something wrong
     * */
    private val abnormalPages: List<WebPage>?
        get() = when {
            loadComponentOrNull != null -> null // everything is OK
            else -> listOf()
        }
    
    /**
     * Flag that indicates whether this context is currently active.
     * */
    override val isActive get() = !closed.get() && applicationContext.isActive
    
    /**
     * The context id
     * */
    override val id = instanceSequencer.incrementAndGet()
    
    init {
        AppContext.start()
    }
    
    /**
     * An immutable config is which loaded from the config file at process startup, and never changes
     * */
    override val unmodifiedConfig: ImmutableConfig get() = getBean()

    /**
     * Url normalizer
     * */
    override val urlNormalizer: ChainedUrlNormalizer get() = getBean()
    
    /**
     * Url normalizer
     * */
    open val urlNormalizerOrNull: ChainedUrlNormalizer? get() = runCatching { urlNormalizer }.getOrNull()
    
    /**
     * The web db
     * */
    open val webDb: WebDb get() = getBean()
    
    open val globalCacheFactory: GlobalCacheFactory get() = getBean()
    
    open val injectComponent: InjectComponent get() = getBean()
    
    open val fetchComponent: BatchFetchComponent get() = getBean()
    
    open val parseComponent: ParseComponent get() = getBean()
    
    open val updateComponent: UpdateComponent get() = getBean()
    
    open val loadComponent: LoadComponent get() = getBean()
    
    override val globalCache: GlobalCache get() = globalCacheFactory.globalCache
    
    override val crawlPool: UrlPool get() = globalCache.urlPool
    
    override val crawlLoops: CrawlLoops get() = getBean()
    
    /**
     * The start time
     * */
    val startTime = System.currentTimeMillis()
    
    /**
     * All open sessions
     * */
    val sessions = ConcurrentSkipListMap<Int, AbstractPulsarSession>()
    
    private val crawlPoolOrNull: UrlPool? get() = runCatching { crawlPool }.getOrNull()
    
    /**
     * Get a bean with the specified class, throws [BeansException] if the bean doesn't exist
     * */
    @Throws(BeansException::class, IllegalStateException::class)
    override fun <T : Any> getBean(requiredType: KClass<T>): T {
        return applicationContext.getBean(requiredType.java)
    }
    
    /**
     * Get a bean with the specified class, returns null if the bean doesn't exist
     * */
    override fun <T : Any> getBeanOrNull(requiredType: KClass<T>): T? {
        if (!isActive) {
            return null
        }
        return applicationContext.runCatching { getBean(requiredType.java) }.getOrNull()
    }
    
    /**
     * Get a bean with the specified class, throws [BeansException] if the bean doesn't exist
     * */
    @Throws(BeansException::class)
    inline fun <reified T : Any> getBean(): T = getBean(T::class)
    
    /**
     * Get a bean with the specified class, returns null if the bean doesn't exist
     * */
    inline fun <reified T : Any> getBeanOrNull(): T? = getBeanOrNull(T::class)
    
    /**
     * Create a session
     * */
    @Throws(Exception::class)
    abstract override fun createSession(): AbstractPulsarSession

    @Throws(Exception::class)
    override fun getOrCreateSession(): AbstractPulsarSession = sessions.values.firstOrNull() ?: createSession()

    /**
     * Close the given session
     * */
    override fun closeSession(session: PulsarSession) {
        session.close()
        logger.info("Removing PulsarSession #{}", session.id)
        sessions.remove(session.id)
    }
    
    /**
     * Register close objects, the objects will be closed when the context closes
     * */
    override fun registerClosable(closable: AutoCloseable, priority: Int) {
        if (!isActive) {
            return
        }
        closableObjects.add(PrioriClosable(priority, closable))
    }
    
    override fun normalize(url: String, options: LoadOptions, toItemOption: Boolean): NormURL {
        val url0 = url.takeIf { it.contains("://") } ?: String(Base64.getUrlDecoder().decode(url))
        val link = Hyperlink(url0, "", href = url0)
        return normalize(link, options, toItemOption)
    }
    
    override fun normalizeOrNull(url: String?, options: LoadOptions, toItemOption: Boolean): NormURL? {
        if (url == null) return null
        return kotlin.runCatching { normalize(url, options, toItemOption) }.getOrNull()
    }
    
    override fun normalize(urls: Iterable<String>, options: LoadOptions, toItemOption: Boolean): List<NormURL> {
        return urls.mapNotNull { normalizeOrNull(it, options, toItemOption) }
    }
    
    override fun normalize(url: UrlAware, options: LoadOptions, toItemOption: Boolean): NormURL {
        val normURL = CombinedUrlNormalizer(urlNormalizerOrNull).normalize(url, options, toItemOption)
        if (normURL.isNil) {
            logger.info("URL is normalized to NIL | {}", url)
        }
        return normURL
    }
    
    override fun normalizeOrNull(url: UrlAware?, options: LoadOptions, toItemOption: Boolean): NormURL? {
        if (url == null) {
            return null
        }

        return kotlin.runCatching { normalize(url, options, toItemOption).takeIf { it.isNotNil } }.getOrNull()
    }
    
    override fun normalize(urls: Collection<UrlAware>, options: LoadOptions, toItemOption: Boolean): List<NormURL> {
        return urls.mapNotNull { normalizeOrNull(it, options, toItemOption) }
    }
    /**
     * Get a webpage from the storage.
     * */
    @Throws(WebDBException::class)
    override fun get(url: String): WebPage {
        return webDbOrNull?.get(url, false) ?: GoraBackendWebPage.NIL
    }
    
    @Throws(WebDBException::class)
    override fun get(url: String, vararg fields: String): WebPage {
        return webDbOrNull?.get(url, false, arrayOf(*fields)) ?: GoraBackendWebPage.NIL
    }
    
    /**
     * Get a webpage from the storage.
     * */
    @Throws(WebDBException::class)
    override fun getOrNull(url: String): WebPage? {
        return webDbOrNull?.getOrNull(url, false)
    }
    
    /**
     * Get a webpage from the storage.
     * */
    @Throws(WebDBException::class)
    override fun getOrNull(url: String, vararg fields: String): WebPage? {
        return webDbOrNull?.getOrNull(url, false, arrayOf(*fields))
    }
    
    @Throws(WebDBException::class)
    override fun getContent(url: String): ByteBuffer? = webDbOrNull?.getContent(url)
    
    @Throws(WebDBException::class)
    override fun getContentAsString(url: String): String? = webDbOrNull?.getContentAsString(url)

    /**
     * Check if a page exists in the storage.
     * */
    @Throws(WebDBException::class)
    override fun exists(url: String) = webDbOrNull?.exists(url) == true
    
    /**
     * Check the fetch state of a page.
     * */
    override fun fetchState(page: WebPage, options: LoadOptions) =
        loadComponentOrNull?.fetchState(page, options) ?: CheckState(FetchState.DO_NOT_FETCH, "closed")
    
    /**
     * Scan pages in the storage.
     * */
    @Throws(WebDBException::class)
    override fun scan(urlPrefix: String): Iterator<WebPage> {
        return webDbOrNull?.scan(urlPrefix) ?: listOf<WebPage>().iterator()
    }
    
    /**
     * Scan pages in the storage.
     * */
    @Throws(WebDBException::class)
    override fun scan(urlPrefix: String, fields: Iterable<GWebPage.Field>): Iterator<WebPage> {
        return webDbOrNull?.scan(urlPrefix, fields) ?: listOf<WebPage>().iterator()
    }
    
    /**
     * Scan pages in the storage.
     * */
    @Throws(WebDBException::class)
    override fun scan(urlPrefix: String, fields: Array<String>): Iterator<WebPage> {
        return webDbOrNull?.scan(urlPrefix, fields) ?: listOf<WebPage>().iterator()
    }

    /**
     * Open a web page with a web driver.
     * */
    override suspend fun open(url: String, driver: WebDriver, options: LoadOptions): WebPage {
        require(options.refresh)
        val normURL = normalize(url, options)
        require(normURL.options.refresh)
        return abnormalPage ?: loadComponent.open(normURL, driver)
    }

    /**
     * Load a page with specified options, see [LoadOptions] for all options.
     *
     * @param url     The url which can be followed by arguments.
     * @param options The load options.
     * @return The WebPage. If there is no web page at local storage nor remote location, [GoraBackendWebPage.NIL] is returned.
     */
    @Throws(WebDBException::class)
    override fun load(url: String, options: LoadOptions): WebPage {
        val normURL = normalize(url, options)
        return abnormalPage ?: loadComponent.load(normURL)
    }
    
    /**
     * Load a url with specified options, see [LoadOptions] for all options.
     *
     * @param url     The url which can be followed by arguments.
     * @param options The load options.
     * @return The WebPage. If there is no web page at local storage nor remote location, [GoraBackendWebPage.NIL] is returned.
     */
    @Throws(WebDBException::class)
    override fun load(url: URL, options: LoadOptions): WebPage {
        return abnormalPage ?: loadComponent.load(url, options)
    }
    
    /**
     * Load a url, options can be specified following the url, see [LoadOptions] for all options.
     *
     * @param url The url which can be followed by arguments.
     * @return The WebPage. If there is no web page at local storage nor remote location, [GoraBackendWebPage.NIL] is returned.
     */
    @Throws(WebDBException::class)
    override fun load(url: NormURL): WebPage {
        return abnormalPage ?: loadComponent.load(url)
    }
    
    @Throws(WebDBException::class)
    override suspend fun loadDeferred(url: NormURL): WebPage {
        return abnormalPage ?: loadComponent.loadDeferred(url)
    }
    
    /**
     * Load a batch of urls with the specified options.
     *
     * If the option indicates prefer parallel, urls are fetched in a parallel manner whenever applicable.
     * If the batch is too large, only a random part of the urls is fetched immediately, all the rest urls are put into
     * a pending fetch list and will be fetched in background later.
     *
     * If a page exists neither in local storage nor at the given remote location, [GoraBackendWebPage.NIL] is returned
     *
     * @param urls    The urls to load
     * @param options The load options
     * @return Pages for all urls.
     */
    @Throws(WebDBException::class)
    override fun loadAll(urls: Iterable<String>, options: LoadOptions): List<WebPage> {
        startLoopIfNecessary()
        return abnormalPages ?: loadComponent.loadAll(normalize(urls, options))
    }
    
    @Throws(WebDBException::class)
    override fun loadAll(urls: Iterable<NormURL>): List<WebPage> {
        startLoopIfNecessary()
        return abnormalPages ?: loadComponent.loadAll(urls)
    }
    
    @Throws(WebDBException::class)
    override fun loadAsync(url: NormURL): CompletableFuture<WebPage> {
        startLoopIfNecessary()
        return loadComponentOrNull?.loadAsync(url) ?: CompletableFuture.completedFuture(GoraBackendWebPage.NIL)
    }
    
    @Throws(WebDBException::class)
    override fun loadAllAsync(urls: Iterable<NormURL>): List<CompletableFuture<WebPage>> {
        startLoopIfNecessary()
        return loadComponentOrNull?.loadAllAsync(urls) ?: listOf()
    }

    override fun submit(url: UrlAware): AbstractPulsarContext {
        startLoopIfNecessary()
        if (url.isStandard || url is DegenerateUrl) {
            crawlPoolOrNull?.add(url)
        }
        return this
    }
    
    override fun submitAll(urls: Iterable<UrlAware>): AbstractPulsarContext {
        startLoopIfNecessary()
        crawlPoolOrNull?.addAll(urls.filter { it.isStandard || it is DegenerateUrl })
        return this
    }

    /**
     * Parse the WebPage content using parseComponent.
     */
    override fun parse(page: WebPage): FeaturedDocument? {
        val parser = loadComponentOrNull?.parseComponent
        return parser?.parse(page, noLinkFilter = true)?.document
    }

    override fun chat(prompt: String): ModelResponse {
        return ChatModelFactory.getOrCreateOrNull(unmodifiedConfig)?.call(prompt) ?: ModelResponse.LLM_NOT_AVAILABLE
    }

    override fun chat(userMessage: String, systemMessage: String): ModelResponse {
        return ChatModelFactory.getOrCreateOrNull(unmodifiedConfig)?.call(userMessage, systemMessage) ?: ModelResponse.LLM_NOT_AVAILABLE
    }

    @Throws(WebDBException::class)
    override fun persist(page: WebPage) {
        webDbOrNull?.put(page, false)
    }
    
    /**
     * Delete the page from the storage.
     * */
    @Throws(WebDBException::class)
    override fun delete(url: String) {
        webDbOrNull?.delete(url)
    }
    
    /**
     * Delete the page from the storage.
     * */
    @Throws(WebDBException::class)
    override fun delete(page: WebPage) {
        webDbOrNull?.delete(page.url)
    }
    
    /**
     * Flush the storage.
     * */
    @Throws(WebDBException::class)
    override fun flush() {
        webDbOrNull?.flush()
    }
    
    /**
     * Wait until there is no tasks in the main loop.
     * */
    @Throws(InterruptedException::class)
    override fun await() {
        if (isActive) {
            crawlLoops.await()
        }
    }
    
    /**
     * Register a shutdown hook with the JVM runtime, closing this context on JVM shutdown unless it has already been
     * closed at that time.
     *
     * Delegates to `doClose()` for the actual closing procedure.
     * @see Runtime.addShutdownHook
     *
     * @see close
     * @see doClose
     */
    @Throws(IllegalStateException::class)
    override fun registerShutdownHook() {
        if (this.shutdownHook == null) { // No shutdown hook registered yet.
            this.shutdownHook = Thread { synchronized(startupShutdownMonitor) { doClose() } }
            Runtime.getRuntime().addShutdownHook(this.shutdownHook)
        }
    }
    
    /**
     * Close this pulsar context.
     *
     * Delegates to `doClose()` for the actual closing procedure.
     * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
     * @see doClose
     * @see registerShutdownHook
     */
    override fun close() {
        synchronized(startupShutdownMonitor) {
            doClose()
            // If we registered a JVM shutdown hook, we don't need it anymore now:
            // We've already explicitly closed the context.
            if (shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook)
                } catch (ex: IllegalStateException) {
                    // ignore - VM is already shutting down
                }
            }
        }
    }
    
    protected open fun doClose() {
        AppContext.terminate()
        
        if (closed.compareAndSet(false, true)) {
            try {
                doClose0()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                System.err.println("Interrupted while closing context | $this")
                warnForClose(this, e)
            } catch (e: Exception) {
                System.err.println("Exception while closing context | $this")
                e.printStackTrace(System.err)
                logger.warn("Exception while closing context | $this", e)
            } catch (t: Throwable) {
                System.err.println("[Unexpected] Failed to close context | $this")
                t.printStackTrace(System.err)
                logger.error("[Unexpected] Failed to close context | $this", t)
            }
        }
        
        AppContext.endTermination()
    }
    
    protected open fun doClose0() {
        logger.info("Closing context #{} with {} sessions | {}", id, sessions.size, this::class.java.simpleName)
        
        val sessions1 = sessions.values.toList()
        sessions.clear()
        val closableObjects1 = closableObjects.toList()
        closableObjects.clear()
        
        sessions1.forEach { session ->
            runCatching { session.close() }.onFailure { warnForClose(this, it) }
        }
        
        closableObjects1.sortedByDescending { it.priority }.forEach { closable ->
            runCatching { closable.closeable.close() }.onFailure { warnForClose(this, it) }
        }
    }
    
    private fun startLoopIfNecessary() {
        if (isActive) {
            crawlLoops.start()
        }
    }
}

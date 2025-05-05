package ai.platon.pulsar.common.proxy

import ai.platon.pulsar.common.NetUtil
import ai.platon.pulsar.common.ResourceLoader
import ai.platon.pulsar.common.Strings
import ai.platon.pulsar.common.readable
import ai.platon.pulsar.common.urls.UrlUtils
import com.google.common.collect.ConcurrentHashMultiset
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.hc.core5.net.URIBuilder
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

open class ProxyEntry(
    /**
     * The host of the proxy server
     * */
    var host: String,
    /**
     * The port of the proxy server
     * */
    var port: Int = 0,
    /**
     * The username
     * */
    var username: String? = null,
    /**
     * The password
     * */
    var password: String? = null,
    /**
     * The proxy type
     * */
    var type: Proxy.Type = Proxy.Type.HTTP
): Comparable<ProxyEntry> {
    enum class Status { FREE, WORKING, RETIRED, EXPIRED, GONE }
    /**
     * The proxy entry id, it's unique in the process scope
     * */
    var id: Int = instanceSequence.incrementAndGet()
    /**
     * The out ip which will be seen by the target site
     * */
    var outIp: String = ""
    
    /**
     * The agent ip which will be seen by the target site.
     * */
    val agentIp: String get() = outIp.takeIf { Strings.isIpLike(it) } ?: host
    
    /**
     * The time to live of the proxy entry declared by the proxy vendor
     * */
    var declaredTTL: Instant? = null
    /**
     * Specify whether we can rotate the out ip via a link.
     * */
    var rotatable: Boolean = false
    /**
     * The link to tell the proxy vendor to rotate the out ip.
     * */
    var rotateURL: String? = null
    /**
     * The last target url
     * */
    var lastTarget: String? = null
    /**
     * The test urls
     * */
    var testUrls: List<URL> = TEST_URLS.toList()
    /**
     * The default test url
     * */
    var defaultTestUrl: URL = DEFAULT_TEST_URL
    /**
     * Check if the proxy is used for test
     * */
    var isTestIp: Boolean = false
    
    val protocol get() = when (type) {
        Proxy.Type.HTTP -> "http"
        Proxy.Type.SOCKS -> "socks"
        else -> ""
    }
    val hostPort get() = "$host:$port"
    
    val segment get() = host.substringBeforeLast(".")
    val outSegment get() = outIp.substringBeforeLast(".")
    val startTime = Instant.now()
    val elapsedTime get() = Duration.between(startTime, Instant.now())
    val display get() = formatDisplay()
    val metadata get() = formatMetadata()
    var networkTester: (URL, Proxy) -> Boolean = NetUtil::testHttpNetwork
    val numTests = AtomicInteger()
    val numConnectionLosses = AtomicInteger()
    // accumulated response time
    val accumResponseMillis = AtomicLong()
    // last time the proxy is proven be available
    var availableTime: Instant = Instant.now()
    // number of failed pages
    val numFailedPages = AtomicInteger()
    // number of success pages
    val numSuccessPages = AtomicInteger()
    val servedDomains = ConcurrentHashMultiset.create<String>()
    val status = AtomicReference(Status.FREE)
    val testSpeed get() = accumResponseMillis.get() / numTests.get().coerceAtLeast(1) / 1000.0
    val ttl get() = declaredTTL ?: (availableTime + PROXY_EXPIRED)
    val ttlDuration get() = Duration.between(Instant.now(), ttl).takeIf { !it.isNegative }
    val isExpired get() = willExpireAt(Instant.now())
    val isRetired get() = status.get() == Status.RETIRED
    val isFree get() = status.get() == Status.FREE
    val isWorking get() = status.get() == Status.WORKING
    val isBanned get() = isRetired && !isExpired
    val isFailed get() = numConnectionLosses.get() >= 3 // large value means ignoring failures
    val isGone get() = isRetired || isFailed
    
    val numRunningTasks = AtomicInteger()
    /**
     * Last time to use this proxy
     * */
    var lastActiveTime = Instant.now()
    var idleTimeout = Duration.ofMinutes(10)
    val idleTime get() = Duration.between(lastActiveTime, Instant.now())
    val isIdle get() = (numRunningTasks.get() == 0 && idleTime > idleTimeout)
    /**
     * Check if this proxy is ready to work.
     * Note: idle proxy can still be ready. It's very common to visit a web page for more than 10 minutes.
     * */
    val isReady get() = !isGone && !isExpired && !isRetired && !isBanned
    
    /**
     * Get the readable proxy state.
     * */
    val readableState: String get() {
        return listOf("retired" to isRetired, "idle" to isIdle, "ready" to isReady)
            .filter { it.second }.joinToString(" ") { it.first }
    }
    
    val params get() =
        listOf(
            "ttl" to declaredTTL,
            "at" to availableTime,
            "st" to status,
            "pg" to numSuccessPages,
            "fpg" to numFailedPages
        )
    
    enum class BanState {
        OK, SEGMENT, HOST, OTHER;
        
        val isOK get() = this == OK
        val isBanned get() = !isOK
    }
    
//    constructor(uri: URI): this(uri.host, uri.port, uri.userInfo?.substringBefore(":"), uri.userInfo?.substringAfter(":"))
    
    fun toURI() = URI(protocol, "$username:$password", host, port, null, null, null)
    
    fun willExpireAt(instant: Instant): Boolean = ttl < instant
    
    fun willExpireAfter(duration: Duration): Boolean = ttl < Instant.now() + duration
    
    fun setFree() { status.set(Status.FREE) }
    
    fun startWork() { status.set(Status.WORKING) }
    
    fun retire() { status.set(Status.RETIRED) }
    
    fun refresh() {
        availableTime = Instant.now()
        lastActiveTime = availableTime
    }
    
    fun canConnect(): Boolean {
        return NetUtil.testTcpNetwork(host, port)
    }
    
    fun test(): Boolean {
        val target = if (lastTarget != null) {
            URL(lastTarget)
        } else testUrls.random()
        
        var available = test(target)
        if (!available && !isGone && target != defaultTestUrl) {
            available = test(defaultTestUrl)
            if (available) {
                logger.warn("Target unreachable via {} | {} | {}", display, metadata, target.host)
            } else if (!isGone) {
                logger.warn("Proxy connection lost {} | {} | {}", display, metadata, target.host)
            }
        }
        
        return available
    }
    
    fun test(target: URL, timeout: Duration = Duration.ofSeconds(5)): Boolean {
        // first, check TCP network is reachable, this is fast
        var available = NetUtil.testTcpNetwork(host, port, timeout)
        if (available) {
            // second, the destination website is reachable through this proxy
            val addr = InetSocketAddress(host, port)
            val proxy = Proxy(Proxy.Type.HTTP, addr)
            
            val start = System.currentTimeMillis()
            available = networkTester(target, proxy)
            val end = System.currentTimeMillis()
            
            numTests.incrementAndGet()
            accumResponseMillis.addAndGet(end - start)
        }
        
        if (!available) {
            numConnectionLosses.incrementAndGet()
            if (isGone) {
                logger.warn("Proxy is gone after {} tests | {}", numTests, this)
            } else {
                logger.info("Proxy is not available | $this")
            }
        } else {
            numConnectionLosses.set(0)
            refresh()
        }
        
        return available
    }
    
    /**
     * The string representation, can be parsed using [parse] or [deserialize]
     * */
    fun serialize(): String {
        val ttlStr = if (declaredTTL != null) ", ttl:$declaredTTL" else ""
        return "$host:$port${META_DELIMITER}at:$availableTime$ttlStr, $metadata"
    }

    fun serialize2(): String {
        val uriBuilder = URIBuilder(toURI())
        params.forEach { uriBuilder.addParameter(it.first, it.second.toString()) }
        return uriBuilder.toString()
    }
    
    /**
     * The string representation, can be parsed using [parse] or [deserialize]
     * */
    fun format() = serialize()
    
    override fun hashCode(): Int = 31 * type.hashCode() + hostPort.hashCode()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        
        return other is ProxyEntry
            && other.type == type
            && other.host == host
            && other.port == port
            && other.username == username
            && other.password == password
            && other.outIp == outIp
    }
    
    /**
     * The string representation, can be parsed using [parse]
     * */
    override fun toString(): String = "$display $metadata"
    
    override fun compareTo(other: ProxyEntry): Int {
        var c = outIp.compareTo(other.outIp)
        if (c == 0) {
            c = hostPort.compareTo(other.hostPort)
        }
        return c
    }
    
    private fun formatDisplay(): String {
        val ban = if (isBanned) "[banned] " else ""
        val ttlStr = ttlDuration?.truncatedTo(ChronoUnit.SECONDS)?.readable() ?:"0s"
        return "$ban[$hostPort => $outIp]($numFailedPages/$numSuccessPages/$ttlStr)[$readableState]"
    }
    
    /**
     * Format metadata as key-value pairs, metadata with zero value are ignored.
     * */
    private fun formatMetadata(): String {
        val nPages = numSuccessPages.get() + numFailedPages.get()
        
        var s = listOf(
            "st" to status.get().ordinal,
            "pg" to nPages,
            "fpg" to numFailedPages.get(),
            "tt" to numTests.get(),
            "ftt" to numConnectionLosses.get()
        ).filter { it.second > 0 }.joinToString(", ")
        
        if (testSpeed > 0) {
            s += ", spd:$testSpeed"
        }
        
        return s
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(ProxyEntry::class.java)
        
        private val instanceSequence = AtomicInteger()
        private const val META_DELIMITER = StringUtils.SPACE
        // Check if the proxy server is still available if it's not used for 120 seconds
        private val PROXY_EXPIRED = Duration.ofSeconds(120)
        // if a proxy server can not be connected in an hour, we announce it's dead and remove it from the file
        private val MISSING_PROXY_DEAD_TIME = Duration.ofHours(1)
        private const val DEFAULT_PROXY_SERVER_PORT = 80
        const val PROXY_TEST_WEB_SITES_FILE = "proxy.test.web.sites.txt"
        val DEFAULT_TEST_URL = URL("https://www.baidu.com")
        // Jan 2 18:06 2021, there is a strange bug in mutableSetOf<URL>(), add items to the set hungs up the process
        // environment:
        // Linux vincent-KLVC-WXX9 5.8.0-34-generic #37~20.04.2-Ubuntu SMP Thu Dec 17 14:53:00 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux
//        val TEST_URLS = mutableSetOf<URL>()
        val TEST_URLS = mutableListOf<URL>()
        
        init {
            ResourceLoader.readAllLines(PROXY_TEST_WEB_SITES_FILE).mapNotNullTo(TEST_URLS) { UrlUtils.getURLOrNull(it) }
        }

        fun create(
            host: String, port: Int, username: String, password: String, type: String?, declaredTTL: String?
        ): ProxyEntry {
            val proxyEntry = ProxyEntry(host, port, username, password)

            if (type != null) {
                proxyEntry.type = when (type.lowercase()) {
                    "http" -> Proxy.Type.HTTP
                    "socks4" -> Proxy.Type.SOCKS
                    "socks5" -> Proxy.Type.SOCKS
                    "direct" -> Proxy.Type.DIRECT
                    else -> Proxy.Type.HTTP
                }
            }

            if (declaredTTL != null) {
                proxyEntry.declaredTTL = runCatching { Instant.parse(declaredTTL) }.getOrNull()
            }

            return proxyEntry
        }

        /**
         * Parse a proxy from a string. A string generated by ProxyEntry.serialize() can be parsed.
         * */
        fun parse(str: String): ProxyEntry? = deserialize(str)
        
        fun fromURI(uri: URI): ProxyEntry? {
            val type = when (uri.scheme) {
                "http", "https" -> Proxy.Type.HTTP
                "socks", "socks4", "socks5" -> Proxy.Type.SOCKS
                else -> return null
            }
            
            val username = uri.userInfo?.substringBefore(":")
            val password = uri.userInfo?.substringAfter(":")
            val proxyEntry = ProxyEntry(uri.host, uri.port, username, password, type)
            
            return proxyEntry
        }
        
        /**
         * Parse a proxy from a string. A string generated by [serialize] can be parsed.
         * */
        fun deserialize(str: String): ProxyEntry? {
            val ipPort = str.trim().substringBefore(META_DELIMITER)
            if (!Strings.isIpPortLike(ipPort)) {
                logger.warn("Malformed ip port - >{}<", str)
                return null
            }
            
            val pos = ipPort.lastIndexOf(':')
            if (pos != -1) {
                val host = ipPort.substring(0, pos)
                val port = NumberUtils.toInt(ipPort.substring(pos + 1), DEFAULT_PROXY_SERVER_PORT)
                
                var availableTime: Instant? = null
                var ttl: Instant? = null
                val parts = str.substringAfter(META_DELIMITER).split(", ")
                parts.forEach { item ->
                    try {
                        when {
                            item.startsWith("at:") -> availableTime = Instant.parse(item.substring("at:".length).trimEnd())
                            item.startsWith("ttl:") -> ttl = Instant.parse(item.substring("ttl:".length).trimEnd())
                        }
                    } catch (e: Throwable) {
                        logger.warn("Ignore malformed proxy metadata <{}>", item)
                    }
                }
                
                val proxyEntry = ProxyEntry(host, port)
                ttl?.let { proxyEntry.declaredTTL = it }
                availableTime?.let { proxyEntry.availableTime = it }
                
                return proxyEntry
            }
            
            return null
        }
        
        fun parse2(str: String): ProxyEntry? = deserialize2(str)
        
        /**
         * Parse a proxy from a string. A string generated by [serialize] can be parsed.
         * TODO: keep consistent with .parse()
         * */
        fun deserialize2(str: String): ProxyEntry? {
            val uri = runCatching { URI.create(str) }.getOrNull() ?: return null
            
            var username: String? = null
            var password: String? = null
            if (uri.userInfo != null) {
                username = uri.userInfo.substringBefore(":")
                password = uri.userInfo.substringAfter(":")
            }
            val type = when (uri.scheme) {
                "http", "https" -> Proxy.Type.HTTP
                "socks", "socks4", "socks5" -> Proxy.Type.SOCKS
                else -> return null
            }
            
            val proxyEntry = ProxyEntry(uri.host, uri.port, username, password, type)
            val params = uri.query.split("&").map { it.split("=") }
                .filter { it.size == 2 }.associate { it[0] to it[1] }
            
            params["ttl"]?.let { proxyEntry.declaredTTL = Instant.parse(it) }
            params["at"]?.let { proxyEntry.availableTime = Instant.parse(it) }
            params["st"]?.let { proxyEntry.status.set(Status.valueOf(it)) }
            params["pg"]?.let { proxyEntry.numSuccessPages.set(Integer.parseInt(it)) }
            params["fpg"]?.let { proxyEntry.numFailedPages.set(Integer.parseInt(it)) }
            
            return proxyEntry
        }
    }
}

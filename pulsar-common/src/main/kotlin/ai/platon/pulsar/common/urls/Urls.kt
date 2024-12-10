package ai.platon.pulsar.common.urls

import java.net.MalformedURLException
import java.net.URL
import java.time.Instant

/**
 * A degenerate url represent a task that executes in the main loop.
 * A degenerate url can be submitted to the url pool normally, the main loop will take it from the url pool,
 * and execute it as a task, but it will never be loaded as a webpage.
 * */
interface DegenerateUrl

/**
 * A callable degenerate url is a degenerate url that can be called.
 * */
interface CallableDegenerateUrl: DegenerateUrl {
    /**
     * Call the degenerate url
     * */
    operator fun invoke()
}

/**
 * `UrlAware` encapsulates a URL along with additional specifications defining its loading behavior.
 *
 * A URL represents a Uniform Resource Locator, a pointer to a "resource" on the World Wide Web.
 * A resource can be something as simple as a file or a directory, or it can be a reference to
 * a more complicated object, such as a query to a database or to a search engine.
 *
 * In java, a [URL] object represents a URL.
 * In PulsarRPA, a [UrlAware] object represents a URL with extra information telling the system
 * how to fetch it.
 * */
interface UrlAware {
    /**
     * The url specification, can be followed by load arguments.
     * */
    var url: String

    /**
     * The explicitly specified load arguments
     * */
    var args: String?

    /**
     * The hypertext reference, it defines the address of the document, which this time is linked from.
     * The href is usually extracted from the webpage and serves as the browser's primary choice for navigation.
     * */
    var href: String?

    /**
     * The referrer url, it is the url of the webpage that contains the hyperlink.
     * */
    var referrer: String?

    /**
     * The priority of the url, the higher the priority, the earlier the url will be loaded.
     * Priority is a numerical value, where smaller numbers indicate higher priority.
     * */
    var priority: Int

    /**
     * The configured url, always be "$url $args"
     * */
    val configuredUrl: String

    /**
     * If true, the url is standard and can be converted to a [java.net.URL]
     * */
    val isStandard: Boolean

    /**
     * Converted to a [java.net.URL]
     * */
    @get:Throws(MalformedURLException::class)
    val toURL: URL

    /**
     * Converted to a [java.net.URL], if the url is invalid, return null
     * */
    val toURLOrNull: URL?

    /**
     * An url is Nil if it equals to AppConstants.NIL_PAGE_URL
     * */
    val isNil: Boolean
    
    /**
     * If true, the url is persistable, it can be saved to the database.
     * Not all urls are persistable, for example, a ListenableHyperlink with events is not persistable.
     * */
    val isPersistable: Boolean
    
    /**
     * The text of the url, it can be the text of the hyperlink.
     * */
    var text: String
    
    /**
     * The order of the url.
     * */
    var order: Int
    
    /**
     * The url label, it should be a shortcut for `-label` option in load options
     * */
    val label: String

    /**
     * The deadline, it should be a shortcut for `-deadline` option in load options
     * */
    val deadline: Instant

    /**
     * Required website language, reserved for future use
     * */
    val lang: String

    /**
     * Required website country, reserved for future use
     * */
    val country: String

    /**
     * Required website district, reserved for future use
     * */
    val district: String

    /**
     * The maximum retry times
     * */
    val nMaxRetry: Int
    
    /**
     * The depth of the url from the root url.
     * */
    val depth: Int
}

/**
 * The ComparableUrlAware interface. A ComparableUrlAware is an [UrlAware] with comparable.
 * */
interface ComparableUrlAware : UrlAware, Comparable<UrlAware>

/**
 * The StatefulUrl interface. A StatefulUrl is an UrlAware with status.
 * */
interface StatefulUrl : ComparableUrlAware {
    /**
     * The authorization token, it is used to authenticate the request.
     * The auth token like this: `a106WzRlrvS9Ae77d4a20e9a30344ef688562c0a249f7`.
     * */
    var authToken: String?
    /**
     * The remote address
     * */
    var remoteAddr: String?
    /**
     * The status of the url
     * */
    var status: Int
    /**
     * The modified time
     * */
    var modifiedAt: Instant
    /**
     * The created time
     * */
    val createdAt: Instant
}

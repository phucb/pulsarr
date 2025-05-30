
package ai.platon.pulsar.persist

import ai.platon.pulsar.common.HtmlIntegrity
import ai.platon.pulsar.common.browser.BrowserType
import ai.platon.pulsar.common.proxy.ProxyEntry
import ai.platon.pulsar.persist.metadata.MultiMetadata
import ai.platon.pulsar.persist.metadata.OpenPageCategory
import ai.platon.pulsar.persist.model.ActiveDOMStatTrace
import ai.platon.pulsar.persist.model.ActiveDOMUrls
import ai.platon.pulsar.persist.model.ActiveDOMMetadata
import java.lang.ref.WeakReference
import java.util.*

/**
 * The page datum collected from a active page open in the browser, it is used to update a WebPage.
 * */
class PageDatum(
    /**
     * The url is the permanent internal address, and it's also the storage key.
     * The url can differ from the original url passed by the user, because the original url might be normalized,
     * and the url also can differ from the final location of the page, because the page can be redirected in the browser.
     */
    val url: String,
    /**
     * In javascript, the baseURI is a property of Node, it's the absolute base URL of the
     * document containing the node. A baseURI is used to resolve relative URLs.
     *
     * This property is retrieved from javascript `document.baseURI`.
     *
     * The base URL is determined as follows:
     * 1. By default, the base URL is the location of the document
     *    (as determined by window.location).
     * 2. If the document has an `<base>` element, its href attribute is used.
     * */
    var baseURI: String = url,
    /**
     * Returns the document location as a string.
     *
     * [location] is the last working address, retrieved by javascript,
     * it might redirect from the original url, or it might have additional query parameters.
     * [location] can differ from [url].
     *
     * In javascript, the documentURI property can be used on any document types. The document.URL
     * property can only be used on HTML documents.
     *
     * @see <a href='https://www.w3schools.com/jsref/prop_document_documenturi.asp'>
     *     HTML DOM Document documentURI</a>
     * */
    var location: String = url,
    /**
     * The protocol status without translation
     * */
    var protocolStatus: ProtocolStatus = ProtocolStatus.STATUS_CANCELED,
    /**
     * The binary content retrieved.
     */
    var content: ByteArray? = null,
    /**
     * The media type of the retrieved content.
     */
    var contentType: String? = null,
    /**
     * Protocol-specific headers.
     */
    val headers: MultiMetadata = MultiMetadata(),
    /**
     * Other protocol-specific data.
     */
    val metadata: MultiMetadata = MultiMetadata(),
) {
    /**
     * The page category, it can be specified by the user or detected automatically
     * */
    var pageCategory: OpenPageCategory? = null
    /**
     * The proxy entry used to fetch the page
     * */
    var proxyEntry: ProxyEntry? = null
    /**
     * The browser type used to fetch the page
     * */
    var lastBrowser: BrowserType? = null
    /**
     * The html content integrity
     * */
    var htmlIntegrity: HtmlIntegrity? = null
    /**
     * Track the DOM states at different time points in a real browser, which are calculated by javascript.
     * */
    var activeDOMStatTrace: ActiveDOMStatTrace? = null
    /**
     * The page URLs in a real browser calculated by javascript.
     * */
    var activeDOMUrls: ActiveDOMUrls? = null
    /**
     * The metadata of the active DOM, which is calculated by javascript.
     * */
    var activeDomMetadata: ActiveDOMMetadata? = null
    /**
     * The length of the original page content in bytes, the content has no inserted pulsar metadata.
     */
    var originalContentLength: Int = -1
    /**
     * The length of the final page content in bytes, the content might has inserted pulsar metadata.
     */
    val contentLength get() = (content?.size ?: 0).toLong()
    /**
     * The page object, it's a weak reference to avoid circular reference.
     * */
    var page = WeakReference<WebPage>(null)

    constructor(page: WebPage): this(page.url) {
        require(page is AbstractWebPage)
        this.page = WeakReference(page)
        page.pageDatum = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return other is PageDatum
                && url == other.url
                && location == other.location
                && contentType == other.contentType
                && Arrays.equals(content, other.content)
    }

    override fun hashCode() = url.hashCode()

    override fun toString() = url
}

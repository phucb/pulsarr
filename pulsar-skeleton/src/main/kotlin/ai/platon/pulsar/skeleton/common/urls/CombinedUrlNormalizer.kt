package ai.platon.pulsar.skeleton.common.urls

import ai.platon.pulsar.common.urls.UrlAware
import ai.platon.pulsar.common.urls.UrlUtils
import ai.platon.pulsar.skeleton.common.options.LoadOptions
import ai.platon.pulsar.skeleton.crawl.GlobalEventHandlers
import ai.platon.pulsar.skeleton.crawl.common.url.ListenableUrl
import ai.platon.pulsar.skeleton.crawl.filter.ChainedUrlNormalizer

class CombinedUrlNormalizer(private val urlNormalizers: ChainedUrlNormalizer? = null) {
    /**
     * Normalize an url.
     *
     * If both url arguments and [LoadOptions] are present, the url arguments overrides the [LoadOptions].
     *
     * @param url the url to be normalized
     * @param options the options to be used
     * @param toItemOption whether to create item options
     * @return the normalized url, or a NIL NormURL if the url is invalid
     * */
    fun normalize(url: UrlAware, options: LoadOptions, toItemOption: Boolean): NormURL {
        val (spec, args1) = UrlUtils.splitUrlArgs(url.url)
        val args2 = url.args ?: ""
        val args3 = options.toString()
        // args1 has the #1 priority, and then args2, and at last args3.
        // the later args overwrite the earlier ones.
        val finalArgs = "$args3 $args2 $args1".trim()
        val finalOptions = createLoadOptions(url, LoadOptions.parse(finalArgs, options), toItemOption)

        val rawEvent = finalOptions.rawEvent
        var normURL: String? = if (rawEvent?.loadEventHandlers?.onNormalize?.isNotEmpty == true) {
            // 1. normalizer in event listener has the #1 priority.
            val spec1 = GlobalEventHandlers.pageEventHandlers?.loadEventHandlers?.onNormalize?.invoke(spec) ?: spec
            // The more specific handlers has the opportunity to override the result of more general handlers.
            rawEvent.loadEventHandlers.onNormalize(spec1) ?: return NormURL.createNil(url)
        } else {
            // 2. global normalizers has the #2 priority
            val normalizers = urlNormalizers
            if (!options.noNorm && normalizers != null) {
                normalizers.normalize(spec) ?: return NormURL.createNil(url)
            } else spec
        }

        if (!finalOptions.isDefault("priority")) {
            url.priority = finalOptions.priority
        }
        val href = url.href?.let { UrlUtils.splitUrlArgs(it).first }?.takeIf { UrlUtils.isStandard(it) }

        // 3. UrlUtils.normalize comes at last to remove fragment, and query string if required
        normURL = UrlUtils.normalizeOrNull(normURL, options.ignoreUrlQuery)

        return if (normURL == null) {
            NormURL.createNil(url)
        } else {
            NormURL(normURL, finalOptions, href, detail = url)
        }
    }

    internal fun createLoadOptions(url: UrlAware, options: LoadOptions, toItemOption: Boolean = false): LoadOptions {
        val options2 = if (toItemOption) options.createItemOptions() else options
        val options3 = createLoadOptions0(url, options2)

        options3.overrideConfiguration()

        return options3
    }

    internal fun createLoadOptions0(url: UrlAware, options: LoadOptions): LoadOptions {
        val clone = options.clone()

        // TODO: disable in product environment for performance issue
        require(options.toString() == clone.toString())

        require(options.rawEvent == clone.rawEvent)
        require(options.rawItemEvent == clone.rawItemEvent)

        clone.conf.name = clone.label
        clone.nMaxRetry = url.nMaxRetry

        if (url is ListenableUrl) {
            clone.eventHandlers.chain(url.eventHandlers)
        }

        return clone
    }
}

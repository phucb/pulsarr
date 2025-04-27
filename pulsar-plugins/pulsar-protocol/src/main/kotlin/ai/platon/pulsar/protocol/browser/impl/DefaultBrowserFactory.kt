package ai.platon.pulsar.protocol.browser.impl

import ai.platon.pulsar.browser.common.BrowserSettings
import ai.platon.pulsar.browser.driver.chrome.common.ChromeOptions
import ai.platon.pulsar.browser.driver.chrome.common.LauncherOptions
import ai.platon.pulsar.common.browser.BrowserType
import ai.platon.pulsar.protocol.browser.driver.playwright.PlaywrightBrowserLauncher
import ai.platon.pulsar.skeleton.crawl.fetch.driver.Browser
import ai.platon.pulsar.skeleton.crawl.fetch.driver.BrowserLauncher
import ai.platon.pulsar.skeleton.crawl.fetch.privacy.BrowserId

class DefaultBrowserFactory : AbstractBrowserFactory() {
    private val launchers = mapOf(
        BrowserType.PULSAR_CHROME to PulsarBrowserLauncher(),
        BrowserType.PLAYWRIGHT_CHROME to PlaywrightBrowserLauncher()
    )

    override fun launch(
        browserId: BrowserId, launcherOptions: LauncherOptions, launchOptions: ChromeOptions
    ): Browser = getLauncher(browserId.browserType).launch(browserId, launcherOptions, launchOptions)

    override fun connect(browserType: BrowserType, port: Int, settings: BrowserSettings): Browser =
        getLauncher(browserType).connect(port, settings)

    private fun getLauncher(browserType: BrowserType): BrowserLauncher {
        return launchers[browserType] ?: throw IllegalArgumentException("Unknown browser type: $browserType")
    }
}

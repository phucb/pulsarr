package ai.platon.pulsar.protocol.browser

import ai.platon.pulsar.common.AppPaths
import ai.platon.pulsar.common.config.CapabilityTypes.MIN_SEQUENTIAL_PRIVACY_AGENT_NUMBER
import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.skeleton.crawl.fetch.driver.Browser
import ai.platon.pulsar.skeleton.crawl.fetch.privacy.BrowserId
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrowserFactoryTest {

    private lateinit var browserFactory: BrowserFactory
    private val browsers = mutableListOf<Browser>()

    @Before
    fun setUp() {
        browserFactory = BrowserFactory()
    }

    @After
    fun tearDown() {
        browsers.forEach { it.close() }
    }

    @Test
    fun testConnect() {

    }

    @Test
    fun testLaunchSystemDefaultBrowser() {
        val browser = browserFactory.launchSystemDefaultBrowser()
        browsers.add(browser)
        assertEquals(BrowserId.SYSTEM_DEFAULT, browser.id)
        assertEquals(BrowserId.SYSTEM_DEFAULT.contextDir, browser.id.contextDir)
    }

    @Test
    fun testLaunchDefaultBrowser() {
        val browser = browserFactory.launchDefaultBrowser()
        browsers.add(browser)
        assertEquals(BrowserId.DEFAULT, browser.id)
        assertEquals(BrowserId.DEFAULT.contextDir, browser.id.contextDir)
    }

    @Test
    fun testLaunchPrototypeBrowser() {
        val browser = browserFactory.launchPrototypeBrowser()
        browsers.add(browser)
        assertEquals(BrowserId.PROTOTYPE, browser.id)
        assertEquals(BrowserId.PROTOTYPE.contextDir, browser.id.contextDir)
    }

    @Test
    fun testLaunchNextSequentialBrowser() {
        val browser1 = browserFactory.launchNextSequentialBrowser()
        browsers.add(browser1)

        val browser2 = browserFactory.launchNextSequentialBrowser()
        browsers.add(browser2)

        assertTrue { browser1.id.contextDir.toString().replace("\\", "/").contains("context/groups/default") }
        assertTrue(
            "Context dir should be start with AppPaths.CONTEXT_GROUP_BASE_DIR\n" +
                    "${browser1.id.contextDir}\n${AppPaths.CONTEXT_GROUP_BASE_DIR}"
        )
        {
            browser1.id.contextDir.startsWith(AppPaths.CONTEXT_GROUP_BASE_DIR)
        }
        assertTrue(
            "Context dir should be start with AppPaths.getContextGroupDir(\"default\")\n" +
                    "${browser1.id.contextDir}\n" +
                    "${AppPaths.getContextGroupDir("default")}"
        )
        {
            browser1.id.contextDir.startsWith(AppPaths.getContextGroupDir("default"))
        }
    }

    @Test
    fun testLaunchNextSequentialBrowserManyTimes() {
        val conf = ImmutableConfig()
        val maxAgents = conf.getInt(MIN_SEQUENTIAL_PRIVACY_AGENT_NUMBER, 10)

        for (i in 1..(maxAgents + 2)) {
            val browser = browserFactory.launchNextSequentialBrowser()
            browsers.add(browser)
            browser.close()
        }

        val contextDirs = browsers.map { it.id.contextDir }.toSet()
        assertTrue("Context dirs should used cyclically\n${contextDirs.joinToString("\n")}") { contextDirs.size <= maxAgents }
    }

    @Test
    fun testLaunchRandomTempBrowser() {
        val browser = browserFactory.launchRandomTempBrowser()
        browsers.add(browser)

        assertTrue { browser.id.contextDir.toString().replace("\\", "/").contains("context/tmp/groups/rand") }
        assertTrue("Context dir should be start with AppPaths.CONTEXT_TMP_DIR\n${browser.id.contextDir}\n${AppPaths.CONTEXT_TMP_DIR}") {
            browser.id.contextDir.startsWith(AppPaths.CONTEXT_TMP_DIR) }
        assertTrue("Context dir should be start with AppPaths.getTmpContextGroupDir(\"rand\")\n" +
                "${browser.id.contextDir}\n" +
                "${AppPaths.getTmpContextGroupDir("rand")}") {
            browser.id.contextDir.startsWith(AppPaths.getTmpContextGroupDir("rand")) }
    }
}

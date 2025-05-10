package ai.platon.pulsar.common.browser

import ai.platon.pulsar.common.AppContext
import ai.platon.pulsar.common.config.CapabilityTypes
import ai.platon.pulsar.common.config.ImmutableConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Browsers {

    val CHROME_BINARY_SEARCH_PATHS = listOf(
        "/usr/bin/google-chrome-stable",
        "/usr/bin/google-chrome",
        "/opt/google/chrome/chrome",
        "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe",
        "C:/Program Files/Google/Chrome/Application/chrome.exe",
        // Windows 7, see https://github.com/platonai/pulsar/issues/9
        AppContext.USER_HOME + "/AppData/Local/Google/Chrome/Application/chrome.exe",
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
        "/Applications/Chromium.app/Contents/MacOS/Chromium",
        "/usr/bin/chromium",
        "/usr/bin/chromium-browser"
    )

    val ADDITIONAL_CHROME_BINARY_SEARCH_PATHS = mutableListOf<String>()

    /**
     * Returns the chrome binary path.
     *
     * @return Chrome binary path.
     */
    fun searchChromeBinary(): Path {
        val path = System.getProperty(CapabilityTypes.CHROME_PATH)
        if (path != null) {
            return Paths.get(path).takeIf { Files.isExecutable(it) }?.toAbsolutePath()
                ?: throw RuntimeException("CHROME_PATH is not executable | $path")
        }

        val searchPaths = ADDITIONAL_CHROME_BINARY_SEARCH_PATHS + CHROME_BINARY_SEARCH_PATHS
        return searchPaths.map { Paths.get(it) }
            .firstOrNull { Files.isExecutable(it) }
            ?.toAbsolutePath()
            ?: throw RuntimeException("Could not find chrome binary in search path. Try setting CHROME_PATH environment value")
    }

    fun searchChromeBinaryOrNull() = kotlin.runCatching { searchChromeBinary() }.getOrNull()

    /**
     * Find BROWSER_CHROME_PATH in all config files
     * */
    private fun searchChromeBinaryPathAllAround(conf: ImmutableConfig) {
        val chromeBinaryPath = conf.get(CapabilityTypes.CHROME_PATH)
        if (chromeBinaryPath != null) {
            val path = Paths.get(chromeBinaryPath).takeIf { Files.isExecutable(it) }?.toAbsolutePath()
            if (path != null) {
                System.setProperty(CapabilityTypes.CHROME_PATH, chromeBinaryPath)
            }
        }
    }
}

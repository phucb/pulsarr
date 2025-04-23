package ai.platon.pulsar.external

import ai.platon.pulsar.common.config.ImmutableConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class LLMConfigTest {

    @Test
    fun testEnvStyleConfig() {
        System.setProperty("DEEPSEEK_API_KEY", "testAPI.key")
        val conf = ImmutableConfig()
        assertTrue { ChatModelFactory.isModelConfigured(conf) }
    }

    @Test
    fun testSpringStyleConfig() {
        System.setProperty("deepseek.api.key", "testAPI.key")
        val conf = ImmutableConfig()
        assertTrue { ChatModelFactory.isModelConfigured(conf) }
    }
}

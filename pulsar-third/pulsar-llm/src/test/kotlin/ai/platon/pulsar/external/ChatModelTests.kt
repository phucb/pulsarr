package ai.platon.pulsar.external

import ai.platon.pulsar.common.ResourceLoader
import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.common.config.KConfiguration
import ai.platon.pulsar.dom.Documents
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@Ignore("Test only when authorized to LLM provider")
class ChatModelTests {

    companion object {
        private val url = "https://www.amazon.com/dp/B0C1H26C46"
        private val args = "-requireSize 200000"
        private val productHtml = ResourceLoader.readString("pages/amazon/B0C1H26C46.original.htm")
        private val productText = ResourceLoader.readString("prompts/product.txt")
        private val clusterAnalysisPrompt = ResourceLoader.readString("prompts/data-expert/fulltext/prompt.p1723107189.6.remarkable.txt")
        private val conf = ImmutableConfig(loadDefaults = true)
        private val isModelConfigured get() = ChatModelFactory.isModelConfigured(conf)
        private val model = ChatModelFactory.getOrCreateOrNull(conf)

        @BeforeAll
        @JvmStatic
        fun checkConfiguration() {
            if (!isModelConfigured) {
                println("=========================== LLM NOT CONFIGURED ==========================================")
                println("> Skip the tests because the API key is not set")
                println("> Please set the API key in the configuration file or environment variable")
                println("> The configuration file can be found in: " + KConfiguration.EXTERNAL_RESOURCE_BASE_DIR)
                println("> All xml files in the directory will be loaded as the configuration file")
            }
        }
    }

    @BeforeTest
    fun checkResource() {
        assertTrue { productHtml.isNotBlank() }
        assertTrue { productText.isNotBlank() }
    }

    @Test
    fun `should generate answer and return token usage and finish reason stop`() {
        if (!isModelConfigured) return
        
        val document = Documents.parse(productHtml, url)
        
        val prompt = "以下是一个电商网站的网页内容，找出商品标题、商品价格："
        val response = model.call(document, prompt)
        println(response.content)
        
        assertTrue { response.tokenUsage.inputTokenCount > 0 }
        assertTrue { response.tokenUsage.outputTokenCount > 0 }
        assertTrue { response.tokenUsage.totalTokenCount > 0 }
        assertTrue { response.state == ResponseState.STOP }
    }
    
    @Test
    fun `should generate answer from the partial content of a webpage`() {
        if (!isModelConfigured) return
        
        val text = productText
        val prompt = """
以下我将提供一个典型电商网站的商品页面内容，找出商品标题、商品价格和评分，并且以以下格式输出：

从提供的网页内容中，以下是商品的相关信息：

- **商品标题**:
  （这里是商品标题）

- **商品价格**:
   （这里是商品价格）

- **商品评分**:
    （这里是商品评分）
        """.trimIndent()
        val response = model.call(text, prompt)
        println(response.content)
        
        assertTrue { response.tokenUsage.inputTokenCount > 0 }
        assertTrue { response.tokenUsage.outputTokenCount > 0 }
        assertTrue { response.tokenUsage.totalTokenCount > 0 }
        assertTrue { response.state == ResponseState.STOP }
    }
    
    @Test
    fun `When ask LLM to analyze cluster then it responses with json`() {
        if (!isModelConfigured) return
        
        val response = model.call(clusterAnalysisPrompt)
        println(response.content)
        
        assertTrue { response.tokenUsage.inputTokenCount > 0 }
        assertTrue { response.tokenUsage.outputTokenCount > 0 }
        assertTrue { response.tokenUsage.totalTokenCount > 0 }
        assertTrue { response.state == ResponseState.STOP }
    }
}

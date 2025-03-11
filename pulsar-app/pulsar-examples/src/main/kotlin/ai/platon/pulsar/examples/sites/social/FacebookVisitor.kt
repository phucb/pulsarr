package ai.platon.pulsar.examples.sites.social

import ai.platon.pulsar.skeleton.PulsarSettings
import ai.platon.pulsar.skeleton.context.PulsarContexts

fun main() {
    // see https://console.volcengine.com/ark/region:ark+cn-beijing/endpoint
    val apiKey = System.getProperty("llm.apiKey")
    PulsarSettings()
        .withLLMProvider("volcengine")
        .withLLMName("ep-20250218132011-2scs8")
        .withLLMAPIKey(apiKey)

    // Use the default browser which has an isolated profile
    PulsarSettings().withSystemDefaultBrowser()
    // Create a pulsar session
    val session = PulsarContexts.createSession()
    // The main url we are playing with
    val url = "https://www.facebook.com/openai"
    val document = session.loadDocument(url)
    val response = session.chat("Tell me something about this webpage", document)
    println(response)
}
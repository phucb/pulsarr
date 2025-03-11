package ai.platon.pulsar.skeleton.session

import ai.platon.pulsar.common.Systems
import ai.platon.pulsar.common.config.CapabilityTypes.H2_SESSION_FACTORY_CLASS

@Deprecated("Deprecated for removal")
class PulsarEnvironment {
    companion object {
        val VITAL_PROPERTIES = mutableMapOf(
            H2_SESSION_FACTORY_CLASS to "ai.platon.pulsar.ql.h2.H2SessionFactory",

            /**
             * Use the sequential privacy agent generator by default.
             * If the prototype data directory doesn't exist, it acts as a browse in incognito mode.
             * If the prototype data directory exists, it copies and inherits the prototype Chrome browser's
             * user data directory.
             * */
            // Use the default is OK: ai.platon.pulsar.skeleton.crawl.fetch.privacy.DefaultPrivacyAgentGenerator
//            PRIVACY_AGENT_GENERATOR_CLASS to "ai.platon.pulsar.skeleton.crawl.fetch.privacy.SequentialPrivacyAgentGenerator"
        )
    }

    init {
        initialize()
    }

    @Synchronized
    private fun initialize() {
        VITAL_PROPERTIES.forEach { (key, value) -> Systems.setPropertyIfAbsent(key, value) }
    }
}

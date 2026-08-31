package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper

object StatusEffectConfigLoader {

    private val configs: Map<String, StatusEffectConfig> = loadConfigs()

    private fun loadConfigs(): Map<String, StatusEffectConfig> {
        return try {
            val inputStream = requireNotNull(
                StatusEffectConfigLoader::class.java
                    .getResourceAsStream("/status_effects.json")
            ) {
                "Could not find /status_effects.json"
            }

            val effects = ObjectMapper().readValue(
                inputStream,
                Array<StatusEffectConfig>::class.java
            )

            effects.associateBy { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @JvmStatic
    fun getConfig(name: String): StatusEffectConfig? =
        configs[name]

    @JvmStatic
    fun getAll(): Collection<StatusEffectConfig> =
        configs.values
}
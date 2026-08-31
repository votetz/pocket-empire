package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper

object FactionConfigLoader {

    private val configs: Map<String, FactionConfig> = loadConfigs()

    private fun loadConfigs(): Map<String, FactionConfig> {
        return try {
            val inputStream = requireNotNull(
                FactionConfigLoader::class.java
                    .getResourceAsStream("/factions.json")
            ) {
                "Could not find /factions.json"
            }

            inputStream.use {
                val factions = ObjectMapper().readValue(
                    it,
                    Array<FactionConfig>::class.java
                )

                factions.associateBy { faction -> faction.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @JvmStatic
    fun getConfig(id: String): FactionConfig? =
        configs[id]

    @JvmStatic
    fun getAll(): Collection<FactionConfig> =
        configs.values
}
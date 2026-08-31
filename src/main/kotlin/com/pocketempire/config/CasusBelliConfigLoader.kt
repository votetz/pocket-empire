package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper

object CasusBelliConfigLoader {

    private val configs: Map<String, CasusBelliConfig> = loadConfigs()

    private fun loadConfigs(): Map<String, CasusBelliConfig> {
        return try {
            val inputStream = requireNotNull(
                CasusBelliConfigLoader::class.java
                    .getResourceAsStream("/casus_belli.json")
            ) {
                "Could not find /casus_belli.json"
            }

            inputStream.use {
                val casusBelli = ObjectMapper().readValue(
                    it,
                    Array<CasusBelliConfig>::class.java
                )

                casusBelli.associateBy { config -> config.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @JvmStatic
    fun getConfig(id: String): CasusBelliConfig? =
        configs[id]

    @JvmStatic
    fun getAll(): Collection<CasusBelliConfig> =
        configs.values
}

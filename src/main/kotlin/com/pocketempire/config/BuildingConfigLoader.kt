package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper

object BuildingConfigLoader {

    private val configs: Map<String, BuildingConfig> = loadConfigs()

    private fun loadConfigs(): Map<String, BuildingConfig> {
        return try {
            val inputStream = requireNotNull(
                BuildingConfigLoader::class.java
                    .getResourceAsStream("/buildings.json")
            ) {
                "Could not find /buildings.json"
            }

            inputStream.use {
                val buildings = ObjectMapper().readValue(
                    it,
                    Array<BuildingConfig>::class.java
                )

                buildings.associateBy { building -> building.name }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @JvmStatic
    fun getConfig(name: String): BuildingConfig? =
        configs[name]

    @JvmStatic
    fun getAll(): Collection<BuildingConfig> =
        configs.values
}
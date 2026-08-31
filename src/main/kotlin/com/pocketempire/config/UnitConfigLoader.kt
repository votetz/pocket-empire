package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.pocketempire.units.UnitStats

object UnitConfigLoader {

    private val configs = HashMap<String, UnitStats>()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        val mapper = ObjectMapper()

        val inputStream = requireNotNull(
            UnitConfigLoader::class.java.getResourceAsStream("/units.json")
        ) {
            "Could not find /units.json"
        }

        val units = mapper.readValue(
            inputStream,
            Array<UnitStats>::class.java
        )

        for (unit in units) {
            configs[unit.type] = unit
        }
    }

    @JvmStatic
    fun getConfig(type: String): UnitStats {
        return configs[type]
            ?: throw IllegalArgumentException(
                "Unexpected config type: $type"
            )
    }
}
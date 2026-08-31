package com.pocketempire.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.pocketempire.units.UnitRole
import java.util.EnumMap

object RoleConfigLoader {

    private val counterMap: Map<UnitRole, Set<UnitRole>> by lazy {
        load()
    }

    @JvmStatic
    fun getCounters(): Map<UnitRole, Set<UnitRole>> = counterMap

    private fun load(): Map<UnitRole, Set<UnitRole>> {
        val inputStream = requireNotNull(
            RoleConfigLoader::class.java.getResourceAsStream("/counters.json")
        ) {
            "Could not find /counters.json"
        }

        val raw: Map<String, List<String>> = ObjectMapper().readValue(
            inputStream,
            object : TypeReference<Map<String, List<String>>>() {}
        )

        return EnumMap<UnitRole, Set<UnitRole>>(UnitRole::class.java).apply {
            raw.forEach { (roleName, targetNames) ->
                put(
                    UnitRole.valueOf(roleName),
                    targetNames.mapTo(HashSet(), UnitRole::valueOf)
                )
            }
        }
    }
}

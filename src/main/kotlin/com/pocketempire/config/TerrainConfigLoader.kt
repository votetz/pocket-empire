package com.pocketempire.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.pocketempire.tiles.TileType
import java.util.EnumMap

object TerrainConfigLoader {

    private val bonuses: Map<TileType, IntArray> by lazy {
        load()
    }

    @JvmStatic
    fun getDefendBonus(type: TileType): Int =
        bonuses[type]?.get(0) ?: 0

    @JvmStatic
    fun getAttackModifier(type: TileType): Int =
        bonuses[type]?.get(1) ?: 0

    private fun load(): Map<TileType, IntArray> {
        try {
            val inputStream = requireNotNull(
                TerrainConfigLoader::class.java
                    .getResourceAsStream("/terrain.json")
            ) {
                "Could not find /terrain.json"
            }

            inputStream.use {
                val raw: Map<String, Map<String, Int>> = ObjectMapper().readValue(
                    it,
                    object : TypeReference<Map<String, Map<String, Int>>>() {}
                )

                return EnumMap<TileType, IntArray>(TileType::class.java).apply {
                    raw.forEach { (name, values) ->
                        val type = TileType.valueOf(name)
                        val defendBonus = values["defendBonus"] ?: 0
                        val attackModifier = values["attackModifier"] ?: 0

                        put(type, intArrayOf(defendBonus, attackModifier))
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to load terrain.json", e)
        }
    }
}
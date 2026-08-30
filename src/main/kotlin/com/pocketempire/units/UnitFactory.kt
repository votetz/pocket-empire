package com.pocketempire.units

import com.pocketempire.config.UnitConfigLoader
import com.pocketempire.entities.Unit

object UnitFactory {

    @JvmStatic
    fun create(
        type: UnitType,
        id: String,
        name: String,
        q: Int,
        r: Int,
        factionId: String
    ): Unit = createUnit(type, id, name, q, r, factionId)

    @JvmStatic
    fun createUnit(
        type: UnitType,
        id: String,
        name: String,
        q: Int,
        r: Int,
        factionId: String
    ): Unit {

        val stats = UnitConfigLoader.getConfig(type.name)

        return Unit.Builder(id, q, r, factionId)
            .name(name)
            .config(stats)
            .unitType(type)
            .build()
    }
}

package com.pocketempire.units

import com.pocketempire.config.RoleConfigLoader

enum class UnitRole {
    ASSAULT, TANK, ASSASSIN, SNIPER, SIEGE, SUPPORT, CIVILIAN;

    fun counters(other: UnitRole): Boolean =
        counterMap[this]?.contains(other) == true

    fun getAttackBonus(target: UnitRole): Int =
        if (counters(target)) 2 else 0

    private companion object {
        val counterMap: Map<UnitRole, Set<UnitRole>> by lazy {
            RoleConfigLoader.getCounters()
        }
    }
}

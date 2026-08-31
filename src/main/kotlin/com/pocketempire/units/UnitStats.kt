package com.pocketempire.units

import com.fasterxml.jackson.annotation.JsonProperty

data class UnitStats(
    var type: String = "",
    var hp: Int = 0,
    var attack: Int = 0,
    var defense: Int = 0,
    var movement: Int = 0,
    var range: Int = 0,
    var cost: Int = 0,
    var movementType: MovementType? = null,

    @field:JsonProperty("role")
    var unitRole: UnitRole? = null,

    var effectChance: Double = 0.0,
    var requiredTech: String? = null,
    var roleByAbility: Map<String, UnitRole> = emptyMap()
)

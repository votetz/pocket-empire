package com.pocketempire.config

data class FactionConfig(
    var id: String = "",
    var name: String = "",
    var theme: String = "",
    var icon: String = "",
    var color: String = "",
    var description: String = "",

    var atkBonus: Int = 0,
    var movementBonus: Int = 0,
    var entrenchBonus: Int = 0,
    var effectChanceBonus: Double = 0.0,
    var forestDefBonus: Int = 0,
    var heavyHpBonus: Int = 0,
    var sightBonus: Int = 0,

    var lightUnitCostReduction: Int = 0,
    var catapultCostReduction: Int = 0,
    var mageCostReduction: Int = 0,
    var researchMultiplier: Double = 1.0,
    var wallCostReductionPercent: Int = 0,

    var startingReputation: Int = 0,
    var startingGold: Int = 0,
    var borderContactRepDrain: Int = 0,
    var betrayalThreshold: Int = 0,

    var warAtkBonusPerWar: Int = 0,
    var goldDividendPercent: Double = 0.0,

    var preferredUnits: List<String> = emptyList(),
    var aiPersonality: String = ""
)
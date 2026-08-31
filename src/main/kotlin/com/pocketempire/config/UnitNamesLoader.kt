package com.pocketempire.config

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Random

object UnitNamesLoader {

    private const val NAMES_FILE = "/name_pools.json"
    private var config: NamesConfig? = null
    private val usedNames = HashSet<String>()
    private val random = Random()

    init {
        loadNames()
    }

    private fun loadNames() {
        try {
            val inputStream = requireNotNull(
                UnitNamesLoader::class.java.getResourceAsStream(NAMES_FILE)
            ) {
                "Could not find $NAMES_FILE"
            }

            inputStream.use {
                config = ObjectMapper().readValue(it, NamesConfig::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getRandomName(): String {
        val namesConfig = config ?: return "Unknown Unit"

        if (namesConfig.first_names.isNotEmpty()) {
            val name = namesConfig.first_names.removeAt(random.nextInt(namesConfig.first_names.size))
            usedNames.add(name)
            return name
        }

        if (namesConfig.prefixes.isNotEmpty() && namesConfig.suffixes.isNotEmpty()) {
            repeat(100) {
                val name = namesConfig.prefixes[random.nextInt(namesConfig.prefixes.size)] +
                    namesConfig.suffixes[random.nextInt(namesConfig.suffixes.size)]
                if (usedNames.add(name)) {
                    return name
                }
            }
        }

        return "Unknown Unit"
    }

    @JvmStatic
    fun getRandomCityName(): String {
        val namesConfig = config ?: return "New City"

        if (namesConfig.city_names.isEmpty()) {
            return "New City"
        }

        val name = namesConfig.city_names.removeAt(random.nextInt(namesConfig.city_names.size))
        usedNames.add(name)
        return name
    }
}

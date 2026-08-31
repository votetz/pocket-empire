package com.pocketempire.config

data class NamesConfig(
    @JvmField
    var first_names: MutableList<String> = mutableListOf(),

    @JvmField
    var prefixes: MutableList<String> = mutableListOf(),

    @JvmField
    var suffixes: MutableList<String> = mutableListOf(),

    @JvmField
    var city_names: MutableList<String> = mutableListOf()
)
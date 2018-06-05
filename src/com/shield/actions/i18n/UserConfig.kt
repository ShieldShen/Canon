package com.shield.actions.i18n

class UserConfig {
    var defaultModule: String = ""

    var sourceRes: String? = ""

    var selectedAllI18n: Boolean = true

    var resOptions: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()
}
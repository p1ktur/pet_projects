package com.renatsolocorp.dutyapp.profile

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.*

class SettingsContextWrapper(context: Context): ContextWrapper(context) {
    companion object {

        fun wrap(context: Context, language: String): ContextWrapper {
            val config = context.resources.configuration
            val sysLocale: Locale = getSystemLocale(config)
            if (language != "" && sysLocale.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                setSystemLocale(config, locale)
            }
            return SettingsContextWrapper(context.createConfigurationContext(config))
        }

        private fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        private fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}
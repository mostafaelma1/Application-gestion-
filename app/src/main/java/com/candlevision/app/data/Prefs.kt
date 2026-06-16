package com.candlevision.app.data

import android.content.Context

/**
 * Thin wrapper over SharedPreferences for the user-configurable settings:
 * analysis language and default market. (The analysis runs fully on-device,
 * so there is no API key.)
 */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("candlevision_prefs", Context.MODE_PRIVATE)

    /** "fr" or "darija". */
    var language: String
        get() = sp.getString(KEY_LANG, LANG_FR) ?: LANG_FR
        set(value) = sp.edit().putString(KEY_LANG, value).apply()

    /** "crypto", "forex" or "stocks". */
    var market: String
        get() = sp.getString(KEY_MARKET, MARKET_CRYPTO) ?: MARKET_CRYPTO
        set(value) = sp.edit().putString(KEY_MARKET, value).apply()

    companion object {
        const val LANG_FR = "fr"
        const val LANG_DARIJA = "darija"
        const val MARKET_CRYPTO = "crypto"
        const val MARKET_FOREX = "forex"
        const val MARKET_STOCKS = "stocks"

        private const val KEY_LANG = "analysis_language"
        private const val KEY_MARKET = "default_market"
    }
}

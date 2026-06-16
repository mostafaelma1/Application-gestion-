package com.candlevision.app.data

import android.content.Context
import com.candlevision.app.BuildConfig

/**
 * Thin wrapper over SharedPreferences for the user-configurable settings:
 * API key, analysis language and default market.
 */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("candlevision_prefs", Context.MODE_PRIVATE)

    /** Falls back to the optional build-time key when the user has not set one. */
    var apiKey: String
        get() = sp.getString(KEY_API, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_ANTHROPIC_API_KEY
        set(value) = sp.edit().putString(KEY_API, value.trim()).apply()

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

        private const val KEY_API = "anthropic_api_key"
        private const val KEY_LANG = "analysis_language"
        private const val KEY_MARKET = "default_market"
    }
}

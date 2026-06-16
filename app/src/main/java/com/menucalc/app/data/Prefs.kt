package com.menucalc.app.data

import android.content.Context

/** Réglages simples persistés : la devise affichée dans toute l'app. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("menucalc_prefs", Context.MODE_PRIVATE)

    /** Symbole de devise : "DH", "€", "$"… */
    var currency: String
        get() = sp.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        set(value) = sp.edit().putString(KEY_CURRENCY, value).apply()

    companion object {
        const val DEFAULT_CURRENCY = "DH"
        private const val KEY_CURRENCY = "currency"
    }
}

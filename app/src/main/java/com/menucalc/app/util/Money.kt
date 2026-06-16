package com.menucalc.app.util

import java.util.Locale

/** Formatage des montants et pourcentages, avec la devise choisie. */
object Money {

    /** Ex. : 12.5 -> "12,50 DH". */
    fun format(amount: Double, currency: String): String =
        String.format(Locale.FRANCE, "%,.2f %s", amount, currency)

    /** Ex. : 31.7 -> "31,7 %". */
    fun percent(value: Double): String =
        String.format(Locale.FRANCE, "%.1f %%", value)
}

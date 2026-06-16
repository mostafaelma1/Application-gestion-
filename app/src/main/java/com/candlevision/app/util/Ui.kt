package com.candlevision.app.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.candlevision.app.R

/** Maps the constrained direction / risk labels to terminal colors. */
object Ui {

    fun directionColor(context: Context, direction: String): Int {
        val d = direction.lowercase()
        val res = when {
            d.contains("achat") -> R.color.bull
            d.contains("vente") -> R.color.bear
            d.contains("danger") -> R.color.bear
            else -> R.color.neutral // Attente
        }
        return ContextCompat.getColor(context, res)
    }

    fun riskColor(context: Context, risk: String): Int {
        val r = risk.lowercase()
        val res = when {
            r.contains("faible") -> R.color.risk_low
            r.startsWith("élev") || r.startsWith("elev") -> R.color.risk_high
            else -> R.color.risk_medium // Moyen
        }
        return ContextCompat.getColor(context, res)
    }
}

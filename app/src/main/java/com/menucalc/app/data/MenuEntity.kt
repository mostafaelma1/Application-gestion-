package com.menucalc.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un menu servi à un nombre de couverts donné. Les pourcentages de frais
 * généraux et de marge servent à proposer un prix de vente par personne.
 */
@Entity(tableName = "menus")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Nombre de personnes / couverts servis. */
    val couverts: Int,
    /** Frais généraux en % du coût matière (main d'œuvre, énergie…). */
    val feesPercent: Double,
    /** Marge bénéficiaire souhaitée en %. */
    val marginPercent: Double,
    val createdAt: Long = System.currentTimeMillis(),
)

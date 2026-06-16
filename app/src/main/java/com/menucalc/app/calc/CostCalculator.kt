package com.menucalc.app.calc

/** Un plat tel qu'il entre dans le calcul d'un menu. */
data class DishCost(
    /** Coût matière total de la recette. */
    val totalCost: Double,
    /** Nombre de portions de la recette (>= 1). */
    val portions: Int,
) {
    /** Coût matière d'une seule portion. */
    val costPerPortion: Double
        get() = if (portions > 0) totalCost / portions else 0.0
}

/** Résultat chiffré complet d'un menu, par personne et pour le service entier. */
data class MenuResult(
    val couverts: Int,
    val materialPerPerson: Double,
    val feesPerPerson: Double,
    val costPricePerPerson: Double,
    val marginPerPerson: Double,
    val sellingPerPerson: Double,
    val materialTotal: Double,
    val sellingTotal: Double,
    val marginTotal: Double,
    val foodCostPercent: Double,
)

/**
 * Calculs de coût d'un menu de restauration collective. Fonctions pures,
 * sans dépendance Android, faciles à tester.
 *
 * Modèle de prix :
 *   coût matière (CM)      = Σ coût/portion des plats
 *   frais généraux         = CM × frais%
 *   coût de revient        = CM + frais généraux
 *   marge bénéficiaire     = coût de revient × marge%
 *   prix de vente conseillé = coût de revient + marge
 *   food cost %            = CM / prix de vente × 100
 */
object CostCalculator {

    fun menuResult(
        dishes: List<DishCost>,
        couverts: Int,
        feesPercent: Double,
        marginPercent: Double,
    ): MenuResult {
        val material = dishes.sumOf { it.costPerPortion }
        val fees = material * feesPercent / 100.0
        val costPrice = material + fees
        val margin = costPrice * marginPercent / 100.0
        val selling = costPrice + margin
        val foodCost = if (selling > 0) material / selling * 100.0 else 0.0
        val n = couverts.coerceAtLeast(0)

        return MenuResult(
            couverts = couverts,
            materialPerPerson = material,
            feesPerPerson = fees,
            costPricePerPerson = costPrice,
            marginPerPerson = margin,
            sellingPerPerson = selling,
            materialTotal = material * n,
            sellingTotal = selling * n,
            marginTotal = margin * n,
            foodCostPercent = foodCost,
        )
    }
}

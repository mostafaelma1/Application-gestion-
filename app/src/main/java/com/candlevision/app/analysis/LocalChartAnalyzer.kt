package com.candlevision.app.analysis

import android.graphics.Bitmap
import android.graphics.Color
import com.candlevision.app.api.AnalysisResult
import com.candlevision.app.data.Prefs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Fully on-device, offline chart analysis — no API key, no network, free.
 *
 * It reads the candlestick screenshot with simple image processing: it detects
 * green (bullish) and red (bearish) candle pixels, estimates the overall trend
 * from how the price drifts left-to-right, measures volatility, and turns those
 * facts into a pedagogical explanation (FR / Darija).
 *
 * This is a heuristic, not a cloud AI — it works best on standard green/red
 * candlestick charts (Binance, TradingView, MetaTrader…).
 */
object LocalChartAnalyzer {

    private const val WORK_WIDTH = 700

    private data class Metrics(
        val isChart: Boolean,
        val bullPercent: Int,
        val trendPercent: Int,   // >0 price rising left→right
        val volatility: Double,  // % of chart height
    )

    fun analyze(bitmap: Bitmap, language: String, market: String): AnalysisResult {
        val m = measure(bitmap)
        return buildResult(m, language, market)
    }

    // --- 1. Image processing ---

    private fun measure(src: Bitmap): Metrics {
        val scaled = scaleForWork(src)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        var totalGreen = 0L
        var totalRed = 0L
        val sumY = DoubleArray(w)
        val cnt = IntArray(w)

        val hsv = FloatArray(3)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val p = pixels[row + x]
                Color.colorToHSV(p, hsv)
                val hue = hsv[0]; val sat = hsv[1]; val value = hsv[2]
                if (sat < 0.25f || value < 0.20f) continue // background / grid

                val green = hue in 70f..185f && sat > 0.25f
                val red = (hue <= 18f || hue >= 335f) && sat > 0.30f
                if (green) {
                    totalGreen++
                } else if (red) {
                    totalRed++
                } else {
                    continue
                }
                sumY[x] = sumY[x] + y.toDouble()
                cnt[x] = cnt[x] + 1
            }
        }

        val coverage = (totalGreen + totalRed).toDouble() / (w * h)
        val isChart = coverage >= 0.004 && (totalGreen + totalRed) > 0

        val totalCandle = (totalGreen + totalRed).coerceAtLeast(1)
        val bullPercent = (totalGreen.toDouble() / totalCandle * 100).roundToInt()

        // Per-column price level (top of screen = higher price → invert Y).
        val levels = ArrayList<Pair<Int, Double>>(w)
        for (x in 0 until w) {
            if (cnt[x] > 0) levels.add(x to (h - sumY[x] / cnt[x]))
        }

        val trendPercent: Int
        val volatility: Double
        if (levels.size < 6 || !isChart) {
            trendPercent = 0
            volatility = 0.0
        } else {
            val third = levels.size / 3
            val leftAvg = levels.take(third).map { it.second }.average()
            val rightAvg = levels.takeLast(third).map { it.second }.average()
            trendPercent = ((rightAvg - leftAvg) / h * 100).roundToInt()

            val all = levels.map { it.second }
            val mean = all.average()
            val variance = all.sumOf { (it - mean) * (it - mean) } / all.size
            volatility = sqrt(variance) / h * 100
        }

        if (scaled != src) scaled.recycle()
        return Metrics(isChart, bullPercent, trendPercent, volatility)
    }

    private fun scaleForWork(src: Bitmap): Bitmap {
        if (src.width <= WORK_WIDTH) return src
        val ratio = WORK_WIDTH.toFloat() / src.width
        val h = (src.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, WORK_WIDTH, h, true)
    }

    // --- 2. Interpretation ---

    private enum class Trend { UP, DOWN, FLAT }

    private fun buildResult(m: Metrics, language: String, market: String): AnalysisResult {
        val darija = language == Prefs.LANG_DARIJA

        val trend = when {
            !m.isChart -> Trend.FLAT
            m.trendPercent >= 3 -> Trend.UP
            m.trendPercent <= -3 -> Trend.DOWN
            else -> Trend.FLAT
        }
        val volHigh = m.volatility >= 20
        val volMed = m.volatility in 10.0..20.0
        val bearPercent = 100 - m.bullPercent

        val direction = when {
            !m.isChart -> "Attente"
            volHigh && trend == Trend.FLAT -> "Zone dangereuse"
            trend == Trend.UP -> "Achat possible"
            trend == Trend.DOWN -> "Vente possible"
            else -> "Attente"
        }

        val risk = when {
            !m.isChart -> "Élevé"
            direction == "Zone dangereuse" || volHigh -> "Élevé"
            volMed -> "Moyen"
            else -> "Faible"
        }

        return if (darija) {
            darijaResult(m, trend, direction, risk, bearPercent, market)
        } else {
            frenchResult(m, trend, direction, risk, bearPercent, market)
        }
    }

    // --- 3a. French text ---

    private fun frenchResult(
        m: Metrics, trend: Trend, direction: String, risk: String,
        bearPercent: Int, market: String,
    ): AnalysisResult {
        if (!m.isChart) {
            return AnalysisResult(
                direction = "Attente",
                trend = "Indéterminée",
                risk = "Élevé",
                analysis = "Je n'ai pas réussi à reconnaître des chandeliers verts/rouges " +
                    "clairs sur cette image. Assure-toi d'importer une capture nette d'un " +
                    "graphique en chandeliers japonais (Binance, TradingView, MetaTrader…), " +
                    "bien cadrée et sans trop de texte.",
                zones = "Non détectées : l'image n'est pas un graphique en chandeliers lisible.",
                scenario = "Réimporte une capture plus claire pour obtenir une analyse fiable.",
                advice = "N'entre jamais dans un trade sur la base d'une image illisible. " +
                    "Cette analyse est éducative et locale (hors-ligne).",
            )
        }

        val trendLabel = when (trend) {
            Trend.UP -> "Haussière"
            Trend.DOWN -> "Baissière"
            Trend.FLAT -> "Latérale"
        }
        val trendReason = when (trend) {
            Trend.UP -> "le prix forme des sommets et des creux de plus en plus hauts"
            Trend.DOWN -> "le prix forme des sommets et des creux de plus en plus bas"
            Trend.FLAT -> "le prix évolue dans une zone horizontale sans direction nette"
        }
        val volLabel = volLabelFr(m.volatility)

        val analysis = buildString {
            append("D'après les bougies détectées sur l'image : environ ${m.bullPercent}% sont ")
            append("haussières (vertes) et $bearPercent% baissières (rouges). ")
            append("La tendance générale semble $trendLabel, car $trendReason. ")
            append("La volatilité paraît $volLabel, ce qui indique ")
            append(
                when {
                    m.volatility >= 20 -> "des mouvements larges : le prix bouge vite et le risque augmente."
                    m.volatility >= 10 -> "des mouvements modérés, à surveiller."
                    else -> "des mouvements calmes pour le moment."
                }
            )
            append(" Rappelle-toi : une tendance se confirme par une cassure nette accompagnée ")
            append("d'une bougie forte, pas par une seule bougie isolée.")
        }

        val zones = "Support (zone basse) : la partie inférieure récente du graphique, là où le " +
            "prix a rebondi. Résistance (zone haute) : la partie supérieure récente, là où le prix " +
            "a été rejeté. Zone d'entrée possible : attends que le prix revienne tester l'une de " +
            "ces zones et réagisse clairement avant d'envisager une entrée."

        val scenario = when (trend) {
            Trend.UP -> "Si le prix casse la résistance avec une forte bougie verte et clôture " +
                "au-dessus, la hausse peut continuer. S'il rejette la résistance, une correction " +
                "vers le support devient probable."
            Trend.DOWN -> "Si le prix casse le support avec une forte bougie rouge et clôture " +
                "en-dessous, la baisse peut continuer. S'il rebondit sur le support, un rebond " +
                "technique vers la résistance est possible."
            Trend.FLAT -> "Tant que le prix reste coincé dans la zone, mieux vaut attendre une " +
                "cassure claire (vers le haut ou vers le bas) avant d'agir."
        }

        val advice = "Sur le marché ${marketLabel(market)} : ne te précipite pas. " +
            "La meilleure décision actuelle est « $direction », mais n'entre jamais sans une " +
            "confirmation claire (cassure + clôture). Place toujours un stop loss logique " +
            "derrière la zone invalidée et ne risque qu'une petite partie de ton capital. " +
            "⚠️ Analyse locale et éducative — ce n'est pas un conseil financier."

        return AnalysisResult(
            direction = direction,
            trend = "$trendLabel (pente ≈ ${m.trendPercent}%)",
            risk = risk,
            analysis = analysis,
            zones = zones,
            scenario = scenario,
            advice = advice,
        )
    }

    private fun volLabelFr(v: Double): String = when {
        v >= 20 -> "élevée"
        v >= 10 -> "moyenne"
        else -> "faible"
    }

    // --- 3b. Darija text ---

    private fun darijaResult(
        m: Metrics, trend: Trend, direction: String, risk: String,
        bearPercent: Int, market: String,
    ): AnalysisResult {
        if (!m.isChart) {
            return AnalysisResult(
                direction = "Attente",
                trend = "ماشي واضحة",
                risk = "Élevé",
                analysis = "ما قدرتش نلقى شموع خضر/حمر واضحين فهاد الصورة. تأكد بلي درتي " +
                    "تصويرة واضحة ديال graphique بالشموع اليابانية (Binance, TradingView…)، " +
                    "مقادة مزيان وبلا بزاف ديال الكتابة.",
                zones = "ما تلقاوش: الصورة ماشي graphique ديال الشموع واضح.",
                scenario = "عاود صيفط تصويرة أوضح باش يطلع ليك تحليل مزيان.",
                advice = "عمرك ما تدخل صفقة على صورة ماشي واضحة. هاد التحليل تعليمي ومحلي (offline).",
            )
        }

        val trendLabel = when (trend) {
            Trend.UP -> "Haussière"
            Trend.DOWN -> "Baissière"
            Trend.FLAT -> "Latérale"
        }
        val trendReason = when (trend) {
            Trend.UP -> "الثمن كيطلع وكيدير قمم وقيعان أعلى من قبل"
            Trend.DOWN -> "الثمن كيهبط وكيدير قمم وقيعان أقل من قبل"
            Trend.FLAT -> "الثمن كيتحرك فمنطقة أفقية بلا اتجاه واضح"
        }
        val volLabel = when {
            m.volatility >= 20 -> "عالية"
            m.volatility >= 10 -> "متوسطة"
            else -> "ضعيفة"
        }

        val analysis = "حسب الشموع اللي بان فالصورة: تقريبا ${m.bullPercent}% خضر (صاعدة) و " +
            "$bearPercent% حمر (هابطة). الاتجاه العام كيبان $trendLabel، حيت $trendReason. " +
            "التذبذب (volatilité) كيبان $volLabel. تفكر: الاتجاه كيتأكد بكسر واضح مع شمعة قوية، " +
            "ماشي بشمعة وحدة."

        val zones = "الدعم (support) = الجزء السفلي ديال graphique فين الثمن رجع طلع. " +
            "المقاومة (résistance) = الجزء العلوي فين الثمن ترفض. منطقة الدخول الممكنة: تسنى " +
            "الثمن يرجع يختبر وحدة من هاد المناطق ويعطي رد فعل واضح قبل ما تفكر تدخل."

        val scenario = when (trend) {
            Trend.UP -> "إلا كسر الثمن المقاومة بشمعة خضرة قوية وسد فوقها، الصعود يمكن يكمل. " +
                "إلا ترفض المقاومة، يمكن يجي تصحيح نحو الدعم."
            Trend.DOWN -> "إلا كسر الثمن الدعم بشمعة حمرة قوية وسد تحتها، الهبوط يمكن يكمل. " +
                "إلا رجع من الدعم، يمكن يكون ارتداد نحو المقاومة."
            Trend.FLAT -> "مادام الثمن محصور فالمنطقة، الأحسن تسنى كسر واضح (فوق ولا تحت) قبل ما تتحرك."
        }

        val advice = "فسوق ${marketLabel(market)}: ما تزربش. القرار الأحسن دابا هو « $direction »، " +
            "ولكن عمرك ما تدخل بلا تأكيد واضح (كسر + إغلاق). ديما حط stop loss منطقي ومخاطر " +
            "غير بشوية ديال الرأس مال. ⚠️ تحليل محلي وتعليمي — ماشي نصيحة مالية."

        return AnalysisResult(
            direction = direction,
            trend = "$trendLabel (ميلان ≈ ${m.trendPercent}%)",
            risk = risk,
            analysis = analysis,
            zones = zones,
            scenario = scenario,
            advice = advice,
        )
    }

    private fun marketLabel(market: String): String = when (market) {
        Prefs.MARKET_FOREX -> "Forex"
        Prefs.MARKET_STOCKS -> "Actions"
        else -> "Crypto"
    }
}

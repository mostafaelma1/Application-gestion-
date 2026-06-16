package com.candlevision.app.api

import com.candlevision.app.BuildConfig
import com.candlevision.app.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Calls the Anthropic Messages API (vision + structured outputs) to analyse a
 * trading chart screenshot like a teacher.
 *
 * NOTE: the request is sent directly from the device using the user's own API
 * key (stored locally). For a published product you would proxy this through a
 * backend so the key is never shipped to clients — this is acceptable here for
 * a personal / educational tool.
 */
class ClaudeClient(private val prefs: Prefs) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Thrown for predictable, user-facing failures (no key, network, API error). */
    class AnalysisException(val kind: Kind, message: String) : Exception(message) {
        enum class Kind { NO_KEY, NETWORK, API }
    }

    suspend fun analyze(imageBase64Jpeg: String): AnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = prefs.apiKey
        if (apiKey.isBlank()) {
            throw AnalysisException(AnalysisException.Kind.NO_KEY, "missing api key")
        }

        val body = buildRequestBody(imageBase64Jpeg, prefs.language, prefs.market)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        val responseText = try {
            http.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    throw AnalysisException(
                        AnalysisException.Kind.API,
                        "HTTP ${resp.code}: ${extractApiError(text)}"
                    )
                }
                text
            }
        } catch (e: IOException) {
            throw AnalysisException(AnalysisException.Kind.NETWORK, e.message ?: "network error")
        }

        parseResult(responseText)
    }

    private fun buildRequestBody(imageB64: String, language: String, market: String): JSONObject {
        val imageBlock = JSONObject()
            .put("type", "image")
            .put(
                "source", JSONObject()
                    .put("type", "base64")
                    .put("media_type", "image/jpeg")
                    .put("data", imageB64)
            )

        val textBlock = JSONObject()
            .put("type", "text")
            .put("text", userInstruction(language))

        val content = JSONArray().put(imageBlock).put(textBlock)

        val message = JSONObject()
            .put("role", "user")
            .put("content", content)

        return JSONObject()
            .put("model", BuildConfig.CLAUDE_MODEL)
            .put("max_tokens", 2000)
            .put("system", systemPrompt(language, market))
            .put("messages", JSONArray().put(message))
            // Structured outputs: guarantees the response is valid JSON for our schema.
            .put("output_config", JSONObject().put("format", outputFormat()))
    }

    private fun outputFormat(): JSONObject {
        val props = JSONObject()
        listOf("direction", "trend", "risk", "analysis", "zones", "scenario", "advice")
            .forEach { props.put(it, JSONObject().put("type", "string")) }

        val schema = JSONObject()
            .put("type", "object")
            .put("properties", props)
            .put(
                "required",
                JSONArray(listOf("direction", "trend", "risk", "analysis", "zones", "scenario", "advice"))
            )
            .put("additionalProperties", false)

        return JSONObject().put("type", "json_schema").put("schema", schema)
    }

    private fun marketLabel(market: String): String = when (market) {
        Prefs.MARKET_FOREX -> "Forex"
        Prefs.MARKET_STOCKS -> "Actions / Bourse"
        else -> "Crypto"
    }

    private fun systemPrompt(language: String, market: String): String {
        val languageRule = if (language == Prefs.LANG_DARIJA) {
            "Rédige les champs 'analysis', 'zones', 'scenario' et 'advice' en DARIJA marocaine, " +
                "avec un langage simple et des exemples."
        } else {
            "Rédige tous les champs en FRANÇAIS simple et pédagogique."
        }

        return """
            Tu es un professeur de trading expérimenté et pédagogue, spécialisé dans la lecture
            des graphiques en chandeliers japonais. On te fournit une image d'un graphique de trading
            (marché concerné : ${marketLabel(market)}, plateformes possibles : Binance, TradingView,
            MetaTrader...). Analyse l'image comme un professeur qui explique à un débutant.

            Examine : les chandeliers japonais, la tendance générale, les supports et résistances,
            les zones d'achat/vente, les figures chartistes, les cassures et faux signaux, la force
            des acheteurs et des vendeurs, les zones de risque, le volume si visible, le timeframe si
            visible, et le comportement probable du prix. Explique TOUJOURS pourquoi.

            $languageRule

            Contraintes strictes sur certains champs (toujours en français, même en mode Darija,
            pour rester cohérent) :
            - "direction" : exactement l'une de ces valeurs : "Achat possible", "Vente possible",
              "Attente", "Zone dangereuse".
            - "trend" : commence par "Haussière", "Baissière" ou "Latérale", suivi d'une courte raison.
            - "risk" : exactement "Faible", "Moyen" ou "Élevé".
            - "analysis" : explication pédagogique complète et structurée.
            - "zones" : supports, résistances et zone d'entrée possible.
            - "scenario" : ce qui peut arriver si le prix casse ou respecte une zone (haussier vs baissier).
            - "advice" : conseil de prudence ; rappelle toujours qu'il ne faut jamais entrer sans confirmation.

            Règles : ne donne jamais de promesse de gain. Reste éducatif. Si l'image n'est pas un
            graphique de trading lisible, explique-le dans "analysis" et mets "direction" à "Attente"
            et "risk" à "Élevé".
        """.trimIndent()
    }

    private fun userInstruction(language: String): String = if (language == Prefs.LANG_DARIJA) {
        "Analyser had le graphique bحال أستاذ ديال التداول، وعطيني التحليل بالداريجة المغربية حسب الشكل المطلوب."
    } else {
        "Analyse ce graphique comme un professeur de trading et renvoie l'analyse au format demandé."
    }

    /** Reads the JSON text block produced under output_config.format. */
    private fun parseResult(responseText: String): AnalysisResult {
        val root = JSONObject(responseText)
        val content = root.optJSONArray("content")
            ?: throw AnalysisException(AnalysisException.Kind.API, "réponse invalide")

        var jsonText: String? = null
        for (i in 0 until content.length()) {
            val block = content.getJSONObject(i)
            if (block.optString("type") == "text") {
                jsonText = block.optString("text")
                break
            }
        }
        if (jsonText.isNullOrBlank()) {
            throw AnalysisException(AnalysisException.Kind.API, "réponse vide")
        }

        val obj = JSONObject(jsonText)
        return AnalysisResult(
            direction = obj.optString("direction", "Attente"),
            trend = obj.optString("trend", ""),
            risk = obj.optString("risk", "Moyen"),
            analysis = obj.optString("analysis", ""),
            zones = obj.optString("zones", ""),
            scenario = obj.optString("scenario", ""),
            advice = obj.optString("advice", ""),
        )
    }

    private fun extractApiError(body: String): String = try {
        JSONObject(body).optJSONObject("error")?.optString("message") ?: body.take(180)
    } catch (_: Exception) {
        body.take(180)
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

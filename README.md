# CandleVision — Trading AI 📈

CandleVision is a native Android app that turns a **photo or screenshot of a
trading chart** into a clear, pedagogical analysis — like a personal trading
teacher. It reads the chart with **Claude Vision** (`claude-sonnet-4-6`) and
explains the market situation in **French** or **Moroccan Darija**.

> ⚠️ **Educational tool only.** CandleVision never promises gains. Every analysis
> is technical education to help you understand the chart — all trading decisions
> remain your own responsibility.

---

## ✨ Features

- **Home screen** — capture a chart with the camera or import a screenshot from
  the gallery, with the 5 most recent analyses listed below.
- **Analysis screen** — shows the chart, an "Analyze" button, a loading
  indicator (*Analyse en cours…*), then a structured result:
  - 📈 Direction du marché — *Achat possible / Vente possible / Attente / Zone dangereuse*
  - 📊 Résumé de la tendance — *Haussière / Baissière / Latérale*
  - ⚠️ Niveau de risque — *Faible / Moyen / Élevé*
  - 🎓 Analyse complète (pédagogique)
  - 🎯 Zones importantes — supports / résistances / zone d'entrée
  - 🔮 Scénario probable
  - 💡 Conseil du professeur
- **Local history** — every analysis is saved to **SQLite (Room)** with the date,
  a thumbnail and the market direction; tap any item to reopen it.
- **Settings** — choose the analysis **language** (Français / Darija) and the
  **default market** (Crypto / Forex / Actions), and store your Anthropic API key.

---

## 🏗️ Tech stack

| Concern        | Choice                                            |
| -------------- | ------------------------------------------------- |
| Language       | Kotlin                                            |
| UI             | Android Views + Material 3 + ViewBinding          |
| Persistence    | Room (SQLite)                                      |
| Networking     | OkHttp → Anthropic Messages API (vision + JSON)   |
| Async          | Kotlin Coroutines                                  |
| Min / Target   | Android 7.0 (API 24) / Android 14 (API 34)        |

The chart is downscaled to ~1568 px, JPEG-encoded and sent to the
`/v1/messages` endpoint as a base64 `image` block. The request uses
**structured outputs** (`output_config.format`) so the response is always valid
JSON for the seven result sections.

---

## 🚀 Getting started

1. Open the project in **Android Studio** (Hedgehog or newer) — it will create
   `local.properties` with your `sdk.dir` automatically. From the command line,
   copy `local.properties.sample` to `local.properties` and set `sdk.dir`.
2. (Optional) add `ANTHROPIC_API_KEY=sk-ant-…` to `local.properties` to ship a
   default key, **or** just leave it empty and enter the key in the app's
   **Settings** screen on first launch.
3. Build & run:
   ```bash
   ./gradlew assembleDebug
   ```

You need an Anthropic API key from <https://console.anthropic.com>.

---

## 🔐 A note on the API key

For simplicity this app calls Anthropic **directly from the device** using the
user's own key (stored locally in `SharedPreferences`, excluded from backups).
That is fine for a personal / educational app. For a **published product**, put
the key behind a backend proxy so it is never shipped to clients.

---

## 📂 Project structure

```
app/src/main/java/com/candlevision/app/
├── CandleVisionApp.kt        # Application: DB + prefs holders
├── MainActivity.kt           # Home: capture / import / history
├── AnalysisActivity.kt       # Analyze a chart or view a saved analysis
├── SettingsActivity.kt       # Language / market / API key
├── api/
│   ├── ClaudeClient.kt       # Anthropic Messages API call (vision + JSON)
│   └── AnalysisResult.kt
├── data/                     # Room entity, DAO, database, Prefs
├── ui/HistoryAdapter.kt      # RecyclerView for the history list
└── util/                     # ImageUtils (downscale/EXIF/base64), Ui colors
```

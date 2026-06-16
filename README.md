# CandleVision — Trading AI 📈

CandleVision is a native Android app that turns a **photo or screenshot of a
trading chart** into a clear, pedagogical analysis — like a personal trading
teacher. The analysis runs **100% on the device, offline and free**: no API key,
no account, no internet. The app reads the **green / red candlesticks** and the
**trend** directly from the image.

> ⚠️ **Educational tool only.** CandleVision never promises gains. The on-device
> analysis is a heuristic (it reads candle colors, trend and volatility) — useful
> to learn, but not a financial advisor. Every trading decision is your own.

---

## ✨ Features

- **Home screen** — capture a chart with the camera or import a screenshot from
  the gallery, with the 5 most recent analyses listed below.
- **Analysis screen** — shows the chart, an "Analyze" button, a short loading
  indicator, then a structured result:
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
  **default market** (Crypto / Forex / Actions). No API key needed.

---

## 🧠 How the on-device analysis works

`LocalChartAnalyzer` processes the screenshot with plain image processing — no
network, no ML model download:

1. Each pixel is classified by **HSV color** as a bullish (green) or bearish
   (red) candle pixel, ignoring the background and grid.
2. The **trend** is estimated from how the average price level drifts from the
   left third to the right third of the chart (top of screen = higher price).
3. **Volatility** is the spread of the per-column price levels.
4. Those facts (bull/bear ratio, trend slope, volatility) are turned into a
   teacher-style explanation in **French** or **Moroccan Darija**.

Works best on standard green/red candlestick charts (Binance, TradingView,
MetaTrader…). If no clear candles are detected, the app says so instead of
inventing an analysis.

---

## 🏗️ Tech stack

| Concern        | Choice                                            |
| -------------- | ------------------------------------------------- |
| Language       | Kotlin                                            |
| UI             | Android Views + Material 3 + ViewBinding          |
| Persistence    | Room (SQLite)                                      |
| Analysis       | On-device image processing (offline, free)        |
| Async          | Kotlin Coroutines                                  |
| Min / Target   | Android 7.0 (API 24) / Android 14 (API 34)        |

No internet permission. No third-party network SDK.

---

## 🚀 Getting started

1. Open the project in **Android Studio** (Hedgehog or newer) — it creates
   `local.properties` with your `sdk.dir` automatically. From the command line,
   copy `local.properties.sample` to `local.properties` and set `sdk.dir`.
2. Build & run:
   ```bash
   ./gradlew assembleDebug
   ```

A pre-built APK is also published automatically by GitHub Actions on every push
to the development branch — see the repository **Releases** for a direct download.

---

## 📂 Project structure

```
app/src/main/java/com/candlevision/app/
├── CandleVisionApp.kt         # Application: DB + prefs holders
├── MainActivity.kt            # Home: capture / import / history
├── AnalysisActivity.kt        # Analyze a chart or view a saved analysis
├── SettingsActivity.kt        # Language / market
├── analysis/
│   └── LocalChartAnalyzer.kt  # Offline on-device chart analysis
├── api/AnalysisResult.kt      # Result model (7 sections)
├── data/                      # Room entity, DAO, database, Prefs
├── ui/HistoryAdapter.kt       # RecyclerView for the history list
└── util/                      # ImageUtils (downscale/EXIF), Ui colors
```

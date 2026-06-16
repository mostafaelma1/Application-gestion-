package com.candlevision.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.candlevision.app.analysis.LocalChartAnalyzer
import com.candlevision.app.api.AnalysisResult
import com.candlevision.app.data.Analysis
import com.candlevision.app.databinding.ActivityAnalysisBinding
import com.candlevision.app.util.ImageUtils
import com.candlevision.app.util.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private val app get() = application as CandleVisionApp

    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val analysisId = intent.getLongExtra(EXTRA_ANALYSIS_ID, -1L)
        imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)

        binding.btnAnalyze.setOnClickListener { analyze() }
        binding.btnRetry.setOnClickListener { analyze() }

        if (analysisId > 0) {
            loadExisting(analysisId)
        } else {
            val path = imagePath
            if (path == null) {
                finish()
                return
            }
            binding.imgChart.setImageBitmap(ImageUtils.decodeThumb(path, 1000))
        }
    }

    private fun loadExisting(id: Long) {
        binding.btnAnalyze.visibility = View.GONE
        lifecycleScope.launch {
            val saved = app.database.analysisDao().byId(id) ?: run { finish(); return@launch }
            imagePath = saved.imagePath
            binding.imgChart.setImageBitmap(ImageUtils.decodeThumb(saved.imagePath, 1000))
            render(saved.toResult())
        }
    }

    private fun analyze() {
        val path = imagePath ?: return
        showLoading()

        lifecycleScope.launch {
            try {
                // On-device analysis — no API key, no network.
                val result = withContext(Dispatchers.Default) {
                    val bitmap = ImageUtils.decodeThumb(path, 1200)
                        ?: error("image illisible")
                    LocalChartAnalyzer.analyze(bitmap, app.prefs.language, app.prefs.market)
                }
                save(path, result)
                render(result)
            } catch (e: Exception) {
                showError(getString(R.string.error_generic))
            }
        }
    }

    private suspend fun save(path: String, result: AnalysisResult) {
        val entity = Analysis(
            imagePath = path,
            direction = result.direction,
            trend = result.trend,
            risk = result.risk,
            analysis = result.analysis,
            zones = result.zones,
            scenario = result.scenario,
            advice = result.advice,
            language = app.prefs.language,
            market = app.prefs.market,
        )
        app.database.analysisDao().insert(entity)
    }

    private fun render(result: AnalysisResult) {
        binding.loadingBox.visibility = View.GONE
        binding.errorBox.visibility = View.GONE
        binding.btnAnalyze.visibility = View.GONE
        binding.resultContainer.visibility = View.VISIBLE

        binding.txtDirection.text = result.direction
        binding.txtDirection.setTextColor(Ui.directionColor(this, result.direction))

        binding.txtTrend.text = result.trend

        binding.txtRisk.text = result.risk
        binding.txtRisk.setTextColor(Ui.riskColor(this, result.risk))

        binding.txtAnalysis.text = result.analysis
        binding.txtZones.text = result.zones
        binding.txtScenario.text = result.scenario
        binding.txtAdvice.text = result.advice
    }

    private fun showLoading() {
        binding.loadingBox.visibility = View.VISIBLE
        binding.errorBox.visibility = View.GONE
        binding.resultContainer.visibility = View.GONE
        binding.btnAnalyze.isEnabled = false
    }

    private fun showError(message: String) {
        binding.loadingBox.visibility = View.GONE
        binding.resultContainer.visibility = View.GONE
        binding.errorBox.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = true
        binding.btnAnalyze.visibility = View.VISIBLE
        binding.txtError.text = message
    }

    private fun Analysis.toResult() = AnalysisResult(
        direction = direction,
        trend = trend,
        risk = risk,
        analysis = analysis,
        zones = zones,
        scenario = scenario,
        advice = advice,
    )

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val EXTRA_ANALYSIS_ID = "extra_analysis_id"
    }
}

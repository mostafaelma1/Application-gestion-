package com.candlevision.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.candlevision.app.databinding.ActivityMainBinding
import com.candlevision.app.ui.HistoryAdapter
import com.candlevision.app.util.ImageUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: HistoryAdapter

    private val app get() = application as CandleVisionApp

    private var pendingCapturePath: String? = null

    // --- Activity result launchers ---

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else toast(getString(R.string.permission_camera_denied))
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingCapturePath
        if (success && path != null) {
            ImageUtils.processCaptured(path)
            openAnalysis(path)
        }
        pendingCapturePath = null
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching { ImageUtils.importUri(this, uri) }
                .onSuccess { openAnalysis(it) }
                .onFailure { toast(getString(R.string.error_generic)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter { analysis ->
            startActivity(
                Intent(this, AnalysisActivity::class.java)
                    .putExtra(AnalysisActivity.EXTRA_ANALYSIS_ID, analysis.id)
            )
        }
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        binding.btnCamera.setOnClickListener { ensureCameraThenCapture() }
        binding.btnGallery.setOnClickListener { pickImage.launch("image/*") }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val recent = app.database.analysisDao().recent(5)
            adapter.submit(recent)
            binding.emptyState.visibility =
                if (recent.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun ensureCameraThenCapture() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun launchCamera() {
        val (file, uri) = ImageUtils.newCaptureFile(this)
        pendingCapturePath = file.absolutePath
        try {
            takePicture.launch(uri)
        } catch (_: Exception) {
            pendingCapturePath = null
            toast(getString(R.string.error_no_camera))
        }
    }

    private fun openAnalysis(imagePath: String) {
        startActivity(
            Intent(this, AnalysisActivity::class.java)
                .putExtra(AnalysisActivity.EXTRA_IMAGE_PATH, imagePath)
        )
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

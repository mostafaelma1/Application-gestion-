package com.candlevision.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.candlevision.app.data.Prefs
import com.candlevision.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val app get() = application as CandleVisionApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = app.prefs

        // Pre-fill with the current saved values.
        when (prefs.language) {
            Prefs.LANG_DARIJA -> binding.langDarija.isChecked = true
            else -> binding.langFrench.isChecked = true
        }
        when (prefs.market) {
            Prefs.MARKET_FOREX -> binding.marketForex.isChecked = true
            Prefs.MARKET_STOCKS -> binding.marketStocks.isChecked = true
            else -> binding.marketCrypto.isChecked = true
        }

        binding.btnSave.setOnClickListener { save(prefs) }
    }

    private fun save(prefs: Prefs) {
        prefs.language = if (binding.langDarija.isChecked) Prefs.LANG_DARIJA else Prefs.LANG_FR

        prefs.market = when {
            binding.marketForex.isChecked -> Prefs.MARKET_FOREX
            binding.marketStocks.isChecked -> Prefs.MARKET_STOCKS
            else -> Prefs.MARKET_CRYPTO
        }

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        finish()
    }
}

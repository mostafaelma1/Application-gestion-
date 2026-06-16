package com.menucalc.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.menucalc.app.data.Prefs
import com.menucalc.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = (application as MenuCalcApp).prefs

        b.header.headerTitle.setText(R.string.settings_title)
        b.header.headerBack.setOnClickListener { finish() }

        b.editCurrency.setText(prefs.currency)

        b.btnSave.setOnClickListener {
            val value = b.editCurrency.text.toString().trim()
            prefs.currency = value.ifEmpty { Prefs.DEFAULT_CURRENCY }
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

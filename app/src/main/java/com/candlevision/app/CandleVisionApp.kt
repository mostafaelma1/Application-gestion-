package com.candlevision.app

import android.app.Application
import com.candlevision.app.data.AppDatabase
import com.candlevision.app.data.Prefs

class CandleVisionApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: Prefs by lazy { Prefs(this) }
}

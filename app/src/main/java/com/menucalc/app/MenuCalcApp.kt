package com.menucalc.app

import android.app.Application
import com.menucalc.app.data.AppDatabase
import com.menucalc.app.data.Prefs

class MenuCalcApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: Prefs by lazy { Prefs(this) }
}

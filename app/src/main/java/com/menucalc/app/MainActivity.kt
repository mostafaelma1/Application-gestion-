package com.menucalc.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.menucalc.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.cardIngredients.setOnClickListener {
            startActivity(Intent(this, IngredientsActivity::class.java))
        }
        b.cardDishes.setOnClickListener {
            startActivity(Intent(this, DishesActivity::class.java))
        }
        b.cardMenus.setOnClickListener {
            startActivity(Intent(this, MenusActivity::class.java))
        }
        b.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

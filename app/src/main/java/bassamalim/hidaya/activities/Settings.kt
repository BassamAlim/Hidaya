package bassamalim.hidaya.activities

import android.R.id
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import bassamalim.hidaya.R
import bassamalim.hidaya.screens.SettingsScreen
import bassamalim.hidaya.utils.ActivityUtils

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        setContentView(R.layout.activity_settings)
        findViewById<View>(id.home).setOnClickListener { onBackPressed() }

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, SettingsScreen.newInstance()).commit()
    }

}
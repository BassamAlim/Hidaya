package bassamalim.hidaya.activities

import android.R.id
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import bassamalim.hidaya.R
import bassamalim.hidaya.fragments.SettingsFragment
import bassamalim.hidaya.other.Utils

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        setContentView(R.layout.activity_settings)
        findViewById<View>(id.home).setOnClickListener { onBackPressed() }

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, SettingsFragment.newInstance()).commit()
    }

}
package bassamalim.hidaya.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityWelcomeBinding
import bassamalim.hidaya.screens.LocationScreen
import bassamalim.hidaya.screens.SettingsScreen
import bassamalim.hidaya.utils.ActivityUtils

class WelcomeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().replace(
                R.id.frame, SettingsScreen.newInstance(true)
            ).commit()

        setListeners()
    }

    private fun setListeners() {
        binding.saveBtn.setOnClickListener {
            binding.saveBtn.visibility = View.GONE

            supportFragmentManager.beginTransaction().replace(
                R.id.frame, LocationScreen.newInstance("initial")
            ).commit()

            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putBoolean("new_user", false)
            editor.apply()
        }
    }

}
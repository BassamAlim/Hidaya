package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityWelcomeBinding
import bassamalim.hidaya.fragments.SettingsFragment
import bassamalim.hidaya.other.Utils

class WelcomeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().replace(
                R.id.settings, SettingsFragment.newInstance("initial")
            ).commit()

        setListeners()
    }

    private fun setListeners() {
        binding.saveBtn.setOnClickListener {
            binding.settings.visibility = View.GONE
            binding.saveBtn.visibility = View.GONE
            binding.disclaimerSpace.visibility = View.VISIBLE

            val editor: SharedPreferences.Editor = PreferenceManager
                .getDefaultSharedPreferences(this).edit()
            editor.putBoolean("new_user", false)
            editor.apply()
        }

        binding.agreed.setOnClickListener {
            val intent = Intent(this, Splash::class.java)
            startActivity(intent)
            finish()
        }
        binding.rejected.setOnClickListener { launchAnyway() }
    }

    private fun launchAnyway() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("located", false)
        startActivity(intent)
        finish()
    }

}
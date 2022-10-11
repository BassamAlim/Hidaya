package bassamalim.hidaya.activities

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityCollectionTelawatBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.screens.TelawatFragment
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatCollectionActivity : FragmentActivity() {

    private lateinit var binding: ActivityCollectionTelawatBinding
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityCollectionTelawatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { finish() }

        viewPager = findViewById(R.id.telawat_pager)
        adapter = TAdapter(this)
        viewPager.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val tabs = arrayOf(getString(R.string.all), getString(R.string.favorite), getString(R.string.downloaded))
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        setupContinue()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()

            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                val location: Location? = Keeper(this).retrieveLocation()
                intent.putExtra("located", location != null)
                intent.putExtra("location", location)
                startActivity(intent)
                finish()
            }
        }
        else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    private fun setupContinue() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val mediaId = pref.getString("last_played_media_id", "")!!

        if (mediaId.isEmpty()) binding.continueListening.text = getString(R.string.no_last_play)
        else {
            val db = DBUtils.getDB(this)

            val reciterId = mediaId.substring(0, 3).toInt()
            val versionId = mediaId.substring(3, 5).toInt()
            val suraIndex = mediaId.substring(5).toInt()

            val suraName =
                if (PrefUtils.getLanguage(this, pref) == "en")
                    db.suarDao().getNameEn(suraIndex)
                else db.suarDao().getName(suraIndex)
            val reciterName = db.telawatRecitersDao().getName(reciterId)
            val rewaya = db.telawatVersionsDao().getVersion(reciterId, versionId).getRewaya()

            val text = "${getString(R.string.last_play)}: " +
                    "${getString(R.string.sura)} $suraName ${getString(R.string.for_reciter)} $reciterName ${getString(R.string.in_rewaya_of)} $rewaya"
            binding.continueListening.text = text

            binding.continueListening.setOnClickListener {
                val intent = Intent(this, TelawatClient::class.java)
                intent.action = "continue"
                intent.putExtra("media_id", mediaId)
                startActivity(intent)
            }
        }
    }

}

@RequiresApi(api = Build.VERSION_CODES.O)
internal class TAdapter(fragment: FragmentActivity?) : FragmentStateAdapter(fragment!!) {

    override fun createFragment(position: Int): Fragment {
        val type = when (position) {
            0 -> ListType.All
            1 -> ListType.Favorite
            else -> ListType.Downloaded
        }
        return TelawatFragment(type)
    }

    override fun getItemCount(): Int {
        return 3
    }

}
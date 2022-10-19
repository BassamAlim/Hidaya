package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityCollectionTelawatSuarBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.screens.TelawatSuarFragment
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatSuarCollectionActivity : FragmentActivity() {

    private lateinit var binding: ActivityCollectionTelawatSuarBinding
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityCollectionTelawatSuarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { finish() }

        val intent = intent
        val reciterId = intent.getIntExtra("reciter_id", 0)
        val reciterName = DBUtils.getDB(this).telawatRecitersDao().getName(reciterId)
        val versionId = intent.getIntExtra("version_id", 0)

        binding.topBarTitle.text = reciterName
        viewPager = findViewById(R.id.telawat_pager)
        adapter = TSAdapter(this, reciterId, versionId)
        viewPager.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val tabs = arrayOf(getString(R.string.all), getString(R.string.favorite), getString(R.string.downloaded))
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()

            if (isTaskRoot) {
                val intent = Intent(this, TelawatActivity::class.java)
                startActivity(intent)
            }
        }
        else  // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
    }

}

@RequiresApi(api = Build.VERSION_CODES.O)
internal class TSAdapter(
    fragment: FragmentActivity?, private val reciterId: Int, private val versionId: Int
) : FragmentStateAdapter(fragment!!) {

    override fun createFragment(position: Int): Fragment {
        val type = when (position) {
            0 -> ListType.All
            1 -> ListType.Favorite
            else -> ListType.Downloaded
        }
        return TelawatSuarFragment(type, reciterId, versionId)
    }

    override fun getItemCount(): Int {
        return 3
    }

}
package bassamalim.hidaya.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityCollectionBookChaptersBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.fragments.BookChaptersFragment
import bassamalim.hidaya.utils.ActivityUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class BooksChaptersCollectionActivity : FragmentActivity() {

    private lateinit var binding: ActivityCollectionBookChaptersBinding
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityCollectionBookChaptersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { finish() }

        val intent = intent
        val bookId: Int = intent.getIntExtra("book_id", 0)
        val bookTitle: String = intent.getStringExtra("book_title")!!
        binding.topBarTitle.text = bookTitle

        viewPager = findViewById(R.id.pager)
        adapter = FSAdapter(this, bookId)
        viewPager.adapter = adapter

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val tabs = arrayOf(getString(R.string.all), getString(R.string.favorite))
        TabLayoutMediator(tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = tabs[position] }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else  // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
    }

}

internal class FSAdapter(fragment: FragmentActivity?, private val bookId: Int) :
    FragmentStateAdapter(fragment!!) {

    override fun createFragment(position: Int): Fragment {
        val type = if (position == 0) ListType.All else ListType.Favorite
        return BookChaptersFragment(type, bookId)
    }

    override fun getItemCount(): Int {
        return 2
    }

}
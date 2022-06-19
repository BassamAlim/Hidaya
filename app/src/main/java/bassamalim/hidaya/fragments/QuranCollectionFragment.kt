package bassamalim.hidaya.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.databinding.FragmentCollectionQuranBinding
import bassamalim.hidaya.dialogs.TutorialDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class QuranCollectionFragment : Fragment() {
    private var binding: FragmentCollectionQuranBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionQuranBinding.inflate(layoutInflater)
        checkFirstTime()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val qAdapter = QAdapter(this)
        val viewPager: ViewPager2 = view.findViewById(R.id.quran_pager)
        viewPager.adapter = qAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        val tabs = arrayOf(getString(R.string.all), getString(R.string.favorite))
        TabLayoutMediator(tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = tabs[position] }.attach()
    }

    override fun onResume() {
        super.onResume()
        setupContinue()
    }

    private fun setupContinue() {
        val pref: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val page: Int = pref.getInt("bookmarked_page", -1)
        var text: String = pref.getString("bookmarked_text", "")!!
        if (page == -1) text = getString(R.string.no_bookmarked_page) else {
            text = getString(R.string.bookmarked_page) + text
            binding!!.continueReading.setOnClickListener {
                val intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_page"
                intent.putExtra("page", page)
                requireContext().startActivity(intent)
            }
        }
        binding!!.continueReading.text = text
    }

    private fun checkFirstTime() {
        val key = "is_first_time_in_quran_fragment"
        if (PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(key, true)
        ) TutorialDialog(
            requireContext(), getString(R.string.quran_fragment_tips), key
        ).show(
            requireActivity().supportFragmentManager, TutorialDialog.TAG
        )
    }
}

internal class QAdapter(fragment: Fragment?) : FragmentStateAdapter(fragment!!) {
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) AllQuranFragment() else FavoriteQuranFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}
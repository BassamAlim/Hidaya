package bassamalim.hidaya.fragments

import android.content.Intent
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
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class QuranCollectionFragment : Fragment() {

    private var binding: FragmentCollectionQuranBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        TabLayoutMediator(tabLayout, viewPager) {
                tab: TabLayout.Tab, position: Int -> tab.text = tabs[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        setupContinue()
    }

    private fun setupContinue() {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val page = pref.getInt("bookmarked_page", -1)
        val sura = pref.getInt("bookmarked_sura", -1)

        if (page == -1)
            binding!!.continueReading.text = getString(R.string.no_bookmarked_page)
        else {
            val db = DBUtils.getDB(requireContext())
            val suraName =
                if (PrefUtils.getLanguage(requireContext(), pref) == "en")
                    db.suarDao().getNameEn(sura)
                else db.suarDao().getName(sura)

            val text = "${getString(R.string.bookmarked_page)} ${getString(R.string.page)} " +
                    "${LangUtils.translateNums(requireContext(), page.toString())}," +
                    " ${getString(R.string.sura)} $suraName"

            binding!!.continueReading.setOnClickListener {
                val intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_page"
                intent.putExtra("page", page)
                requireContext().startActivity(intent)
            }

            binding!!.continueReading.text = text
        }
    }

    private fun checkFirstTime() {
        val prefKey = "is_first_time_in_quran_fragment"
        if (PreferenceManager
                .getDefaultSharedPreferences(requireContext()).getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                getString(R.string.quran_fragment_tips), prefKey
            ).show(requireActivity().supportFragmentManager, TutorialDialog.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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
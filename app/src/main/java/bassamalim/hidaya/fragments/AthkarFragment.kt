package bassamalim.hidaya.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import bassamalim.hidaya.activities.AthkarListActivity
import bassamalim.hidaya.databinding.FragmentAthkarBinding

class AthkarFragment : Fragment() {

    private var binding: FragmentAthkarBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAthkarBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        setListeners()

        return root
    }

    private fun setListeners() {
        binding!!.allThikrs.setOnClickListener {
            val intent = Intent(context, AthkarListActivity::class.java)
            intent.action = "all"
            startActivity(intent)
        }

        binding!!.favoriteAthkar.setOnClickListener {
            val intent = Intent(context, AthkarListActivity::class.java)
            intent.action = "favorite"
            startActivity(intent)
        }

        binding!!.dayAndNight.setOnClickListener { showThikrs(0) }
        binding!!.prayers.setOnClickListener { showThikrs(1) }
        binding!!.quran.setOnClickListener { showThikrs(2) }
        binding!!.actions.setOnClickListener { showThikrs(3) }
        binding!!.events.setOnClickListener { showThikrs(4) }
        binding!!.emotions.setOnClickListener { showThikrs(5) }
        binding!!.places.setOnClickListener { showThikrs(6) }
        binding!!.more.setOnClickListener { showThikrs(7) }
    }

    private fun showThikrs(category: Int) {
        val intent = Intent(context, AthkarListActivity::class.java)
        intent.action = "category"
        intent.putExtra("category", category)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
package bassamalim.hidaya.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.*
import bassamalim.hidaya.databinding.FragmentOtherBinding
import bassamalim.hidaya.other.Global

class OtherFragment : Fragment() {

    private var binding: FragmentOtherBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherBinding.inflate(inflater, container, false)

        setListeners()

        return binding!!.root
    }

    fun setListeners() {
        binding!!.telawat.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startActivity(Intent(context, TelawatCollectionActivity::class.java))
            else
                Toast.makeText(context, getString(R.string.feature_not_supported), Toast.LENGTH_SHORT).show()
        }

        binding!!.qibla.setOnClickListener {
            startActivity(Intent(context, QiblaActivity::class.java))
        }

        binding!!.quiz.setOnClickListener {
            startActivity(Intent(context, QuizLobbyActivity::class.java))
        }

        binding!!.books.setOnClickListener {
            startActivity(Intent(context, BooksActivity::class.java))
        }

        binding!!.channels.setOnClickListener {
            startActivity(Intent(context, TvActivity::class.java))
        }

        binding!!.radio.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startActivity(Intent(context, RadioClient::class.java))
            else
                Toast.makeText(context, getString(R.string.feature_not_supported), Toast.LENGTH_SHORT).show()
        }

        binding!!.dateConverter.setOnClickListener {
            startActivity(Intent(context, DateConverter::class.java))
        }

        binding!!.settings.setOnClickListener {
            startActivity(Intent(context, Settings::class.java))
        }

        binding!!.contact.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", Global.CONTACT_EMAIL, null
                )
            )
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hidaya")
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))
        }

        binding!!.share.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Share")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Global.PLAY_STORE_URL)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }

        binding!!.about.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bassamalim.hidaya.databinding.ActivityQuizLobbyBinding
import bassamalim.hidaya.other.Utils

class QuizLobbyActivity : AppCompatActivity() {

    private var binding: ActivityQuizLobbyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityQuizLobbyBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        setListeners()
    }

    private fun setListeners() {
        binding!!.startQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }
    }
}
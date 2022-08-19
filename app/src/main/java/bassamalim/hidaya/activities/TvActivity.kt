package bassamalim.hidaya.activities

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityTvBinding
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.ActivityUtils
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class TvActivity : YouTubeBaseActivity() {

    private lateinit var binding: ActivityTvBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var apiKey: String
    private lateinit var makkahUrl: String
    private lateinit var madinaUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityTvBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        getLinksAndInit()
    }

    private fun getLinksAndInit() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                apiKey = remoteConfig.getString("yt_api_key")
                makkahUrl = remoteConfig.getString("makkah_url")
                madinaUrl = remoteConfig.getString("madina_url")

                Log.i(Global.TAG, "Config params updated")
                Log.i(Global.TAG, "Makkah URL: $makkahUrl")
                Log.i(Global.TAG, "Madina URL: $madinaUrl")

                initYtPlayer()
            }
            else Log.e(Global.TAG, "Fetch failed")
        }
    }

    private fun initYtPlayer() {
        binding.ytPlayer.initialize(apiKey, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?, youTubePlayer: YouTubePlayer, b: Boolean
            ) {
                setListeners(youTubePlayer)
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                youTubeInitializationResult: YouTubeInitializationResult?
            ) {
                Log.e(Global.TAG, java.lang.String.valueOf(youTubeInitializationResult))
                Toast.makeText(applicationContext, getString(R.string.playback_failed), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setListeners(ytPlayer: YouTubePlayer) {
        binding.quranBtn.setOnClickListener {
            ytPlayer.loadVideo(makkahUrl)
            ytPlayer.play()
        }
        binding.sunnahBtn.setOnClickListener {
            ytPlayer.loadVideo(madinaUrl)
            ytPlayer.play()
        }
    }

}
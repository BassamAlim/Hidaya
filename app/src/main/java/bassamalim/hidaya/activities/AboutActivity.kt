package bassamalim.hidaya.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityAboutBinding
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class AboutActivity : AppCompatActivity() {
    private var binding: ActivityAboutBinding? = null
    private var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }
        setListener()
    }

    private fun setListener() {
        binding!!.titleTv.setOnClickListener {
            counter++
            if (counter == 5) voala()
        }
        binding!!.rebuildDb.setOnClickListener {
            deleteDatabase("HidayaDB")
            Log.i(Global.TAG, "Database Rebuilt")
            Utils.reviveDb(this)
            Toast.makeText(
                this, getString(R.string.database_rebuilt),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding!!.driveUpdate.setOnClickListener {
            val url: String = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private fun voala() {
        Toast.makeText(this, getString(R.string.vip_welcome), Toast.LENGTH_SHORT).show()
        binding!!.driveUpdate.visibility = View.VISIBLE
        binding!!.rebuildDb.visibility = View.VISIBLE
    }
}
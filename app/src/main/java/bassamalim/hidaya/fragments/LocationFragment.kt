package bassamalim.hidaya.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.databinding.FragmentLocationBinding
import bassamalim.hidaya.dialogs.LocationPickerDialog
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.DBUtils
import com.google.android.gms.location.LocationServices


class LocationFragment: Fragment() {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private var action: String = "normal"

    companion object {
        fun newInstance(action: String): LocationFragment {
            val fragment = LocationFragment()
            val args = Bundle()
            args.putString("action", action)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        action = arguments?.getString("action", "")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        db = DBUtils.getDB(requireContext())

        setupListeners()

        return root
    }

    private fun setupListeners() {
        binding.locate.setOnClickListener {
            val editor = pref.edit()
            editor.putString("location_type", "auto")
            editor.apply()

            getLocation()
        }

        binding.chooseManually.setOnClickListener {
            val editor = pref.edit()
            editor.putString("location_type", "manual")
            editor.apply()

            locationDialog.launch(Intent(context, LocationPickerDialog::class.java))
        }

        if (action == "initial")
            binding.reject.setOnClickListener {
                val editor = pref.edit()
                editor.putString("location_type", "none")
                editor.apply()

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("located", false)
                startActivity(intent)
                activity?.finish()
            }
        else binding.reject.visibility = View.GONE
    }

    private fun getLocation() {
        if (granted()) {
            locate()
            background()
        }
        else {
            if (pref.getString("location_type", "auto") == "auto")
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            else launch(null)
        }
    }

    private val locationDialog: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!
                val countryId = data.getIntExtra("country_id", -1)
                val cityId = data.getIntExtra("city_id", -1)

                val editor = pref.edit()
                editor.putInt("country_id", countryId)
                editor.putInt("city_id", cityId)
                editor.apply()

                val city = db.cityDao().getCity(cityId)

                val location = Location("")
                location.latitude = city.latitude
                location.longitude = city.longitude

                launch(location)
            }
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener(requireActivity()) { location: Location? ->
                launch(location)

                Log.d(Global.TAG, "START")
                val closestCity = db.cityDao().getClosest(location!!.latitude, location.longitude)
                Log.d(Global.TAG, "END")

                val editor = pref.edit()
                editor.putInt("country_id", closestCity.countryId)
                editor.putInt("city_id", closestCity.id)
                editor.apply()

                background()
            }
    }

    private fun launch(location: Location?) {
        var loc = location

        val intent = Intent(context, MainActivity::class.java)
        if (loc == null) {
            loc = Keeper(requireContext()).retrieveLocation()
            intent.putExtra("located", loc != null)
        }
        else intent.putExtra("located", true)
        intent.putExtra("location", loc)
        startActivity(intent)
        activity?.finish()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION]!!
            && permissions[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
            background()
            getLocation()
        }
        else launch(null)
    }

    private val backgroundLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {}

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context,
                getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG).show()

            backgroundLocationPermissionRequest.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

}
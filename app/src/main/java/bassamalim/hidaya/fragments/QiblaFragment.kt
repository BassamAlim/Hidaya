package bassamalim.hidaya.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.databinding.FragmentQiblaBinding
import bassamalim.hidaya.dialogs.CalibrationDialog
import bassamalim.hidaya.helpers.Compass
import bassamalim.hidaya.helpers.Compass.CompassListener
import bassamalim.hidaya.other.Utils
import kotlin.math.*

class QiblaFragment : Fragment() {

    private val kaabaLat = 21.4224779
    private val kaabaLatInRad = Math.toRadians(kaabaLat)
    private val kaabaLng = 39.8251832
    private var binding: FragmentQiblaBinding? = null
    private var located = true
    private var compass: Compass? = null
    private lateinit var location: Location
    private var currentAzimuth = 0f
    private var distance = 0.0
    private var bearing = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MainActivity.located) {
            location = MainActivity.location!!

            distance = getDistance()
            bearing = calculateBearing()

            setupCompass()

            compass?.start(requireContext())
        }
        else located = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentQiblaBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        if (!located)
            binding!!.distanceText.text = getString(R.string.location_permission_for_qibla)
        else {
            val distanceText = (getString(R.string.distance_to_kaaba) + ": " +
                    Utils.translateNumbers(requireContext(), distance.toString()) + " " + getString(R.string.distance_unit))
            binding!!.distanceText.text = distanceText

            binding!!.accuracyIndicator.setBackgroundColor(Color.TRANSPARENT)
        }

        inflater.inflate(R.layout.fragment_qibla, container, false)
        return root
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (located && compass != null) compass!!.start(requireContext())
    }

    override fun onStop() {
        super.onStop()
        compass?.stop()
    }

    private fun setupCompass() {
        compass = Compass(requireContext())
        val listener: CompassListener = object : CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                adjust(azimuth)
                adjustNorthDial(azimuth)
            }

            override fun calibration(accuracy: Int) {
                updateAccuracy(accuracy)
            }
        }
        compass!!.setListener(listener)
    }

    private fun adjust(azimuth: Float) {
        val target = bearing - currentAzimuth

        val rotate: Animation = RotateAnimation(
            target, -azimuth, Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF, 0.565f
        )
        rotate.duration = 500
        rotate.repeatCount = 0
        rotate.fillAfter = true
        binding!!.qiblaPointer.startAnimation(rotate)

        currentAzimuth = azimuth

        if (target > -2 && target < 2) binding!!.bingo.visibility = View.VISIBLE
        else binding!!.bingo.visibility = View.INVISIBLE
    }

    // maybe points north
    fun adjustNorthDial(azimuth: Float) {
        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true
        binding!!.compass.startAnimation(an)
    }

    private fun getDistance(): Double {
        val earthRadius = 6371.0
        val dLon = Math.toRadians(abs(location.latitude - kaabaLat))
        val dLat = Math.toRadians(abs(location.longitude - kaabaLng))
        val a = sin(dLat / 2) * sin(dLat / 2) + (cos(
            Math.toRadians(
                location.latitude
            )
        ) * cos(Math.toRadians(kaabaLat))
                * sin(dLon / 2) * sin(dLon / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        distance = earthRadius * c
        distance = (distance * 10).toInt() / 10.0

        return distance
    }

    private fun calculateBearing(): Float {
        val result: Float
        val myLatRad = Math.toRadians(location.latitude)
        val lngDiff = Math.toRadians(kaabaLng - location.longitude)
        val y = sin(lngDiff) * cos(kaabaLatInRad)
        val x = cos(myLatRad) * sin(kaabaLatInRad) - (sin(myLatRad)
                * cos(kaabaLatInRad) * cos(lngDiff))
        result = ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()

        return result
    }

    private fun updateAccuracy(accuracy: Int) {
        when (accuracy) {
            3 -> {
                binding!!.accuracyText.setText(R.string.high_accuracy_text)
                binding!!.accuracyIndicator.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.green_dot)
                )
                binding!!.accuracyIndicator.setOnClickListener(null)
            }
            2 -> {
                binding!!.accuracyText.setText(R.string.medium_accuracy_text)
                binding!!.accuracyIndicator.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.yellow_dot)
                )
                binding!!.accuracyIndicator.setOnClickListener(null)
            }
            0, 1 -> {
                binding!!.accuracyText.setText(R.string.low_accuracy_text)
                binding!!.accuracyIndicator.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_warning)
                )
                binding!!.accuracyIndicator.setOnClickListener {
                    CalibrationDialog(requireContext())
                        .show(requireActivity().supportFragmentManager, CalibrationDialog.TAG)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
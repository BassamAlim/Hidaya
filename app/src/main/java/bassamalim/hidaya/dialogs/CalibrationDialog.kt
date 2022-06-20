package bassamalim.hidaya.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import bassamalim.hidaya.R
import com.bumptech.glide.Glide

class CalibrationDialog(private val gContext: Context) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.window!!.setLayout(10, 10)

        val view: View = layoutInflater.inflate(
            R.layout.compass_calibration_gif, LinearLayout(gContext)
        )

        val screen = view.findViewById<ImageView>(R.id.gif_screen)
        screen.setOnClickListener { v: View? -> dismiss() }
        Glide.with(gContext).load(R.drawable.compass_calibration).into(screen)

        return view
    }

    companion object {
        var TAG = "CompassCalibrationGif"
    }
}
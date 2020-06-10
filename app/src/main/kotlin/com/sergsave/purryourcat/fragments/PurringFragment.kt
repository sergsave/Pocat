package com.sergsave.purryourcat.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.fragment_purring.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.Singleton
import com.sergsave.purryourcat.helpers.*

class PurringFragment : Fragment() {

    interface OnEditRequestedListener {
        fun onEditRequested()
    }

    private var transitionName: String? = null
    private var onEditListener: OnEditRequestedListener? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough.create(requireContext())

        arguments?.let {
            transitionName = it.getString(ARG_TRANSITION_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = getActivity() as AppCompatActivity?
        activity?.getSupportActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(getResources().getString(R.string.purring_title))
        }

        setHasOptionsMenu(true)

        ImageUtils.loadInto(context, Singleton.catData?.photoUri, photo_image)

        // Shared element transition
        photo_image.setTransitionName(transitionName)
        photo_image.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                photo_image.viewTreeObserver.removeOnPreDrawListener(this)
                activity?.startPostponedEnterTransition()
                return true;
            }
        })
    }

    fun setOnEditRequestedListener(listener: OnEditRequestedListener) {
        onEditListener = listener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_purring, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            onEditListener?.onEditRequested()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val ARG_TRANSITION_NAME = "TransitionName"

        @JvmStatic
        fun newInstance(transitionName: String?) =
            PurringFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRANSITION_NAME, transitionName)
                }
            }
    }
}

// Trash from MainActivity

//private var mediaPlayer : MediaPlayer? = null
//private var visualiser : Visualizer? = null
//private var mEqualizer : Equalizer? = null
//private var mTimer : Timer ? = null
//
//private val CAPTURE_SIZE = 256
//
//private var type = 1
//
//override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
//
//    val vibrator =
//        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//    val notNullable : ByteArray = waveform ?: ByteArray(0)
//
//    val shiftedWaveform = notNullable.map({it.toUByte().toInt() - 128})
//
//    val waveData = shiftedWaveform
//
//    val max = waveData.max() ?: 0
//    val min = waveData.min() ?: 0
//    val sqrSum = waveData.fold(0) {sum, elem -> sum + elem*elem}
//    val rms = kotlin.math.sqrt(sqrSum.toDouble())
//
//    val toDBm = { value: Double -> 20 * kotlin.math.log10(kotlin.math.abs(value)) }
//    val normalize = { value: Double, maxValue: Double -> -9600 * value / maxValue }
//
//    val rmsDBm = toDBm(rms)
//    val rmsLin = normalize(rms, 128.0 * 128)
//
//    val maxDBm = toDBm(max.toDouble())
//    val minDBm = toDBm(min.toDouble())
//
//    val maxLin = normalize(max.toDouble(), 128.0)
//    val minLin = normalize(min.toDouble(), -128.0)
//
//    val r = { value : Double -> kotlin.math.round(value)}
//
//    val message = """
//    Original  Rms ${r(rms)} Min $min Max $max
//    """
//    Log.i("", message)
//
//    var measurement = MeasurementPeakRms()
//    if(visualizer?.enabled ?: false) {
//        visualizer?.getMeasurementPeakRms(measurement)
//    }
//    Log.i("", "peak : " + measurement.mPeak + " rms : " + measurement.mRms)
//
////        var measurement = MeasurementPeakRms()
////        if(visualizer?.enabled ?: false) {
////            visualizer?.getMeasurementPeakRms(measurement)
////        }
////        Log.i(ContentValues.TAG, "peak : " + measurement.mPeak + " rms : " + measurement.mRms)
////        if (measurement.mPeak > -4500) vibrator.vibrate(15)
//    //       if(measurement.mPeak > 3500 || measurement.mPeak < -3500) vibrator.vibrate(20)
////        if (avg > 20.0 || avg < -20.0) {
////            vibrator.vibrate(15)
////        }
//
////        Log.i(ContentValues.TAG, avg.toString())
//
//    // TEST
//    if(
//        (measurement.mPeak > -6000 && type == 0) || // 6000 это почти мин
//        (measurement.mRms > -4500 && type == 1) || // 6000 хорошо себя на середине показывает
//        (max > 20 && type == 2)|| // в идеале надо 64, но так не работает //дергано звучит
//        (rms > 150 && type == 3) // 127 * sqr(256) / 2 // в идеале бы 1016 но так не работает
//    ) {
//        vibrator.vibrate(30)
//    }
//}
//
//override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
//
//private fun startVisualiser() {
//
//    if(visualiser != null) {
//        visualiser?.setEnabled(true)
//        return
//    }
//
//    visualiser = Visualizer(mediaPlayer?.getAudioSessionId() ?: 0)
//    visualiser?.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, false)
//
//    // Ugly hack https://github.com/felixpalmer/android-visualizer/issues/19
//    mEqualizer = Equalizer(0, mediaPlayer?.getAudioSessionId() ?: 0)
//    mEqualizer?.setEnabled(true)
//
//    //       val t = Visualizer.getMaxCaptureRate()
//    //       val t2 = Visualizer.getCaptureSizeRange()
//
//    visualiser?.setCaptureSize(CAPTURE_SIZE)
//    visualiser?.setEnabled(true)
//    visualiser?.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS)
//    visualiser?.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED)
//}
//
////        image_view.setOnTouchListener { _, event ->
////            if(event.getAction() == ACTION_MOVE) {
////                playAudio()
////                startVisualiser()
//////                vibrate()
////            }
////            true
////        }
//
//setVolumeControlStream (AudioManager.STREAM_MUSIC)
//
//private fun playAudio() {
////        if(mediaPlayer?.isPlaying() ?: false) {
////            mediaPlayer?.pause()`
////            mediaPlayer?.seekTo(0)
////        }
//    mediaPlayer?.start()
//
//    mTimer?.cancel()
//    mTimer?.purge()
//    mTimer = Timer("SettingUp", false)
//    mTimer?.schedule(3000) {
//        mediaPlayer?.pause()
//    }
//}
//
//private fun vibrate() {
//    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//    val pattern : LongArray = longArrayOf(0, 100)
//    if (vibrator.hasVibrator()) { // Vibrator availability checking
//        if (VERSION.SDK_INT >= VERSION_CODES.O) {
//            vibrator.vibrate(createWaveform(pattern, -1)) // New vibrate method for API Level 26 or higher
//        } else {
//            vibrator.vibrate(pattern, -1) // Vibrate method for below API Level 26
//        }
//    }
//}
//
////handle result of picked image
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//
////            mediaPlayer = MediaPlayer.create(this, R.raw.beep)
//    mediaPlayer = MediaPlayer.create(this, data?.data)
//    mediaPlayer?.setLooping(true)
//    visualiser = null // invalidate
//    mediaPlayer?.setOnCompletionListener {
//        visualiser?.setEnabled(false)
//    }
//}
//}

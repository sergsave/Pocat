package com.github.sergsave.purr_your_cat

//import android.R
//import android.R.layout
import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.MeasurementPeakRms
import android.os.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.VibrationEffect.createWaveform
import android.util.Log
import android.view.MotionEvent.ACTION_MOVE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Timer
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), Visualizer.OnDataCaptureListener {

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var viewAdapter: RecyclerView.Adapter<*>
//    private lateinit var viewManager: RecyclerView.LayoutManager

    private var mediaPlayer : MediaPlayer? = null
    private var visualiser : Visualizer? = null
    private var mEqualizer : Equalizer? = null
    private var mTimer : Timer ? = null

    private val CAPTURE_SIZE = 256

    private var type = 1

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {

        val vibrator =
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val notNullable : ByteArray = waveform ?: ByteArray(0)

        val shiftedWaveform = notNullable.map({it.toUByte().toInt() - 128})

        val waveData = shiftedWaveform

        val max = waveData.max() ?: 0
        val min = waveData.min() ?: 0
        val sqrSum = waveData.fold(0) {sum, elem -> sum + elem*elem}
        val rms = kotlin.math.sqrt(sqrSum.toDouble())

        val toDBm = { value: Double -> 20 * kotlin.math.log10(kotlin.math.abs(value)) }
        val normalize = { value: Double, maxValue: Double -> -9600 * value / maxValue }

        val rmsDBm = toDBm(rms)
        val rmsLin = normalize(rms, 128.0 * 128)

        val maxDBm = toDBm(max.toDouble())
        val minDBm = toDBm(min.toDouble())

        val maxLin = normalize(max.toDouble(), 128.0)
        val minLin = normalize(min.toDouble(), -128.0)

        val r = { value : Double -> kotlin.math.round(value)}

        val message = """
    Original  Rms ${r(rms)} Min $min Max $max
    """
        Log.i("", message)

        var measurement = MeasurementPeakRms()
        if(visualizer?.enabled ?: false) {
            visualizer?.getMeasurementPeakRms(measurement)
        }
        Log.i("", "peak : " + measurement.mPeak + " rms : " + measurement.mRms)

//        var measurement = MeasurementPeakRms()
//        if(visualizer?.enabled ?: false) {
//            visualizer?.getMeasurementPeakRms(measurement)
//        }
//        Log.i(ContentValues.TAG, "peak : " + measurement.mPeak + " rms : " + measurement.mRms)
//        if (measurement.mPeak > -4500) vibrator.vibrate(15)
 //       if(measurement.mPeak > 3500 || measurement.mPeak < -3500) vibrator.vibrate(20)
//        if (avg > 20.0 || avg < -20.0) {
//            vibrator.vibrate(15)
//        }

//        Log.i(ContentValues.TAG, avg.toString())

        // TEST
        if(
            (measurement.mPeak > -6000 && type == 0) || // 6000 это почти мин
            (measurement.mRms > -4500 && type == 1) || // 6000 хорошо себя на середине показывает
            (max > 20 && type == 2)|| // в идеале надо 64, но так не работает //дергано звучит
            (rms > 150 && type == 3) // 127 * sqr(256) / 2 // в идеале бы 1016 но так не работает
        ) {
            vibrator.vibrate(30)
        }
    }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {}

    private fun startVisualiser() {

        if(visualiser != null) {
            visualiser?.setEnabled(true)
            return
        }

        visualiser = Visualizer(mediaPlayer?.getAudioSessionId() ?: 0)
        visualiser?.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, false)

        // Ugly hack https://github.com/felixpalmer/android-visualizer/issues/19
        mEqualizer = Equalizer(0, mediaPlayer?.getAudioSessionId() ?: 0)
        mEqualizer?.setEnabled(true)

 //       val t = Visualizer.getMaxCaptureRate()
 //       val t2 = Visualizer.getCaptureSizeRange()

        visualiser?.setCaptureSize(CAPTURE_SIZE)
        visualiser?.setEnabled(true)
        visualiser?.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS)
        visualiser?.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val viewManager = AutoFitGridLayoutManager(this, columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })
        val viewAdapter = CatsListAdapter(arrayListOf(
            CatItem("Simka"),
            CatItem("Masik"),
            CatItem("Uta"),
            CatItem("Sherya"),
            CatItem("Sema"),
            CatItem("Philya"),
            CatItem("Ganya")
        ))

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(itemDecoration)
        }


        setVolumeControlStream (AudioManager.STREAM_MUSIC)

        //BUTTON CLICK
//        choose_img_button.setOnClickListener {
//            //check runtime permission
//            if (VERSION.SDK_INT >= VERSION_CODES.M){
//                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
//                    PackageManager.PERMISSION_DENIED){
//                    //permission denied
//                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
//                    //show popup to request runtime permission
//                    requestPermissions(permissions, PERMISSION_CODE);
//                }
//                else{
//                    //permission already granted
//                    pickImageFromGallery();
//                }
//            }
//            else{
//                //system OS is < Marshmallow
//                pickImageFromGallery();
//            }
//        }
//
//        choose_sound_btn.setOnClickListener {
//            //check runtime permission
//            if (VERSION.SDK_INT >= VERSION_CODES.M){
//                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
//                    PackageManager.PERMISSION_DENIED){
//                    //permission denied
//                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
//                    //show popup to request runtime permission
//                    requestPermissions(permissions, PERMISSION_CODE);
//                }
//                else{
//                    //permission already granted
//                    pickAudio();
//                }
//            }
//            else{
//                //system OS is < Marshmallow
//                pickAudio();
//            }
//        }

//        image_view.setOnTouchListener { _, event ->
//            if(event.getAction() == ACTION_MOVE) {
//                playAudio()
//                startVisualiser()
////                vibrate()
//            }
//            true
//        }

        requestPermission()
    }

    private fun playAudio() {
//        if(mediaPlayer?.isPlaying() ?: false) {
//            mediaPlayer?.pause()`
//            mediaPlayer?.seekTo(0)
//        }
        mediaPlayer?.start()

        mTimer?.cancel()
        mTimer?.purge()
        mTimer = Timer("SettingUp", false)
        mTimer?.schedule(3000) {
            mediaPlayer?.pause()
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern : LongArray = longArrayOf(0, 100)
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                vibrator.vibrate(createWaveform(pattern, -1)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(pattern, -1) // Vibrate method for below API Level 26
            }
        }
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO //音频
        )
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "用户曾拒绝xxxx", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_RECORD_CODE)
            }
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun pickAudio() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "audio/*"
        startActivityForResult(intent, SOUND_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;

        private val SOUND_PICK_CODE = 1002;

        private val PERMISSION_RECORD_CODE = 1003;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            //image_view.setImageURI(data?.data)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == SOUND_PICK_CODE){
//            mediaPlayer = MediaPlayer.create(this, R.raw.beep)
            mediaPlayer = MediaPlayer.create(this, data?.data)
            mediaPlayer?.setLooping(true)
            visualiser = null // invalidate
            mediaPlayer?.setOnCompletionListener {
                visualiser?.setEnabled(false)
            }
        }
    }
}
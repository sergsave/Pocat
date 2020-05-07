package com.example.myapplication

//import android.R
//import android.R.layout
import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import com.example.myapplication.MusicWatcher.WatcherBinder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    private var mediaPlayer : MediaPlayer? = null

//    @SuppressLint("HandlerLeak")
    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what === 1) {
                val vibrator =
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                val t = vibrator.hasAmplitudeControl()
                Log.i(ContentValues.TAG, (msg.obj as Int).toString())

                val volume = msg.obj as Int
                if (volume > 225) {
                    vibrator.vibrate(15)
                }
//                vibrator.vibrate(longArrayOf(20), -1)
//                vibrator.vibrate(createWaveform(longArrayOf(20), intArrayOf(msg.obj as Int), -1) )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //BUTTON CLICK
        choose_img_button.setOnClickListener {
            //check runtime permission
            if (VERSION.SDK_INT >= VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        choose_sound_btn.setOnClickListener {
            //check runtime permission
            if (VERSION.SDK_INT >= VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickAudio();
                }
            }
            else{
                //system OS is < Marshmallow
                pickAudio();
            }
        }

        image_view.setOnTouchListener { _, event ->
            if(event.getAction() == ACTION_MOVE) {
                playAudio()
//                vibrate()
            }
            true
        }

        requestPermission()
        bindService(
            Intent(this, MusicWatcher::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        val service1 = service as WatcherBinder
        service1.service.setListener { max: Int ->
            val message = Message()
            message.what = 1
            message.obj = max
            handler.sendMessage(message)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {}

    private fun playAudio() {
        if(mediaPlayer?.isPlaying() ?: false) {
            mediaPlayer?.pause()
            mediaPlayer?.seekTo(0)
        }
        mediaPlayer?.start()
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
            image_view.setImageURI(data?.data)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == SOUND_PICK_CODE){
//            mediaPlayer = MediaPlayer.create(this, R.raw.beep)
            mediaPlayer = MediaPlayer.create(this, data?.data)
        }
    }
}
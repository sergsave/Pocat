package com.example.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Context.*

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.content.pm.PackageManager
import android.content.Intent
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.VibrationEffect.*
import android.os.Vibrator
import android.view.MotionEvent
import android.view.MotionEvent.*

import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
//import com.example.myProject.R;

class MainActivity : AppCompatActivity() {

    private var mediaPlayer : MediaPlayer? = null

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
                vibrate()
            }
            true
        }

    }

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
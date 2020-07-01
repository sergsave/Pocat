package com.sergsave.purryourcat.vibration

// TODO? Adjustable beat level
// Use release() for free any resources, don't call start() after release()
interface ISoundBeatDetector {
    fun start()
    fun stop()
    fun release()
    fun setOnBeatDetectedListener(listener: ()->Unit)
}
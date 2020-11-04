package com.sergsave.purryourcat.vibration

import io.reactivex.Completable
import io.reactivex.Observable

// TODO? Adjustable beat level

// Use prepare() for acquire necessary resources, don't use detect() before prepare()
// Use release() for free any resources, don't use detect() after release()
interface SoundBeatDetector {
    val detectionPeriodMs: Long
    fun prepare(): Completable
    fun detect(): Observable<Unit>

    // Support only sync impls
    fun release()
}
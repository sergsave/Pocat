package com.sergsave.purryourcat.helpers

import android.media.MediaPlayer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit

interface SoundEffect {
    fun start(onCompleteListener: () -> Unit)
    fun stop()
}

// Volume from 0 to 1.0
open class VolumeChangeSoundEffect(private val player: MediaPlayer,
                                   durationMillis: Long,
                                   startVolume: Float,
                                   endVolume: Float): SoundEffect {
    private val volumes: List<Float>

    init {
        val stepsQuantity = durationMillis / TIME_STEP
        val step = (endVolume - startVolume) / stepsQuantity
        volumes = mutableListOf<Float>().apply {
            for (i in 0..stepsQuantity - 1)
                add(startVolume + step * i)
            add(endVolume)
        }
    }

    private var disposable: Disposable? = null

    override fun start(onCompleteListener: () -> Unit) {
        stop()
        disposable = Observables.zip(
            Observable.fromIterable(volumes),
            Observable.interval(TIME_STEP, TimeUnit.MILLISECONDS)
        )
            .doOnNext { player.setVolume(it.first, it.first) }
            .observeOn(AndroidSchedulers.mainThread())
            .ignoreElements()
            .subscribe({ onCompleteListener() }, { _ -> onCompleteListener() })
    }

    override fun stop() {
        disposable?.dispose()
    }

    companion object { private const val TIME_STEP = 50L }
}

// Just change volume, doesn't control play state
class FadeInSoundEffect(player: MediaPlayer, durationMillis: Long)
    : VolumeChangeSoundEffect(player, durationMillis, 0f, 1f) {}

// Just change volume, doesn't control play state
class FadeOutSoundEffect(player: MediaPlayer, durationMillis: Long)
    : VolumeChangeSoundEffect(player, durationMillis, 1f, 0f) {}

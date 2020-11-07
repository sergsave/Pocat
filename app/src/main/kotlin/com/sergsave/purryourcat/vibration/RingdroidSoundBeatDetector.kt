package com.sergsave.purryourcat.vibration

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.ringdroid.soundfile.SoundFile
import com.sergsave.purryourcat.helpers.FileUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class RingdroidSoundBeatDetector(
    private val context: Context,
    private val audioUri: Uri,
    private val playerPositionFetcher: () -> Int?
) : SoundBeatDetector {

    data class FileData(
        val frameGains: IntArray,
        val frameDurationMs: Int,
        val averageGain: Double,
        val minGain: Int,
        val maxGain: Int
    )

    private lateinit var fileData: FileData

    private fun prepareTempFile(uri: Uri): File? {
        val fileName = FileUtils.getContentFileName(context, uri)
        if (fileName == null)
            return null

        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null)
            return null

        // TODO: To constants
        val tempDir = File(context.cacheDir, "beat_detector")
        tempDir.mkdir()

        val suffix = fileName.substringAfterLast('.', "").let {
            if (it.isNotEmpty()) ".$it" else ""
        }
        val tempFile = File.createTempFile("audio", suffix, tempDir)

        FileUtils.copyStreamToFile(inputStream, tempFile)
        return tempFile
    }

    private fun parseFile(file: File): FileData {
        val soundFile = SoundFile.create(file.absolutePath, null)

        val frameGains = soundFile.frameGains
        val trimmedGains = frameGains.toMutableList()
        trimmedGains.removeAll { it == 0 }
        repeat(trimmedGains.size / 100) {
            trimmedGains.min()?.let { trimmedGains.remove(element = it) }
            trimmedGains.max()?.let { trimmedGains.remove(element = it) }
        }
        val frameDuration = (1000.0 * soundFile.samplesPerFrame / soundFile.sampleRate).roundToInt()

        return FileData(
            frameGains = frameGains,
            frameDurationMs = frameDuration,
            averageGain = trimmedGains.average(),
            minGain = trimmedGains.min() ?: 0,
            maxGain = trimmedGains.max() ?: 0
        )
    }

    override fun prepare(): Completable {
        var tempFile: File? = null
        return Completable.create { emitter ->
            val file = try {
                audioUri.toFile()
            } catch(e: Exception) {
                tempFile = prepareTempFile(audioUri)
                tempFile
            }

            if (file != null) {
                fileData = parseFile(file)
                emitter.onComplete()
            } else
                emitter.onError(IOException("Can't parse file"))

            if (emitter.isDisposed) tempFile?.delete()
        }
            .doOnEvent { tempFile?.delete() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun detect(): Observable<Unit> {
        return Observable.interval(detectionPeriodMs, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter {
                playerPositionFetcher()?.let { isThereBeatInPosition(it) } == true
            }
            .map { Unit }
    }

    private fun isThereBeatInPosition(position: Int): Boolean {
        if (::fileData.isInitialized.not())
            return false

        val unboundFrameNumber = (position.toDouble() / fileData.frameDurationMs).roundToInt()
        val frameNumber = min(unboundFrameNumber, fileData.frameGains.lastIndex)

        val bias = -0.15 // from -1.0 to 1.0
        val toMaxDeviation = fileData.maxGain - fileData.averageGain
        val toMinDeviation = fileData.averageGain - fileData.minGain
        val offset = if (bias > 0) bias * toMaxDeviation else bias * toMinDeviation
        val threshold = fileData.averageGain + offset

        return fileData.frameGains[frameNumber] > threshold
    }

    override fun release() {  }

    override val detectionPeriodMs: Long
        get() = 40
}

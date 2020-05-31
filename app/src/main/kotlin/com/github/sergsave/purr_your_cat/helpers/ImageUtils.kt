package com.github.sergsave.purr_your_cat.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.Element
import android.renderscript.ScriptIntrinsicBlur
import java.io.FileNotFoundException


class ImageUtils {

    enum class BlurIntensity {
        LOW, MEDIUM, HIGH
    }

    companion object {

        fun blur(context: Context, bitmap: Bitmap, intensity: BlurIntensity): Bitmap? {

            val blurRadius = when(intensity) {
                BlurIntensity.LOW -> 5f
                BlurIntensity.MEDIUM -> 15f
                BlurIntensity.HIGH -> 25f
            }

            val inputBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable())
            val outputBitmap = Bitmap.createBitmap(inputBitmap)

            val rs = RenderScript.create(context)
            val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
            val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
            theIntrinsic.setRadius(blurRadius)
            theIntrinsic.setInput(tmpIn)
            theIntrinsic.forEach(tmpOut)
            tmpOut.copyTo(outputBitmap)

            return outputBitmap
        }

        fun getScaledBitmapFromUri(context: Context, uri : Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
            if(reqWidth == 0 || reqHeight == 0)
                return null

            val openStream = { context.getContentResolver().openInputStream(uri) }

            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                return BitmapFactory.Options().run {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeStream(openStream(), null, this)

                    inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                    inJustDecodeBounds = false

                    BitmapFactory.decodeStream(openStream(), null, this)
                }
            }
            catch (e: FileNotFoundException) {
                return null
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }
}
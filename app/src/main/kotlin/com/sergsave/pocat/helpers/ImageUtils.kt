package com.sergsave.pocat.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    fun loadInto(context: Context, uri: Uri?, imageView: ImageView,
                 finishCallback: ((Boolean)->Unit)? = null) {

        if(uri == null) {
            Glide.with(context).clear(imageView)
            imageView.setImageURI(uri)
            finishCallback?.invoke(false)
            return
        }

        Glide.with(context)
            .load(uri)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    finishCallback?.invoke(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    finishCallback?.invoke(true)
                    return false
                }
            })
            .into(imageView)
    }

    // https://code.luasoftware.com/tutorials/android/android-resize-image-file-with-glide/
    fun loadInto(context: Context, uri: Uri?, outputFile: File, width: Int, height: Int,
                 finishCallback: ((Boolean)->Unit)? = null) {
        if(uri == null) {
            finishCallback?.invoke(false)
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(uri)
            .fitCenter()
            .into(object : CustomTarget<Bitmap>(width, height) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    var out: FileOutputStream? = null
                    val finish = { res: Boolean ->
                        out?.flush()
                        out?.close()
                        finishCallback?.invoke(res)
                    }
                    try {
                        out = FileOutputStream(outputFile)
                        val quality = 100
                        resource.compress(Bitmap.CompressFormat.JPEG, quality, out)
                        finish(true)
                    }
                    catch (e: IOException) {
                        finish(false)
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    finishCallback?.invoke(false)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

}
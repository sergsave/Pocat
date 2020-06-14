package com.sergsave.purryourcat.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

object ImageUtils {
    fun loadInto(context: Context?, uri: Uri?, imageView: ImageView, finishCallback: (()->Unit)? = null) {
        if(context == null) {
            finishCallback?.invoke()
            return
        }

        if(uri == null) {
            Glide.with(context).clear(imageView)
            imageView.setImageURI(uri)
            finishCallback?.invoke()
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
                    finishCallback?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    finishCallback?.invoke()
                    return false
                }
            })
            .into(imageView)
    }
}
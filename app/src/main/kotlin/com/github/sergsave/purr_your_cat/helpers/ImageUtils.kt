package com.github.sergsave.purr_your_cat.helpers

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

class ImageUtils {
    companion object {
        fun loadInto(context: Context?, uri: Uri?, imageView: ImageView) {
            if(context == null)
                return

            Glide.with(context).load(uri).into(imageView)
        }
    }
}
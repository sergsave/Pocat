package com.sergsave.purryourcat.helpers

import android.app.Activity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View

class ActionModeController : ActionMode.Callback {

    interface OnActionItemClickListener {
        fun onItemClick(item: MenuItem)
    }

    interface OnFinishedListener {
        fun onFinished()
    }

    var onActionItemClickListener: OnActionItemClickListener? = null
    var onFinishedListener: OnFinishedListener? = null

    var title: String? = null
        set(value) {
            field = value
            mode?.title = value
        }

    var subtitle: String? = null
        set(value) {
            field = value
            mode?.subtitle = value
        }

    private var mode: ActionMode? = null
    private var menuResId: Int = 0

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        mode.menuInflater.inflate(menuResId, menu)
        mode.title = title
        mode.subtitle = subtitle
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        this.mode = null
        onFinishedListener?.onFinished()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        onActionItemClickListener?.onItemClick(item)
        mode.finish()
        return true
    }

    fun startActionMode(activity: Activity,
                        menuResId: Int,
                        title: String? = null,
                        subtitle: String? = null) {
        this.menuResId = menuResId
        this.title = title
        this.subtitle = subtitle
        activity.startActionMode(this)
    }

    fun finishActionMode() {
        mode?.finish()
    }

    fun isInActionMode() = mode != null
}
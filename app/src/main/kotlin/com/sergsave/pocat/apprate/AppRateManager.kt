package com.sergsave.pocat.apprate

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.sergsave.pocat.R
import com.vorlonsoft.android.rate.*
import java.lang.ref.WeakReference

class AppRateManager(private val context: Context) {
    fun onRootActivityStart() {
        AppRate
            .with(context)
            .setThemeResId(R.style.MyTheme_NonMaterialAlertDialog_HighlightPositiveButton)
            .setInstallDays(1)
            .setLaunchTimes(3)
            .setRemindInterval(1)
            .setCancelable(true)
            .setShowLaterButton(true)
            .setTitle(R.string.apprate_dialog_title)
            .setMessage(R.string.apprate_dialog_message)
            .setTextLater(R.string.apprate_dialog_ask_later)
            .setTextNever(R.string.apprate_dialog_ask_never)
            .setTextRateNow(R.string.apprate_dialog_rate_now)
            .setDialogManagerFactory(RemindLaterOnCancelDialogManager.Factory)
            .monitor()
    }

    enum class ActionType() {
        ACCEPT, DECLINE, SHOW_LATER
    }

    fun showAppRateDialogIfPossible(activity: Activity,
                                    onButtonClickListener: ((ActionType)->Unit)?): Boolean {
        AppRate
            .with(context)
            .setOnClickButtonListener { id ->
                when(id.toInt()) {
                    DialogInterface.BUTTON_POSITIVE -> ActionType.ACCEPT
                    DialogInterface.BUTTON_NEGATIVE -> ActionType.DECLINE
                    DialogInterface.BUTTON_NEUTRAL -> ActionType.SHOW_LATER
                    else -> null
                }?.let { onButtonClickListener?.invoke(it) }
            }

        return AppRate.showRateDialogIfMeetsConditions(activity)
    }

    fun dismissRateDialog() {
        AppRate
            .with(context)
            .dismissRateDialog()
    }

    fun clearSavedRateInfo() {
        AppRate
            .with(context)
            .clearSettingsParam()
    }
}

// A little bit hack. Need emulate "Not now" button on dialog cancel
private class RemindLaterOnCancelDialogManager(
    context: Context,
    dialogOptions: DialogOptions,
    storeOptions: StoreOptions
):
    DefaultDialogManager(context, dialogOptions, storeOptions) {

    companion object {
        @Volatile
        private var singleton: WeakReference<RemindLaterOnCancelDialogManager>? = null
    }

    override fun createDialog(): Dialog? {
        val dialog = super.createDialog()

        dialog?.setOnCancelListener {
            neutralListener?.onClick(dialog, DialogInterface.BUTTON_NEUTRAL)
        }

        return dialog
    }

    object Factory : DialogManager.Factory {
        init {
            singleton?.clear()
        }

        override fun clearDialogManager() {
            singleton?.clear()
        }

        override fun createDialogManager(
            context: Context,
            dialogOptions: DialogOptions,
            storeOptions: StoreOptions
        ): DialogManager? {
            if (singleton == null || singleton?.get() == null) {
                synchronized(RemindLaterOnCancelDialogManager::class.java) {
                    if (singleton == null || singleton?.get() == null) {
                        singleton?.clear()
                        singleton = WeakReference(
                            RemindLaterOnCancelDialogManager(
                                context,
                                dialogOptions,
                                storeOptions
                            )
                        )
                    } else {
                        singleton?.get()?.setContext(context)
                    }
                }
            } else {
                singleton?.get()?.setContext(context)
            }
            return singleton?.get()
        }
    }
}

package com.sergsave.purryourcat.fragments

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import com.sergsave.purryourcat.sharing.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable

// Headless and retained fragments for encapsulation of async sharing process
// For sharing just create fragment

class TakeSharingHeadlessFragment: SharingHeadlessFragment<Intent>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cat = arguments?.let { it.getParcelable<Pack>(ARG_PACK)?.cat }
        val single = cat?.let { makeSharingManager(requireContext()).makeTakeObservable(Pack(it)) }
        executeSingle(single)
    }

    companion object {
        private val ARG_PACK = "ArgPack"

        @JvmStatic
        fun newInstance(pack: Pack) =
            TakeSharingHeadlessFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PACK, pack)
                }
            }
    }
}

class GiveSharingHeadlessFragment: SharingHeadlessFragment<Pack>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = arguments?.let { it.getParcelable<Intent>(ARG_INTENT) }
        val single = intent?.let { makeSharingManager(requireContext()).makeGiveObservable(intent) }
        executeSingle(single)
    }

    companion object {
        private val ARG_INTENT = "ArgIntent"

        @JvmStatic
        fun newInstance(intent: Intent) =
            GiveSharingHeadlessFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INTENT, intent)
                }
            }
    }
}

// TODO? Deliver result, if activity was destroyed when sharing finished
abstract class SharingHeadlessFragment<T> : Fragment() {

    enum class ErrorType {
        INVALID_INPUT_DATA,
        ERROR_IN_PROCESS
    }

    interface OnResultListener<T> {
        fun onSuccess(data: T)
        fun onError(error: ErrorType, message: String?)
    }

    var onResultListener: OnResultListener<T>? = null

    private var disposable: Disposable? = null

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDetach() {
        super.onDetach()
        // To avoid leakage
        onResultListener = null
    }

    protected fun executeSingle(single: Single<T>?) {
        if(single == null) {
            onResultListener?.onError(ErrorType.INVALID_INPUT_DATA, null)
            return
        }

        disposable = single.subscribe(
            { data -> onResultListener?.onSuccess(data) },
            { throwable ->
                onResultListener?.onError(ErrorType.ERROR_IN_PROCESS, throwable.message)
            }
        )
    }
}

private fun makeSharingManager(context: Context): SharingManager {
    // TODO: Firebase impl?
    val appContext = context.applicationContext
    return WebSharingManager(
        appContext,
        SendAnywhereNetworkService(appContext),
        ZipDataPacker(appContext),
        cleanCacheOnCreate = true
    )
}
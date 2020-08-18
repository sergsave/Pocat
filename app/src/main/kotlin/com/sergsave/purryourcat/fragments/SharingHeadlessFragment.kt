package com.sergsave.purryourcat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.sharing.SharingManager
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable

// Headless and retained fragments for encapsulation of async sharing process
// For sharing just create fragment

class TakeSharingHeadlessFragment: SharingHeadlessFragment<Intent>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cat = arguments?.let { it.getParcelable<Pack>(ARG_PACK)?.cat }
        val single = cat?.let { getSharingManager(this)?.makeTakeObservable(Pack(it)) }
        executeSingle(single)
    }

    companion object {
        private const val ARG_PACK = "ArgPack"

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

        val intent = arguments?.getParcelable<Intent>(ARG_INTENT)
        val single = intent?.let { getSharingManager(this)?.makeGiveObservable(intent) }
        executeSingle(single)
    }

    companion object {
        private const val ARG_INTENT = "ArgIntent"

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

private fun getSharingManager(fragment: Fragment): SharingManager? {
    return ((fragment.activity?.application) as? MyApplication)?.appContainer?.sharingManager
}
package com.sergsave.purryourcat.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sergsave.purryourcat.sharing.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_sharing.*

class TakeSharingFragment: BaseSharingFragment<Intent>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cat = arguments?.let { it.getParcelable<CatData>(ARG_CAT) }
        val single = cat?.let { SharingManager.instance?.makePrepareObservable(Pack(it))}
        executeSingle(single)
    }

    companion object {
        private val ARG_CAT = "ArgCat"

        @JvmStatic
        fun newInstance(pack: Pack) =
            TakeSharingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CAT, pack.cat)
                }
            }
    }
}

class GiveSharingFragment: BaseSharingFragment<Pack>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = arguments?.let { it.getParcelable<Intent>(ARG_INTENT) }
        val single = intent?.let { SharingManager.instance?.makeExtractObservable(intent) }
        executeSingle(single)
    }

    companion object {
        private val ARG_INTENT = "ArgIntent"

        @JvmStatic
        fun newInstance(intent: Intent) =
            GiveSharingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INTENT, intent)
                }
            }
    }
}

open class BaseSharingFragment<T> : Fragment() {

    interface OnSuccessListener<T> {
        fun onSuccess(data: T)
    }

    interface OnErrorListener {
        fun onError(error: String?)
    }

    interface OnStartFailedListener {
        fun onStartFailed()
    }

    var onSuccessListener: OnSuccessListener<T>? = null
    var onErrorListener: OnErrorListener? = null
    var onStartFailedListener: OnStartFailedListener? = null

    private var disposable: Disposable? = null

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    protected fun executeSingle(single: Single<T>?) {
        if(single == null) {
            onStartFailedListener?.onStartFailed()
            return
        }

        disposable = single.subscribe(
            { data -> onSuccessListener?.onSuccess(data) },
            { throwable ->
                throwable.printStackTrace()
                onErrorListener?.onError(context?.getString(R.string.connection_error))
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sharing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

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
import kotlinx.android.synthetic.main.fragment_sharing.*

class TakeSharingFragment: BaseSharingFragment<Intent>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cat = arguments?.let { it.getParcelable<CatData>(ARG_CAT) }
        val task = cat?.let { SharingManager.instance?.makePrepareTask(Pack(it))}
        executeTask(task)
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
        val task = intent?.let { SharingManager.instance?.makeExtractTask(intent)}
        executeTask(task)
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

    interface OnFinishedListener<T> {
        fun onFinished(data: T?)
        fun onFailed(error: String?)
    }

    var onFinishedListener: OnFinishedListener<T>? = null

    private var task: ISharingTask<T>? = null

    override fun onDestroy() {
        task?.cancel()
        task?.setListener(null)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    protected fun executeTask(task: ISharingTask<T>?) {
        if(task == null) {
            onFinishedListener?.onFinished(null)
            return
        }

        task.setListener(object: ISharingListener<T> {
            override fun onSuccessed(data: T) {
                onFinishedListener?.onFinished(data)
            }

            override fun onCanceled() {
                onFinishedListener?.onFinished(null)
                println("canceled")
            }

            override fun onFailed(error: String) {
                onFinishedListener?.onFailed(error)
                println(error)
            }

            override fun onProgressChanged(progress: Int) {
                progressBar.setProgress(progress)
                println(progress)
            }
        })
        task.start()
        this.task = task
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

        progressBar.setMax(100)
    }

}

//companion object {
//    fun createSharingGiveInstance(pack: Pack): SharingFragment<Intent> {
//        return SharingFragment<Intent>().also { it.task = }
//    }
//
//    fun createSharingTakeInstance(intent: Intent): SharingFragment<Pack> {
//        return SharingFragment<Pack>().also { it.task = }
//    }
//
//    private val manager: ISharingManager = WebSharingManager
//}
//class SharingFragment : Fragment() {
//
//    interface OnSharingIntentReadyListener {
//        fun onIntentReady(intent: Intent)
//    }
//
//    interface OnSharingPackExtractedListener {
//        fun onPackExtracted(pack: Pack)
//    }
//
//    private var sharingManager: ISharingManager? = null
//    private var currentPrepareTask: PrepareTask? = null
//    private var currentExtractTask: ExtractTask? = null
//
//    var onSharingIntentReadyListener: OnSharingIntentReadyListener? = null
//    var onSharingPackExtractedListener: OnSharingPackExtractedListener? = null
//
//    override fun onDestroy() {
//        currentPrepareTask?.cancel()
//        currentExtractTask?.cancel()
//        super.onDestroy()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        val appContext = activity?.applicationContext
//        sharingManager = WebSharingManager(cleanCacheOnCreate = true, context = appContext!!)
//        retainInstance = true
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_sharing, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }
//
//    fun startPrepareSharingIntent(pack: Pack) {
//        currentPrepareTask?.cancel()
//        currentPrepareTask = sharingManager?.makePrepareTask(pack)?.also{
//            it.setListener(object: ISharingListener<Intent> {
//                override fun onSuccessed(data: Intent) {
//                    onSharingIntentReadyListener?.onIntentReady(data)
//                }
//
//                override fun onCanceled() { println("canceled") }
//                override fun onFailed(error: String) { println(error) }
//                override fun onProgressChanged(progress: Int) { println(progress) }
//            })
//            it.start()
//        }
//    }
//
//    fun startExtractFromSharingIntent(intent: Intent) {
//        if(intent.data == null)
//            return
//        currentExtractTask?.cancel()
//        currentExtractTask = sharingManager?.makeExtractTask(intent)?.also {
//            it.setListener(object: ISharingListener<Pack> {
//                override fun onSuccessed(data: Pack) {
//                    onSharingPackExtractedListener?.onPackExtracted(data)
//                }
//
//                override fun onCanceled() { println("canceled") }
//                override fun onFailed(error: String) { println(error) }
//                override fun onProgressChanged(progress: Int) { println(progress) }
//            })
//            it.start()
//        }
//    }
//
//    companion object {
//        private val TAG = "SharingFragmentTag"
//
//        fun getInstance(parentViewId: Int, fragmentManager: FragmentManager): SharingFragment {
//            var fragment = fragmentManager.findFragmentByTag(TAG) as? SharingFragment
//            if (fragment == null) {
//                fragment = SharingFragment()
//                fragmentManager.beginTransaction()
//                    .add(parentViewId, fragment, TAG)
//                    .addToBackStack(null)
//                    .commit()
//            }
//            return fragment
//        }
//    }
//}

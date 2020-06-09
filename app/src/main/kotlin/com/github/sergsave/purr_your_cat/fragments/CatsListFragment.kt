package com.github.sergsave.purr_your_cat.fragments

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_cats_list.*
import com.github.sergsave.purr_your_cat.R
import com.github.sergsave.purr_your_cat.Singleton
import com.github.sergsave.purr_your_cat.adapters.CatsListAdapter
import com.github.sergsave.purr_your_cat.helpers.*
import com.github.sergsave.purr_your_cat.models.CatData
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class CatsListFragment : Fragment() {

    interface OnItemClickListener {
        fun onItemClick(cat: CatData?, view: View?, sharedElementTransitionName: String?)
    }

    private lateinit var catsListAdapter : CatsListAdapter
    private var catId : Int? = null
    private var onItemClickListener : OnItemClickListener? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    // TODO: DATABASE storage
    class UriAdapter : TypeAdapter<Uri?>() {

        override fun write(out: JsonWriter?, value: Uri?) {
            out?.value(value?.toString())
        }

        override fun read(`in`: JsonReader?): Uri? {
            return `in`?.nextString()?.let { Uri.parse(it) }
        }
    }

    override fun onPause() {
        super.onPause()

        val json = GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .toJson(catsListAdapter.getItems())

        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        preferences?.edit()?.let {
            it.putString(CATS_LIST_KEY, json)
            it.commit()
        }
    }

    private fun loadCatsFromSettings(): ArrayList<CatData>? {
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)

        val json = preferences?.getString(CATS_LIST_KEY, null)
        if(json == null)
            return null

        val catsType = object : TypeToken<ArrayList<CatData>>() {}.type

        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .fromJson<ArrayList<CatData>>(json, catsType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cats_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.actionBar?.apply {
            setTitle(getResources().getString(R.string.app_name))
        }

        val testUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.cat)
                    + '/' + getResources().getResourceTypeName(R.drawable.cat)
                    + '/' + getResources().getResourceEntryName(R.drawable.cat))

        val testCats = arrayListOf(
            CatData("Simka", testUri),
            CatData("Masik", testUri),
            CatData("Uta", testUri),
            CatData("Sherya", testUri),
            CatData("Sema", testUri),
            CatData("Philya", testUri),
            CatData("Ganya", testUri)
        )

        setupCatsList(loadCatsFromSettings() ?: testCats)

        fab.setOnClickListener {
            //TODO: Interface
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun setupCatsList(cats: ArrayList<CatData>?) {
        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val listener = object : CatsListAdapter.OnClickListener {
            override fun onClick(position: Int, sharedView: View, sharedElementTransitionName: String) {
                val data = catsListAdapter.getItems().get(position)
                catId = position
                onItemClickListener?.onItemClick(data, sharedView, sharedElementTransitionName)
            }
        }

        catsListAdapter = CatsListAdapter(listener)
        if(cats != null)
            catsListAdapter.addItems(cats)

        if(context == null)
            return

        val viewManager = AutoFitGridLayoutManager(requireContext(), columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        // For the shared element transition to work correctly when returning to this screen
        catsListAdapter.setHasStableIds(true)

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    companion object {
        private val CATS_LIST_KEY = "CatsList"
    }
}

package com.github.sergsave.purr_your_cat.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.ChangeImageTransform
import android.transition.TransitionSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.sergsave.purr_your_cat.R
import com.github.sergsave.purr_your_cat.fragments.CatsListFragment
import com.github.sergsave.purr_your_cat.fragments.CatFormFragment
import com.github.sergsave.purr_your_cat.fragments.PurringFragment
import com.github.sergsave.purr_your_cat.helpers.Constants
import com.github.sergsave.purr_your_cat.models.CatData
import com.github.sergsave.purr_your_cat.Singleton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private sealed class PageType(val fragmentTag: String) {
        private enum class Tag { LIST, CREATE, EDIT, PURRING }

        class List() : PageType(Tag.LIST.name)
        class Create() : PageType(Tag.CREATE.name)
        class Edit() : PageType(Tag.EDIT.name)
        class Purring(val sharedView: View? = null,
                      val sharedElementTransitionName : String? = null) : PageType(Tag.PURRING.name)

        companion object {
            // Return default constructed object
            fun makeFromTag(tag: String?) : PageType? {
                return when(tag) {
                    Tag.LIST.name -> List()
                    Tag.CREATE.name -> Create()
                    Tag.EDIT.name -> Edit()
                    Tag.PURRING.name -> Purring()
                    else -> null
                }
            }
        }
    }

    private var currentPage : PageType? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use common toolbar for pages to avoid transition blinking
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        
        if(savedInstanceState == null) {
            switchToPage(PageType.List())
        }
        else {
            val tag = savedInstanceState.getString(TAG_BUNDLE_KEY)
            PageType.makeFromTag(tag)?.let { switchToPage(it) }
        }
    }

    private fun makeReplaceTransaction(fragment: Fragment, tag: String) : FragmentTransaction?
    {
        if(supportFragmentManager.findFragmentByTag(tag) != null)
            return null

        return supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, tag)
    }

    private fun switchToPage(page: PageType) {
        val existingFragment = supportFragmentManager.findFragmentByTag(page.fragmentTag)

        val transaction : FragmentTransaction? = when(page) {
            is PageType.List -> {
                val fragment = existingFragment as CatsListFragment?
                    ?: CatsListFragment()

                initFragment(fragment)
                makeReplaceTransaction(fragment, page.fragmentTag)
            }
            // Use same fragment, but different tags
            is PageType.Create, is PageType.Edit -> {
                val fragment = existingFragment as CatFormFragment?
                    ?: CatFormFragment.newInstance(Singleton.catData)

                initFragment(fragment)
                makeReplaceTransaction(fragment, page.fragmentTag)
            }
            is PageType.Purring -> {
                val fragment = existingFragment as PurringFragment?
                    ?: PurringFragment.newInstance(page.sharedElementTransitionName)

                initFragment(fragment)

                val view = page.sharedView
                val name = page.sharedElementTransitionName

                supportFragmentManager
                    .beginTransaction()
//                    .setReorderingAllowed(true)
                    .addSharedElement(view!!, name!!)
                    .replace(R.id.container, fragment, page.fragmentTag)
                    .addToBackStack(null)
//                makeReplaceTransaction(fragment, page.fragmentTag)?.apply {
//                    addToBackStack(null)
//                    if(view != null && name != null)
//                        addSharedElement(view, name)
//                }
            }
        }

        transaction?.commit()
        currentPage = page
    }

    private fun initFragment(fragment: CatsListFragment) {
        val listener = object: CatsListFragment.OnItemClickListener {
            override fun onItemClick(cat: CatData?, view: View?, sharedElementTransitionName: String?) {
                Singleton.catData = cat
                switchToPage(PageType.Purring(view, sharedElementTransitionName))
            }
        }

        fragment.apply {
//            setSharedElementReturnTransition(ChangeBounds())
//            setExitTransition(null)
            setOnItemClickListener(listener)
        }
    }

    private fun initFragment(fragment: CatFormFragment) {
        fragment.apply {
            setOnApplyListener(object: CatFormFragment.OnApplyListener {
                override fun onApply() = switchToPage(PageType.Purring())
            })
        }
    }

    private fun initFragment(fragment: PurringFragment) {
        fragment.apply {

            val transition = TransitionSet().apply {
                ordering = TransitionSet.ORDERING_TOGETHER
                addTransition(ChangeClipBounds())
                addTransition(ChangeTransform())
                addTransition(ChangeBounds())
//                addTransition(ChangeImageTransform())
            }
            setSharedElementEnterTransition(transition)
            setSharedElementReturnTransition(transition)
//            setEnterTransition(null)
            setOnEditRequestedListener(object: PurringFragment.OnEditRequestedListener {
                override fun onEditRequested() = switchToPage(PageType.Edit())
            })
        }
    }

    override fun onBackPressed() {
        if (currentPage is PageType.Create) {
            switchToPage(PageType.Purring())
        }
        else
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TAG_BUNDLE_KEY, currentPage?.fragmentTag)
    }

    companion object {
        private val TAG_BUNDLE_KEY = "CurrentTag"
    }
}

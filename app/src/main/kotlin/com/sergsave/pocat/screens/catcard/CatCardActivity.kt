package com.sergsave.pocat.screens.catcard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.sergsave.pocat.Constants
import com.sergsave.pocat.R
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.helpers.EventObserver
import com.sergsave.pocat.helpers.ViewModelFactory
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_cat_card.*

class CatCardActivity : AppCompatActivity() {
    private val navigation: NavigationViewModel by viewModels {
        ViewModelFactory(NavigationViewModel::class.java, {
            NavigationViewModel(getCard(intent), getSharingIntent(intent) != null)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_card)

        // Use common toolbar for avoid transition blinking
        setToolbarAsActionBar(toolbar, true)
        setupNavigation()

        if(savedInstanceState == null && getTransitionName(intent) != null) {
            postponeEnterTransition()
            navigation.isSharedElementTransitionPostponed.value = true
        }
    }

    private fun setupNavigation() {
        val makeFade = { MaterialFadeThrough.create(this) }

        val openFormFragment = { card: Card? ->
            val fragment = FormFragment.newInstance(card).apply { enterTransition = makeFade() }
            setContentFragment(fragment)
        }

        navigation.apply {
            val lifecycleOwner = this@CatCardActivity

            editCatEvent.observe(lifecycleOwner, EventObserver {
                openFormFragment(it)
            })

            addNewCatEvent.observe(lifecycleOwner, EventObserver {
                openFormFragment(null)
            })

            openCatEvent.observe(lifecycleOwner, EventObserver {
                val fragment = PurringFragment.newInstance(it, getTransitionName(intent)).apply {
                    enterTransition = makeFade()
                }
                setContentFragment(fragment)
            })

            startExtractSharingDataEvent.observe(lifecycleOwner, EventObserver {
                val fragment = SharingDataExtractFragment.newInstance(getSharingIntent(intent))
                setContentFragment(fragment)
            })

            finishEvent.observe(lifecycleOwner, EventObserver {
                super.onBackPressed()
            })

            showTutorialEvent.observe(lifecycleOwner, EventObserver {
                if (supportFragmentManager.backStackEntryCount != 0)
                    return@EventObserver

                val fragment = PurringTutorialFragment().apply {
                    enterTransition = makeFade().apply { duration = 750 }
                }
                supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()
            })

            needHideTutorialEvent.observe(lifecycleOwner, EventObserver {
                supportFragmentManager.popBackStack()
            })
        }
    }

    private fun getCard(intent: Intent) =
        intent.getParcelableExtra<Card>(Constants.CARD_INTENT_KEY)

    private fun getSharingIntent(intent: Intent) =
        intent.getParcelableExtra<Intent>(Constants.SHARING_INPUT_INTENT_KEY)

    private fun getTransitionName(intent: Intent) =
        intent.getStringExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY)

    private fun setContentFragment(fragment: Fragment, tag: String? = null) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
    }

    override fun onBackPressed() {
        // Delegate routing
        navigation.onBackButtonPressed()
    }
}

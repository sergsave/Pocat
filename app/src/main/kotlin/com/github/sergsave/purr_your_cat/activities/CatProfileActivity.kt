package com.github.sergsave.purr_your_cat

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_cat_profile.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar


class CatProfileActivity : AppCompatActivity() {

    enum class Mode {
        CREATE, EDIT;

        fun attachTo(intent: Intent) {
            intent.putExtra(KEY, ordinal)
        }

        companion object {
            private val KEY = "CatProfileActivityMode"
            private val values = values()

            fun detachFrom(intent: Intent) : Mode? {
                if(!intent.hasExtra(KEY))
                    return null

                val value = intent.getIntExtra(KEY, -1)
                return values.firstOrNull { it.ordinal == value}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_profile)

        val mode = Mode.detachFrom(getIntent())
        val toolbarTitle = when(mode) {
            Mode.CREATE -> getResources().getString(R.string.add_new_cat)
            Mode.EDIT -> getResources().getString(R.string.edit_cat)
            else -> ""
        }

        setSupportActionBar(toolbar)
        val actionBar = getSupportActionBar()

        actionBar?.title = toolbarTitle
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener{ finish() }

        name_edit_text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        name_edit_text.setOnEditorActionListener( { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocus(v)
            }
            false
        })
        
        pick_sound_edit_text.setOnClickListener( {
            println("Pick a sound")
        })

        applyButton.setOnClickListener ( {
            val intent = Intent(this, PurringActivity::class.java)
            intent.putExtra("cat_name", "cat name")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        })
    }

    private fun clearFocus(v: View) {
        v.clearFocus()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    clearFocus(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}

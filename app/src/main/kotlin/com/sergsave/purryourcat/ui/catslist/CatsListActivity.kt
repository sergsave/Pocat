package com.sergsave.purryourcat.ui.catslist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import com.sergsave.purryourcat.ui.catcard.CatCardActivity

import kotlinx.android.synthetic.main.activity_cats_list.*

// TODO: Check sdk version of all function
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: Code inspect and warnings
// TODO: Hangs on Xiaomi Redmi 6
// TODO: Require context and requireActivity

// БАГИ после рефакторинга

// Краш в трансформе
// Пустой список котов
// Нет опций меню
// Селекшн при перевороте
// Не открывалась карточка кота по апплай
// Пустой текст не считался за изменение
// Щас не воспроизводится, но фотка в форме исчезала при перевороте
// Звук иногда не добавляется, мурки не работают
// При перевороте экрана появляется сообщение, что данные есть несохраненные
// Переинит аудио не оч хорошо
// Меню главного экрана исчезает после переворота
// Шаринга иконка
// Перепутал валидацию данных
// Заголовок при загрузке кота
// Не открывался кот на редактирование после сохранения дискеткой
// После сворачивания приложения не работали мурки иногда
// Можно открыть много карточек при быстром нажатии

// Краш и зависание при глажке кота
// переделать репу контента
// мб инитить аудио на касание

class CatsListActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cats_list)

        if(savedInstanceState != null)
            return

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, CatsListFragment())
            .commit()

        checkInputSharingIntent()
    }

    private fun checkInputSharingIntent() {
        val isForwarded = intent?.getBooleanExtra(Constants.IS_FORWARDED_INTENT_KEY, false) ?: false
        if(isForwarded.not())
            return

        // Forward further
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, this.intent)

        startActivity(intent)
    }
}
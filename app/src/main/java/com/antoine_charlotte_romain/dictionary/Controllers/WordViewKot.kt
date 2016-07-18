package com.antoine_charlotte_romain.dictionary.Controllers

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.TextView
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import org.jetbrains.anko.ctx

/**
 * Created by dineen on 11/07/2016.
 */
class WordViewKot : AppCompatActivity() {

    var word : Word? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.view_word)

        //Set the toolbar on the view
        var toolbar = super.findViewById(R.id.tool_bar) as Toolbar
        super.setSupportActionBar(toolbar)
        this.supportActionBar!!.setTitle(R.string.details)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Set the word and dictionary, come from the segue
        this.word = this.intent.getSerializableExtra(MainActivityKot.EXTRA_WORD) as Word
        var wordDB = WordSQLITE(this.applicationContext, (this.word as Word).idWord, (this.word as Word).note,
                (this.word as Word).image, (this.word as Word).sound, (this.word as Word).headword, (this.word as Word).dateView,
                (this.word as Word).idDictionary)
        var dictionary = this.intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as Dictionary

        //Set fields
        (super.findViewById(R.id.edit_dictionary) as TextView).text = dictionary.getNameDictionary()
        (super.findViewById(R.id.edit_word) as TextView).text = this.word!!.headword
        if (this.word!!.note == null) {
            (super.findViewById(R.id.edit_note) as TextView).text = this.resources.getString(R.string.no_note)
        }
        else {
            (super.findViewById(R.id.edit_note) as TextView).text = this.word!!.headword
        }
        var translations = wordDB.selectAllTranslations()
        var translationField = super.findViewById(R.id.edit_translation) as TextView
        if (translations.count() > 0) {
            var strTranslations = ""
            for (tr in translations) {
                strTranslations = strTranslations.plus("- " + tr.headword + "\n")
            }
            println(strTranslations)
            translationField.text = strTranslations
        }
        if (this.word!!.image != null) {
            var img = BitmapFactory.decodeByteArray(this.word!!.image, 0, this.word!!.image!!.size)
            (super.findViewById(R.id.image_word) as ImageView).setImageBitmap(img)
        }
        //setupUI(findViewById(R.id.word_layout)!!)
    }

    /**
     * This function is called when a child activity back to this view or finish
     */
    public override fun onResume() {
        super.onResume()
    }

}

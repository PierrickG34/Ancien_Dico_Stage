package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import java.util.*

/**
 * Created by dineen on 25/07/2016.
 */

class AdvancedSearchResultsAdapterKot (context: Context, private val layoutResourceId: Int, data: MutableList<Word>) : ArrayAdapter<Word>(context, layoutResourceId, data) {

 /**
 * This function creates a custom ArrayAdapter of words
 * @param context the context of the application
 * @param resource the layout to inflate
 * @param data the ArrayList of words
 */


   private var aw: MutableList<Word>? = null

    init {
        this.aw = data
    }

    override fun getCount(): Int {
        return aw!!.size
    }

    override fun getItem(position: Int): Word {
        return aw!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * This function is used to show the word in the listView each word in a custom layout
     * @param position the position of the item the user is interacting with
     * @param convertView the rowView
     * @param parent the listView
     * @return the rowView completed
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        // Get the data item for this position
        val word = getItem(position)
        val wordModel = WordSQLITE(this.context, word.idWord, word.note, word.image, word.sound, word.headword, word.dateView, word.idDictionary)


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false)
        }

        // Lookup view for data population
        val mainItem = convertView!!.findViewById(R.id.textHeader) as TextView
        val subItem = convertView.findViewById(R.id.textSub) as TextView

        // Populate the data into the template view using the data object
        mainItem.setText(word.headword)
        subItem.setText(wordModel.getAllTranslationText())

       // subItem.setText(word.getTranslation())
       // Exemple de WordAdapterKot.kt
//        if (this.selectedDictionary) {
//            subItem.setText(wordModel.getAllTranslationText())
//        }
//        else {
//            subItem.setText(wordModel.headword)
//        }


        // Return the completed view to render on screen
        return convertView
    }
}


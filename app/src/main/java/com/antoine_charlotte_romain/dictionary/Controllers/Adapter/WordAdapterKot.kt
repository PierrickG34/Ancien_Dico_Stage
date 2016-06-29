package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import java.util.*

/**
 * Created by dineen on 23/06/2016.
 */
class WordAdapterKot(context: Context, resource: Int, data: ArrayList<Word>, selectedDictionary: Boolean): ArrayAdapter<Word>(context, resource, data) {

    var ctx = context
    var layoutResourceId = resource
    var listWord = data
    var listDelete = ArrayList<Word>()
    var selectedDictionary = selectedDictionary
    var allSelected: Boolean = false
    var callback: WordAdapterCallbackKot? = null

    override fun getCount(): Int {
        return this.listWord.size
    }

    override fun getItem(position: Int): Word {
        return this.listWord.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getDeleteList(): ArrayList<Word> {
        return this.listDelete
    }

    fun isAllSelected(): Boolean {
        return this.allSelected
    }

    /**
     * This function is used to show the word in the listView each word in a custom layout
     * @param position the position of the item the user is interacting with
     * *
     * @param convertView the rowView
     * *
     * @param parent the listView
     * *
     * @return the rowView completed
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        // Get the data item for this position
        val word = getItem(position)
        val wordModel = WordSQLITE(this.ctx, word.idWord, word.note, word.image, word.sound, word.headword, word.dateView, word.idDictionary)

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(this.layoutResourceId, parent, false)
        }

        // Lookup view for data population
        val mainItem = convertView!!.findViewById(R.id.textHeader) as TextView
        val subItem = convertView.findViewById(R.id.textSub) as TextView

        // Populate the data into the template view using the data object
        mainItem.setText(word.headword)
        if (this.selectedDictionary) {
            subItem.setText(wordModel.getAllTranslationText())
        }
        else {
            subItem.setText(wordModel.headword)
        }

        if (this.layoutResourceId == R.layout.row_word) {
            val menuButton = convertView.findViewById(R.id.imageButtonWord) as ImageButton
            menuButton.setColorFilter(R.color.textColor, PorterDuff.Mode.MULTIPLY)
            menuButton.setOnClickListener { v ->
                if (!callback!!.getOpen()) {
                    when (v.id) {
                        R.id.imageButtonWord -> {
                            val popup = PopupMenu(context, v)
                            popup.menuInflater.inflate(R.menu.context_menu_word, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.modify -> callback!!.modifyPressed(position)
                                    R.id.delete -> callback!!.deletePressed(position)
                                }
                                true
                            }
                        }
                    }
                }
                else {
                    callback!!.showFloatingMenu(v)
                }
            }
        }
        else if (layoutResourceId == R.layout.row_delete_word) {
            val checkBox = convertView.findViewById(R.id.deleteWordBox) as CheckBox

            checkBox.isChecked = this.listDelete.contains(word)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    addToDeleteList(word)
                }
                else {
                    removeFromDeleteList(word)
                }
            }
            convertView.setOnClickListener(View.OnClickListener {
                if (checkBox.isChecked) {
                    checkBox.isChecked = false
                    removeFromDeleteList(word)
                } else {
                    checkBox.isChecked = true
                    addToDeleteList(word)
                }
            })
        }

        // Return the completed view to render on screen
        return convertView
    }

    private fun addToDeleteList(w: Word) {
        if (!this.listDelete.contains(w)) {
            this.listDelete.add(w)
            this.callback!!.notifyDeleteListChanged()
        }
    }

    private fun removeFromDeleteList(w: Word) {
        this.listDelete.remove(w)
        this.allSelected = false
        this.callback!!.notifyDeleteListChanged()
    }
}
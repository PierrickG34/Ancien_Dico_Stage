package ie.csis.dicosaure.views.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import ie.csis.dicosaure.model.dictionary.Dictionary
import ie.csis.dicosaure.views.R
import java.util.*

/**
 * Created by dineen on 20/06/2016.
 */
class DictionaryAdapter(ctx : Context, layoutRessourceId : Int, data : ArrayList<Dictionary>) : ArrayAdapter<Dictionary>(ctx, layoutRessourceId, data) {

    var ctx : Context = ctx
    var layoutRessourceId : Int = layoutRessourceId
    var data : ArrayList<Dictionary> = data
    var deleteList : ArrayList<Dictionary> = ArrayList<Dictionary>()
    var dictionaryCallback : DictionaryAdapterCallback? = null
    var all_selected : Boolean = false

    /**
     * Set the dictionary callback
     * @param callback the dictionary callback to be set
     */
    fun setCallback(callback: DictionaryAdapterCallback) {
        this.dictionaryCallback = callback
    }

    /**
     * Get the dictionary view
     * @param position the position of the dictionary you want to display
     * @param convertView the initial view
     * @param parent the parent view of convertView
     * @return the created view
     */
    override fun getView(position : Int, convertView : View?, parent : ViewGroup) : View {
        var convertView = convertView
        var dictionary = super.getItem(position) //get item

        if (convertView == null) {
            convertView = LayoutInflater.from(super.getContext()).inflate(this.layoutRessourceId, parent, false);
        }

        var titleCell = convertView!!.findViewById(R.id.dictionary_title) as TextView
        titleCell.setText(dictionary.getNameDictionary())

        //More information in the cell
        if (this.layoutRessourceId == R.layout.dictionary_row) {
            val menuButton = convertView.findViewById(R.id.dico_more_button) as ImageButton
            menuButton.setColorFilter(R.color.textColor, PorterDuff.Mode.MULTIPLY)
            convertView.setOnClickListener {
                this.dictionaryCallback!!.read(position)
            }
            menuButton.setOnClickListener { v ->
                when (v.id) {
                    R.id.dico_more_button -> {
                        val popup = PopupMenu(context, v)
                        popup.menuInflater.inflate(R.menu.context_menu_dictionary, popup.menu)
                        popup.show()
                        popup.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.open -> this.dictionaryCallback!!.read(position)
                                R.id.rename -> this.dictionaryCallback!!.update(position)
                                R.id.delete -> this.dictionaryCallback!!.delete(position)
                                R.id.export -> this.dictionaryCallback!!.export(position)
                            }
                            true
                        }
                    }
                }
            }
        }
        //More information button (top the screen) -> delete
        else if (this.layoutRessourceId == R.layout.delete_dictionary_row) {
            val checkBox = convertView.findViewById(R.id.delete_box) as CheckBox

            checkBox.isChecked = deleteList.contains(dictionary)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    addToDeleteList(dictionary)
                }
                else {
                    removeFromDeleteList(dictionary)
                }
            }
            convertView.setOnClickListener {
                if (checkBox.isChecked) {
                    checkBox.isChecked = false
                    removeFromDeleteList(dictionary)
                }
                else {
                    checkBox.isChecked = true
                    addToDeleteList(dictionary)
                }
            }
        }

        return convertView

    }

    /**
     * Add a dictionary to the delete list
     * @param d the dictionary to be aded
     */
    private fun addToDeleteList(d: Dictionary) {
        if (!this.deleteList.contains(d)) {
            this.deleteList.add(d)
            this.dictionaryCallback!!.notifyDeleteListChanged()
        }
    }

    /**
     * Remove a dictionary from the delete list
     * @param d the dictionary to be removed
     */
    private fun removeFromDeleteList(d: Dictionary) {
        this.deleteList.remove(d)
        this.all_selected = false
        this.dictionaryCallback!!.notifyDeleteListChanged()
    }

    /**
     * Select all the dictionaries and add them to the delete list
     */
    fun selectAll() {
        this.all_selected = !this.all_selected
        if (this.all_selected) {
            for (i in this.data.indices) {
                this.addToDeleteList(this.data[i])
            }
        }
        else {
            this.deleteList.clear()
            this.dictionaryCallback!!.notifyDeleteListChanged()
        }
        super.notifyDataSetChanged()
    }

}
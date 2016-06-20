package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import java.util.*

/**
 * Created by dineen on 20/06/2016.
 */
class DictionaryAdapterKot(ctx : Context, layoutRessourceId : Int, data : ArrayList<Dictionary>) : ArrayAdapter<Dictionary>(ctx, layoutRessourceId, data) {

    var ctx : Context = ctx
    var layoutRessourceId : Int = layoutRessourceId
    var data : ArrayList<Dictionary>  = data
    var deleteList : ArrayList<Dictionary> = ArrayList<Dictionary>()
    var dictionaryCallback : DictionaryAdapterCallbackKot? = null
    var all_selected : Boolean = false

    fun setCallback(callback: DictionaryAdapterCallbackKot) {
        this.dictionaryCallback = callback
    }

    override fun getView(position : Int, convertView : View, parent : ViewGroup) : View {
        var convertView = convertView
        var dictionary = super.getItem(position) //get item

        if (convertView == null) {
            convertView = LayoutInflater.from(super.getContext()).inflate(this.layoutRessourceId, parent, false);
        }

        var titleCell = convertView.findViewById(R.id.dictionary_title) as TextView
        titleCell.setText("""${dictionary.inLang} -> ${dictionary.outLang}""")

        //More information in the cell
        if (layoutRessourceId == R.layout.dictionary_row) {
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

        return convertView

    }

    private fun addToDeleteList(d: Dictionary) {
        if (!this.deleteList.contains(d)) {
            this.deleteList.add(d)
            this.dictionaryCallback!!.notifyDeleteListChanged()
        }
    }

    private fun removeFromDeleteList(d: Dictionary) {
        deleteList.remove(d)
        all_selected = false
        this.dictionaryCallback!!.notifyDeleteListChanged()
    }

    fun selectAll() {
        all_selected = !all_selected
        if (all_selected) {
            for (i in data.indices) {
                addToDeleteList(data[i])
            }
        } else {
            deleteList.clear()
            this.dictionaryCallback!!.notifyDeleteListChanged()
        }
        notifyDataSetChanged()
    }

}
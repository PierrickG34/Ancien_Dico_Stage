package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.antoine_charlotte_romain.dictionary.R

/**
 * Created by dineen on 16/06/2016.
 */
class MenuDrawerAdapterKot(titles : Array<String>, icons : IntArray): RecyclerView.Adapter<MenuDrawerAdapterKot.MenuDrawerViewHolder>() {

    val titles: Array<String> = titles //Array of titles
    val icons: IntArray = icons //Array of icons

    companion object {
        val TYPE_HEADER = 0 //The header cell
        val TYPE_ITEM = 1 //The item cell
    }

    //Class cell
    class MenuDrawerViewHolder : RecyclerView.ViewHolder {

        var holderid: Int = 0 // 0 to a header cell and 1 to a item cell
        var title: TextView? = null //Title of the table
        var textView: TextView? = null //Title of the cell
        var imageHeader: ImageView? = null //Image of the header cell
        var imageView: ImageView? = null //Image of the cell

        constructor(itemView : View, viewType : Int) : super(itemView) {
            if (viewType == MenuDrawerAdapterKot.TYPE_ITEM) { //normal cell
                this.textView = itemView.findViewById(R.id.textViewDrawer) as TextView
                this.imageView = itemView.findViewById(R.id.imageViewDrawer) as ImageView
                this.holderid = 1
            } else { //header cell
                this.title = itemView.findViewById(R.id.textViewHeader) as TextView
                this.imageHeader = itemView.findViewById(R.id.imageViewHeader) as ImageView
                this.holderid = 0
            }
        }

    }

    //bind value on the cells
    override fun onBindViewHolder(holder: MenuDrawerViewHolder, position: Int) {
        if (holder.holderid == 1) {
            holder.textView!!.setText(this.titles[position - 1])
            holder.imageView!!.setImageResource(this.icons[position - 1])
        } else {
            holder.imageHeader!!.setImageResource(R.drawable.ic_settings_white_36dp)
            holder.title!!.setText(R.string.action_settings)
        }
    }

    override fun getItemCount(): Int {
        return this.titles.count() + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MenuDrawerViewHolder {
        if (viewType == TYPE_ITEM) {
            val v = LayoutInflater
                    .from(parent!!.getContext())
                    .inflate(R.layout.drawer_row, parent, false)
            val vhItem = MenuDrawerViewHolder(v, viewType)
            return vhItem
        }
        else {
            val v = LayoutInflater
                    .from(parent!!.getContext())
                    .inflate(R.layout.drawer_header, parent, false)
            val vhHeader = MenuDrawerViewHolder(v, viewType)
            return vhHeader
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        }
        return TYPE_ITEM
    }

}
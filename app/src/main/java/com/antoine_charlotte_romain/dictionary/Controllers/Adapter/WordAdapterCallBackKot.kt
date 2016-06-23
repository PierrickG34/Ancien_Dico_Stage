package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

import android.view.View

/**
 * Created by dineen on 23/06/2016.
 */
interface WordAdapterCallback {
    fun deletePressed(position: Int)
    fun modifyPressed(position: Int)
    val open: Boolean
    fun showFloatingMenu(v: View)
    fun notifyDeleteListChanged()
}

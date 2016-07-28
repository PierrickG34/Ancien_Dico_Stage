package ie.csis.dicosaure.views.adapters

import android.view.View

/**
 * Created by dineen on 23/06/2016.
 */
interface WordAdapterCallbackKot {
    fun deletePressed(position: Int)
    fun modifyPressed(position: Int)
    fun getOpen(): Boolean
    fun showFloatingMenu(v: View)
    fun notifyDeleteListChanged()
}

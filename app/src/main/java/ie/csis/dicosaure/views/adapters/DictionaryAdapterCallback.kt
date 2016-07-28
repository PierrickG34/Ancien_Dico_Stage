package ie.csis.dicosaure.views.adapters

/**
 * Created by dineen on 20/06/2016.
 */
interface DictionaryAdapterCallback {
    fun delete(position: Int)
    fun update(position: Int)
    fun read(position: Int)
    fun export(position: Int)
    fun notifyDeleteListChanged()
}
package com.antoine_charlotte_romain.dictionary.Controllers.Adapter

/**
 * Created by dineen on 20/06/2016.
 */
interface DictionaryAdapterCallbackKot {
    fun delete(position: Int)
    fun update(position: Int)
    fun read(position: Int)
    fun export(position: Int)
    fun notifyDeleteListChanged()
}
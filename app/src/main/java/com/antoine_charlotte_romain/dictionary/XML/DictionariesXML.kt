package com.dicosaure.XML

import com.dicosaure.XML.DictionaryXML
import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

/**
 * Created by dineen on 09/06/2016.
 */
@XRoot(name = "dictionaries")
abstract class DictionariesXML {
    var dictionaries by JXML / XAnyElements / XSub(DictionaryXML::class.java)

    init {
        this.dictionaries = (this.dictionaries?: listOf())
    }

    abstract fun save()
    abstract fun addDictionary()
    abstract fun removeDictionary()

    fun addDictionnary(dictionary : DictionaryXML) {
        this.dictionaries = (this.dictionaries?: listOf()) + dictionary
    }

    fun removeDictionary(dictionary : DictionaryXML) {
        if ((this.dictionaries?: listOf()).contains(dictionary)) {
            this.dictionaries = (this.dictionaries?: listOf()) - dictionary
        }
    }
}

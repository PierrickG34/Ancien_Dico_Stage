package com.dicosaure.XML

import com.dicosaure.XML.TranslationXML
import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XName
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

/**
 * Created by dineen on 09/06/2016.
 */
class WordXML {
    var tagName by JXML / XName
    var headword by JXML / "headword" / XText
    var translations by JXML / "translations" / XAnyElements / XSub(TranslationXML::class.java)
    var note by JXML / "note" / XText

    init {
        this.translations = (this.translations?: listOf())
        this.tagName = "word"
    }

    constructor(headword: String, note: String) {
        this.headword = headword
        this.note = note
    }

    fun addTranslation(translation : TranslationXML) {
        this.translations = (this.translations?: listOf()) + translation
    }

    fun removeTranslation(translation : TranslationXML) {
        if ((this.translations?: listOf()).contains(translation)) {
            this.translations = (this.translations?: listOf()) - translation
        }
    }
}
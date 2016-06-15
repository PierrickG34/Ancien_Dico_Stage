package com.dicosaure.XML

import com.dicosaure.XML.WordXML
import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XName
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

/**
 * Created by dineen on 09/06/2016.
 */
class DictionaryXML {
    var tagName by JXML / XName
    var words by JXML / XAnyElements / XSub(WordXML::class.java)
    var inLang by JXML[1] / XAttribute("inLang")
    var outLang by JXML[2] / XAttribute("outLang")

    init {
        this.words = (this.words?: listOf())
        this.tagName = "dictionary"
    }

    constructor(inLang : String, outLang : String) {
        this.outLang = outLang
        this.inLang = inLang
    }

    fun addWord(word : WordXML) {
        this.words = (this.words?: listOf()) + word
    }

    fun removeWord(word : WordXML) {
        if ((this.words?: listOf()).contains(word)) {
            this.words = (this.words?: listOf()) - word
        }
    }
}

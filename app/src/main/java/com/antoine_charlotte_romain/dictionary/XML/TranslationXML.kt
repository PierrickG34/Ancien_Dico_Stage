package com.dicosaure.XML

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

/**
 * Created by dineen on 09/06/2016.
 */
class TranslationXML {
    var tagName by JXML / XName
    var word by JXML / XText

    init {
        this.tagName = "translation"
    }

    constructor(translation: String) {
        this.word = translation
    }
}

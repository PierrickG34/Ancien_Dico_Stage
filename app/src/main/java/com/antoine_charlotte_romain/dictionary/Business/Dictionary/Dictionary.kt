package com.dicosaure.Business.Dictionary

import com.dicosaure.XML.WordXML
import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XName
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

/**
 * Created by dineen on 14/06/2016.
 */
abstract class Dictionary {

    var inLang : String
    var outLang : String
    var idDictionary : String?

    constructor(inLang : String, outLang: String, idDictionary: String? = null) {
        this.inLang = inLang
        this.outLang = outLang
        this.idDictionary = idDictionary
    }

    abstract fun save()
    abstract fun delete()
    abstract fun read()

}
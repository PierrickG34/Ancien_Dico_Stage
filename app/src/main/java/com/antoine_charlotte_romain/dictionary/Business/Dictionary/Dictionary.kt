package com.antoine_charlotte_romain.dictionary.business.dictionary

/**
 * Created by dineen on 14/06/2016.
 */
open class Dictionary(inLang : String? = null, outLang: String? = null, idDictionary: String? = null) {

    var inLang : String? = inLang
    var outLang : String? = outLang
    var idDictionary : String? = idDictionary

    fun getNameDictionary() : String {
        return """${this.inLang} -> ${this.outLang}"""
    }

}
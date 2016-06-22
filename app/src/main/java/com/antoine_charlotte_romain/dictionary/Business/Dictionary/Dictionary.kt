package com.antoine_charlotte_romain.dictionary.business.dictionary

import java.io.Serializable

/**
 * Created by dineen on 14/06/2016.
 */
open class Dictionary(inLang : String? = null, outLang: String? = null, id: String? = null) : Serializable {

    var inLang : String? = inLang
    var outLang : String? = outLang
    var idDictionary : String? = id

    fun getNameDictionary() : String {
        return """${this.inLang} -> ${this.outLang}"""
    }

    override fun toString(): String {
        return """id => ${this.idDictionary}, inLang => ${this.inLang}, outLang => ${this.outLang}"""
    }

}
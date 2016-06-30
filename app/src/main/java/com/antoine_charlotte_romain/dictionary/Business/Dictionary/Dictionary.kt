package com.antoine_charlotte_romain.dictionary.business.dictionary

import java.io.Serializable

/**
 * Created by dineen on 14/06/2016.
 */
open class Dictionary : Serializable {

    var inLang : String? = null
    var outLang : String? = null
    var idDictionary : String? = null

    constructor(inLang : String? = null, outLang: String? = null, id: String? = null) {
        if (inLang != null && outLang != null) {
            this.inLang = inLang.toUpperCase()
            this.outLang = outLang.toUpperCase()
        }
        this.idDictionary = id
    }

    fun getNameDictionary() : String {
        return """${this.inLang} -> ${this.outLang}""".toUpperCase()
    }

    override fun toString(): String {
        return """id => ${this.idDictionary}, inLang => ${this.inLang}, outLang => ${this.outLang}"""
    }

}
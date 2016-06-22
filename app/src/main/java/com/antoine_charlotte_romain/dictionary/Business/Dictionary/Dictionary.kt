package com.antoine_charlotte_romain.dictionary.business.dictionary

import java.io.Serializable

/**
 * Created by dineen on 14/06/2016.
 */
open class Dictionary : Serializable {

    var inLang : String? = null
    var outLang : String? = null
    var idDictionary : String? = null

    constructor(inLang : String?, outLang: String?, id: String?) {
        if (inLang != null && outLang != null) {
            this.inLang = inLang.toUpperCase()
            this.outLang = outLang.toUpperCase()
        }
        this.idDictionary = id
    }

    fun getNameDictionary() : String {
        return """${this.inLang} -> ${this.outLang}"""
    }

    override fun toString(): String {
        return """id => ${this.idDictionary}, inLang => ${this.inLang}, outLang => ${this.outLang}"""
    }

}
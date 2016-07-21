package com.antoine_charlotte_romain.dictionary.business.dictionary

import java.io.Serializable

/**
 * Eng : This class is a Dictionary.
 * Fr : Cette classe est un dictionnaire
 *  * Created by dineen on 14/06/2016.
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

    /**
     * Return the name of the dictionary under the form "InLang -> OutLang"
     * Retourne le nom du dictionnaire sous la forme "langueDEntree -> langueDeSortie"
     * @return String : the name of the dictionary
     */
    fun getNameDictionary() : String {
        return """${this.inLang} - ${this.outLang}""".toUpperCase()
    }

    /**
     * return the ID, the inLang and outLang of the dictionary as a String
     * Retourne l'id, la langue d'entree et de sortie sous forme de String
     * @return String : return the ID, the inLang and outLang of the dictionary
     */
    override fun toString(): String {
        return """id => ${this.idDictionary}, inLang => ${this.inLang}, outLang => ${this.outLang}"""
    }

}
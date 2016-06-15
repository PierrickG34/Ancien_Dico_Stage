package com.dicosaure.Business.Word

import java.sql.Blob
import java.sql.Date
import java.util.*
import com.dicosaure.Business.Dictionary.Dictionary

/**
 * Created by dineen on 15/06/2016.
 */
abstract class Word {

    val idWord : String?
    var note : String?
    var image : Blob?
    var sound : Blob?
    var headword : String
    var dateView : Date?
    var dictionary : Dictionary

    constructor(idWord: String? = null, note : String? = null, image : Blob? = null, sound : Blob? = null, headword
     : String, dateView: Date? = null, dictionary: Dictionary) {
        this.idWord = idWord
        this.note = note
        this.image = image
        this.sound = sound
        this.headword = headword
        this.dateView = dateView
        this.dictionary = dictionary
    }

    abstract fun save()
    abstract fun delete()
    abstract fun modify()
}

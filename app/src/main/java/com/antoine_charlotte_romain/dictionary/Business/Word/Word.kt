package  com.antoine_charlotte_romain.dictionary.business.word

import java.sql.Blob
import java.sql.Date

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
    var dictionary : com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary

    constructor(idWord: String? = null, note : String? = null, image : Blob? = null, sound : Blob? = null, headword
     : String, dateView: Date? = null, dictionary: com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary) {
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

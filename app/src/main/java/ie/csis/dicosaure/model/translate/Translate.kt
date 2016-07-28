package ie.csis.dicosaure.model.translate

import ie.csis.dicosaure.model.word.Word

/**
 * Eng : This class is a translation. This is the link between a word and his translation
 * Fr : Cette classe est une traduction. Il s'agit du lien entre le mot et sa traduction.
 * Created by dineen on 15/06/2016.
 */
open class Translate(wordInLang: Word?, wordOutLang: Word?) {
    //
    var wordInLang = wordInLang
    var wordOutLang = wordOutLang

    override fun equals(other: Any?): Boolean {
        val t = other as Translate
        return this.wordInLang!!.equals(t.wordInLang) && this.wordOutLang!!.equals(t.wordOutLang)
    }
}

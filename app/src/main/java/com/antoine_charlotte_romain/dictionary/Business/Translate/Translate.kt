package com.dicosaure.Business.Translate

import com.antoine_charlotte_romain.dictionary.business.word.Word

/**
 * Created by dineen on 15/06/2016.
 */
open class Translate {

    var wordTo : Word
    var wordFrom : Word

    constructor(wordTo: Word, wordFrom: Word) {
        this.wordFrom = wordFrom
        this.wordTo = wordTo
    }


}

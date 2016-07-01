package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.dicosaure.Business.Translate.TranslateSQLITE
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Created by dineen on 30/06/2016.
 */
class ImportCSVKot {

    var wdm: WordSQLITE? = null
    var updatedWords = ArrayList<String>()
    var addedWords: Int = 0
    var dicoID: String? = null
    var br: BufferedReader? = null
    var line: String? = null
    var progress: ProgressDialog? = null

    /**
     * Open a CSV file and add its word to a dictionary

     * @param d The dictionary where the words will be added
     * *
     * @param uri The CSV file providing the words to add in the dictionary
     * *
     * @param context Activity which called the method
     */
    fun importCSV(d: DictionarySQLITE, uri: Uri, context: Context, handler: Handler) {
        this.wdm = WordSQLITE(context)
        this.dicoID = d.idDictionary

        //Initialising the progressBar
        this.progress = ProgressDialog(context)
        this.progress!!.setMessage(context.getString(R.string.import_progress))
        this.progress!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        this.progress!!.progress = 0
        this.progress!!.setCancelable(false)
        this.progress!!.show()


        val t = object : Thread() {
            override fun run() {
                // Each line of the CSV file will by split by the comma character
                val cvsSplitBy = ","
                try {
                    //InputStream is = getContentResolver().openInputStream(uri);
                    //InputStream is = new BufferedInputStream(new FileInputStream(fileToRead));
                    var input = context.contentResolver.openInputStream(uri)
                    // ISO-8859-1 interprets accents correctly
                    br = BufferedReader(InputStreamReader(input!!, "ISO-8859-1"))
                    var nbLine = 0
                    while (br!!.readLine() != null) {
                        nbLine++
                    }
                    progress!!.max = nbLine

                    //InputStream iss = new BufferedInputStream(new FileInputStream(fileToRead));
                    input = context.contentResolver.openInputStream(uri)
                    // ISO-8859-1 interprets accents correctly
                    br = BufferedReader(InputStreamReader(input!!, "ISO-8859-1"))

                    var wordInfo: Array<String>
                    var note: String
                    var translation: String
                    var wTo: WordSQLITE
                    var wFrom: WordSQLITE
                    var databaseWord: List<Word>
                    var meanings: String
                    var dbNotes: String
                    var headword : String
                    var access = br!!.readLine()

                    while (access != null) {
                        // Split the line with comma as a separator
                        wordInfo = access!!.split(cvsSplitBy.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        if (wordInfo.size == 3 && extractWord(wordInfo[0]).length > 0) {
                            headword = extractWord(wordInfo[0])
                            translation = extractWord(wordInfo[1])
                            note = extractWord(wordInfo[2])
                            wTo = WordSQLITE(ctx = context, headword = headword, note = note, idDictionary = dicoID)
                            if (wTo.save() < 0) {
                                wTo.readByHeadWord()
                            }
                            if (translation.length > 0) {
                                wFrom = WordSQLITE(ctx = context, headword = translation, note = "", idDictionary = "0")
                                if (wFrom.save() < 0) {
                                    wFrom.readByHeadWord()
                                }
                                var dtm = TranslateSQLITE(context, wTo, wFrom)
                                dtm.save()
                            }
                        }
//                        // Add the word in the database
//                        if (w.getHeadword() != "") {
//                            val result = wdm!!.insert(w)
//
//                            // if the headword of the word we try to insert already exists in this dictionary
//                            if (result == 1) {
//                                // Get the already existing word
//                                databaseWord = wdm!!.select(w.getHeadword(), dicoID!!)
//                                if (databaseWord.size == 1) {
//                                    // Get its translation and its note
//                                    meanings = databaseWord[0].getTranslation()
//                                    dbNotes = databaseWord[0].getNote()
//                                    w.setId(databaseWord[0].getId())
//                                    // if the CSV word translation does not appear in the already existing word translation
//                                    if (!meanings.contains(translation)) {
//                                        meanings = meanings + " - " + translation
//                                        w.setTranslation(meanings)
//                                        wdm!!.update(w)
//                                        updatedWords.add(w.getHeadword())
//                                    }
//                                    // if the CSV word note does not appear in the already existing word note
//                                    if (note != "" && !dbNotes.contains(note)) {
//                                        dbNotes = dbNotes + " - " + note
//                                        w.setNote(dbNotes)
//                                        wdm!!.update(w)
//                                        // to make sure the headword won t be twice in the list of updated words
//                                        if (!updatedWords.contains(w.getHeadword())) {
//                                            updatedWords.add(w.getHeadword())
//                                        }
//                                    }
//                                }
//                            } else if (result == 0) {
//                                // if the word was successfully added in the database
//                                addedWords = addedWords + 1
//                            }
//                        }
                        addedWords = addedWords + 1
                        progress!!.incrementProgressBy(1)
                        access = br!!.readLine()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    if (br != null) {
                        try {
                            br!!.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }
                progress!!.dismiss()
                //Sending a message to the handler to notify that the thread is done
                handler.sendEmptyMessage(0)
            }
        }
        t.start()
    }

    /**
     * Suppress the simple quotes that circle q word in the CSV file
     * @param s
     * *          The word to "clean"
     * *
     * @return
     * *          The word without the simple quotes
     */
    private fun extractWord(s: String): String {
        var word = s
        val splitBy = "'" // Use the simple quote as a separator
        val strings = s.split(splitBy.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        // if the array has more than 1 element, a simple quote was found
        if (strings.size >= 2) {
            // The second element of the array is recorded
            word = strings[1]

            for (i in 2..strings.size - 1 - 1) {
                // Being in that case means that there is a simple quote in the word itself
                // So we concatenate all the elements of the array, avoiding the first and the last element
                // and we had the suppressed simple quote
                word = word + "'" + strings[i]
            }
        }
        return word
    }
}
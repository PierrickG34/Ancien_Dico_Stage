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

    /**
     * Open a CSV file and add its word to a dictionary
     * @param d The dictionary where the words will be added
     * @param uri The CSV file providing the words to add in the dictionary
     * @param context Activity which called the method
     */
    fun importCSV(d: DictionarySQLITE, uri: Uri, context: Context) : IntArray {
        var wdm = WordSQLITE(context)
        var dicoID = d.idDictionary
        var res : IntArray
        var addedWords = 0
        var updateWords = 0
        var br : BufferedReader? = null

        //Initialising the progressBar
        var progress = ProgressDialog(context)
        progress.setMessage(context.getString(R.string.import_progress))
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progress.progress = 0
        progress.setCancelable(false)
        progress.show()

        // Each line of the CSV file will by split by the comma character
        val cvsSplitBy = ","
        try {
            //InputStream is = getContentResolver().openInputStream(uri);
            //InputStream is = new BufferedInputStream(new FileInputStream(fileToRead));
            var input = context.contentResolver.openInputStream(uri)
            // ISO-8859-1 interprets accents correctly
            var br = BufferedReader(InputStreamReader(input!!, "ISO-8859-1"))
            var nbLine = 0
            while (br!!.readLine() != null) {
                nbLine++
            }
            progress!!.max = nbLine
            println(nbLine)

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
                        updateWords++
                    }
                    else {
                        addedWords++
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
                progress!!.incrementProgressBy(1)
                access = br!!.readLine()
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        finally {
            if (br != null) {
                try {
                    br!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        progress!!.dismiss()
        return intArrayOf(addedWords, updateWords)
    }

    /**
     * Suppress the simple quotes that circle q word in the CSV file
     * @param s The word to "clean"
     * @return The word without the simple quotes
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
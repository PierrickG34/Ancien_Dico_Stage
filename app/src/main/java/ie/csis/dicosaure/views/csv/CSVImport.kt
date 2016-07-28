package ie.csis.dicosaure.views.csv

import android.content.Context
import android.net.Uri
import ie.csis.dicosaure.model.dictionary.DictionarySQLITE
import ie.csis.dicosaure.model.translate.TranslateSQLITE
import ie.csis.dicosaure.model.word.WordSQLITE
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by dineen on 30/06/2016.
 */
class CSVImport {

    /**
     * Open a CSV file and add its word to a dictionary
     * @param d The dictionary where the words will be added
     * @param uri The CSV file providing the words to add in the dictionary
     * @param context Activity which called the method
     */
    fun importCSV(d: DictionarySQLITE, uri: Uri, context: Context) {
        var wdm = WordSQLITE(context)
        var dicoID = d.idDictionary
        var res : IntArray
        var br : BufferedReader? = null

        // Each line of the CSV file will by split by the comma character
        val cvsSplitBy = ","
        try {
            //InputStream is = getContentResolver().openInputStream(uri);
            //InputStream is = new BufferedInputStream(new FileInputStream(fileToRead));
            var input = context.contentResolver.openInputStream(uri)
            // ISO-8859-1 interprets accents correctly Edit, not really, why changed for UTF-8
            var br = BufferedReader(InputStreamReader(input!!, "UTF-8"))

            //InputStream iss = new BufferedInputStream(new FileInputStream(fileToRead));
            input = context.contentResolver.openInputStream(uri)
            // ISO-8859-1 interprets accents correctly Edit, not really, why changed for UTF-8
            br = BufferedReader(InputStreamReader(input!!, "UTF-8"))

            var wordInfo: Array<String>
            var note: String
            var translation: String
            var wTo: WordSQLITE
            var wFrom: WordSQLITE
            var headword : String
            var access = br!!.readLine()

            while (access != null) {
                // Split the line with comma as a separator
                wordInfo = access!!.split(cvsSplitBy.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (wordInfo.size >= 2 && extractWord(wordInfo[0]).length > 0) {
                    headword = extractWord(wordInfo[0])
                    translation = extractWord(wordInfo[1])
                    if (wordInfo.size == 2) {
                        // if there is no note
                        note = ""
                    } else {
                        note = extractWord(wordInfo[2])
                    }

                    wFrom = WordSQLITE(ctx = context, headword = headword, note = note, idDictionary = dicoID)
                    wFrom.save()

                    if (translation.length > 0) {
                        wTo = WordSQLITE(ctx = context, headword = translation, note = "", idDictionary = "0")
                        wTo.save()

                        var dtm = TranslateSQLITE(context, wFrom, wTo)
                        dtm.save()
                    }
                }
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
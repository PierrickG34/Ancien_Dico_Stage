package com.antoine_charlotte_romain.dictionary.Utilities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.Controllers.ListWordsActivity;
import com.antoine_charlotte_romain.dictionary.Controllers.MainActivity;
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel;
import com.antoine_charlotte_romain.dictionary.R;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer2 on 30/07/2015.
 */
public class ImportUtility
{
    private static WordDataModel wdm;
    public static ArrayList<String> updatedWords;
    public static int addedWords;
    private static Long dicoID;
    private static BufferedReader br;
    private static String line;
    private static ProgressDialog progress;

    /**
     * Open a CSV file and add its word to a dictionary
     *
     * @param d The dictionary where the words will be added
     * @param uri The CSV file providing the words to add in the dictionary
     * @param context Activity which called the method
     */
    public static void importCSV(final Dictionary d, final Uri uri, final Context context, final Handler handler)
    {
        wdm = new WordDataModel(context);
        updatedWords = new ArrayList<>();
        addedWords = 0;
        dicoID = d.getId();
        br = null;

        //Initialising the progressBar
        progress = new ProgressDialog(context);
        progress.setMessage((context.getString(R.string.import_progress)));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false);
        progress.show();


        final Thread t = new Thread()
        {
            @Override
            public void run() {
                // Each line of the CSV file will by split by the comma character
                String cvsSplitBy = ",";
                try {
                    //InputStream is = getContentResolver().openInputStream(uri);
                    //InputStream is = new BufferedInputStream(new FileInputStream(fileToRead));
                    InputStream is= context.getContentResolver().openInputStream(uri);
                    // ISO-8859-1 interprets accents correctly
                    br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
                    int nbLine=0;
                    while ((line = br.readLine()) != null) {
                        nbLine++;
                    }
                    progress.setMax(nbLine);

                    //InputStream iss = new BufferedInputStream(new FileInputStream(fileToRead));
                    InputStream iss= context.getContentResolver().openInputStream(uri);
                    // ISO-8859-1 interprets accents correctly
                    br = new BufferedReader(new InputStreamReader(iss, "ISO-8859-1"));

                    String[] wordInfo;
                    String note;
                    String translation;
                    Word w;
                    List<Word> databaseWord;
                    String meanings;
                    String dbNotes;
                    while ((line = br.readLine()) != null) {
                        // Split the line with comma as a separator
                        wordInfo = line.split(cvsSplitBy);
                        note = "";
                        translation = "";
                        // if a translation exists
                        if (wordInfo.length >= 2)
                            translation = extractWord(wordInfo[1]);
                        // if a note exists
                        if (wordInfo.length >= 3)
                            note = extractWord(wordInfo[2]);
                        // Add the word in the database
                        w = new Word(dicoID, extractWord(wordInfo[0]), translation, note);
                        if (!w.getHeadword().equals("")) {
                            int result = wdm.insert(w);

                            // if the headword of the word we try to insert already exists in this dictionary
                            if (result == 1) {
                                // Get the already existing word
                                databaseWord = wdm.select(w.getHeadword(), dicoID);
                                if (databaseWord.size() == 1) {
                                    // Get its translation and its note
                                    meanings = databaseWord.get(0).getTranslation();
                                    dbNotes = databaseWord.get(0).getNote();
                                    w.setId(databaseWord.get(0).getId());
                                    // if the CSV word translation does not appear in the already existing word translation
                                    if (!meanings.contains(translation)) {
                                        meanings = meanings + " - " + translation;
                                        w.setTranslation(meanings);
                                        wdm.update(w);
                                        updatedWords.add(w.getHeadword());
                                    }
                                    // if the CSV word note does not appear in the already existing word note
                                    if (!note.equals("") && !dbNotes.contains(note)) {
                                        dbNotes = dbNotes + " - " + note;
                                        w.setNote(dbNotes);
                                        wdm.update(w);
                                        // to make sure the headword won t be twice in the list of updated words
                                        if (!updatedWords.contains(w.getHeadword())) {
                                            updatedWords.add(w.getHeadword());
                                        }
                                    }
                                }
                            } else if (result == 0) {
                                // if the word was successfully added in the database
                                addedWords = addedWords + 1;
                            }
                        }
                        progress.incrementProgressBy(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                progress.dismiss();
                //Sending a message to the handler to notify that the thread is done
                handler.sendEmptyMessage(0);
            }
        };
        t.start();
    }

    /**
     * Suppress the simple quotes that circle q word in the CSV file
     * @param s
     *          The word to "clean"
     * @return
     *          The word without the simple quotes
     */
    private static String extractWord(String s){
        String word = s;
        String splitBy = "'"; // Use the simple quote as a separator
        String[] strings = s.split(splitBy);
        // if the array has more than 1 element, a simple quote was found
        if (strings.length >= 2){
            // The second element of the array is recorded
            word = strings[1];

            for (int i = 2; i<strings.length-1; i++){
                // Being in that case means that there is a simple quote in the word itself
                // So we concatenate all the elements of the array, avoiding the first and the last element
                // and we had the suppressed simple quote
                word = word + "'" + strings[i];
            }
        }
        return word;
    }
}

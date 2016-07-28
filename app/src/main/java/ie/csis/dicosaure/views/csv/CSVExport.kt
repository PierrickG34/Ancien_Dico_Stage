package ie.csis.dicosaure.views.csv

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import ie.csis.dicosaure.Controllers.R
import ie.csis.dicosaure.views.activities.MainActivity
import ie.csis.dicosaure.model.dictionary.Dictionary
import ie.csis.dicosaure.model.dictionary.DictionarySQLITE
import ie.csis.dicosaure.model.word.Word
import ie.csis.dicosaure.model.word.WordSQLITE
import ie.csis.dicosaure.lib.KeyboardUtility
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import java.io.*

/**
 * Created by dineen on 30/06/2016.
 */
class CSVExport : AppCompatActivity() {


    val REQUEST_DIRECTORY = 0

    var toolbar: Toolbar? = null

    /**
     * Used to enter the name of the exported file
     */
    var fileName: EditText? = null

    /**
     * Used to choose the directory on the device where the file will be exported
     */
    var directory: EditText? = null

    /**
     * The dictionary which will be exported
     */
    var dictionary: Dictionary? = null

    /**
     * Used to show the advancement of the export
     */
    var progress: ProgressDialog? = null

    /**
     * The dictionary choosed by the user
     */
    var selectedDirectory: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csvexport)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        this.toolbar = findViewById(R.id.tool_bar) as Toolbar?
        setSupportActionBar(toolbar)
        this.supportActionBar!!.setTitle(R.string.title_activity_csvexport)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        this.fileName = findViewById(R.id.editTextFile) as EditText?
        this.selectedDirectory = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name)

        //Creating a new file named like the application if not exists
        val initialDir = File(this.selectedDirectory!!)
        initialDir.mkdir()

        this.directory = findViewById(R.id.editTextDirectory) as EditText?
        this.directory!!.setText(this.selectedDirectory)
        this.directory!!.setOnClickListener { chooseDirectory() }

        // Getting the dictionary to export
        val intent = this.intent
        if (intent != null) {
            this.dictionary = intent.getSerializableExtra(MainActivity.EXTRA_DICTIONARY) as Dictionary

            this.fileName!!.setText(this.dictionary!!.getNameDictionary() + ".csv")
            this.fileName!!.setSelection(0, this.dictionary!!.getNameDictionary().length)
            this.supportActionBar!!.setTitle(getString(R.string.exporting) + " " + this.dictionary!!.getNameDictionary())
        }


        //Setting the EditText to always have the suffix .csv
        this.fileName!!.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().endsWith(".csv")) {
                    fileName!!.setText(".csv")
                    fileName!!.setSelection(0)
                }
            }
        })

        //Displaying the keyboard
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        setupUI(findViewById(R.id.export_layout)!!)
    }

    /**
     * Method called when the user click on the directory EditText
     */
    private fun chooseDirectory() {
        val chooserIntent = Intent(this, DirectoryChooserActivity::class.java)

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
                "DirChooserSample")

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY,
                this.selectedDirectory)

        startActivityForResult(chooserIntent, REQUEST_DIRECTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DIRECTORY && resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
            this.selectedDirectory = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
            this.directory!!.setText(this.selectedDirectory)
        }
    }

    /**
     * Method called when the user click on the export button
     * @param v
     */
    fun export(v: View) {
        //Creating the file
        val file = File(this.selectedDirectory + "/" + this.fileName!!.text.toString())

        //If the file doesn't already exists it is exported
        if (!file.exists()) {
            exportCSV(file)
        }
        else {
            AlertDialog.Builder(this@CSVExport)
                    .setTitle(R.string.file_already_exists)
                    .setPositiveButton(R.string.overwrite, DialogInterface.OnClickListener { dialog, which -> exportCSV(file) })
                    .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }//Else the user can overwrite the existing file
    }

    /**
     * Method which create a CSV file containing the word list of the current dictionary.
     * If the file already exists it is overwritted.

     * @param file Exported .csv file
     */
    private fun exportCSV(file: File) {
        val ddm = DictionarySQLITE(ctx = this.applicationContext, id = this.dictionary!!.idDictionary)

        //Initialising the progressBar
        this.progress = ProgressDialog(this)
        this.progress!!.setMessage(getString(R.string.export_progress))
        this.progress!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        this.progress!!.progress = 0
        this.progress!!.setCancelable(false)
        this.progress!!.show()

        //Handling the end of the export
        val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                progress!!.dismiss()
                AlertDialog.Builder(this@CSVExport)
                        .setTitle(R.string.success).setMessage(R.string.dictionary_exported)
                        .setNegativeButton(R.string.returnString, DialogInterface.OnClickListener { dialog, which -> finish() })
                        .setPositiveButton(R.string.open_it, DialogInterface.OnClickListener {
                            dialog, which ->
                            val viewDoc = Intent(Intent.ACTION_VIEW)
                            viewDoc.setDataAndType(
                                    Uri.fromFile(file),
                                    "text/csv")
                            try {
                                startActivity(viewDoc)
                            } catch (e: ActivityNotFoundException) {
                                Snackbar.make(findViewById(R.id.export_layout)!!, getString(R.string.no_apps), Snackbar.LENGTH_LONG).setAction(R.string.close_button, View.OnClickListener { })
                                        .show()

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show()
            }
        }

        val t = object : Thread() {
            override fun run() {
                //Selecting the words
                val words : List<Word> = ddm.selectAllWords()
                progress!!.max = words.size

                //Initialising the variables
                val bw: BufferedWriter
                val comma = ","
                var headword = ""
                var translation = ""
                var note = ""

                try {
                    file.createNewFile()
                    val os = BufferedOutputStream(FileOutputStream(file, false))
                    // ISO-8859-1 interprets accents correctly
                    bw = BufferedWriter(OutputStreamWriter(os, "ISO-8859-1"))
                    println(words)

                    // For each word in the dictionary
                    for (w in words) {
                        val wdm = WordSQLITE(ctx = applicationContext, idWord = w.idWord)
                        val translations = wdm.selectAllTranslations()

                        if (translations.size == 0) {
                            headword = filterComma(w.headword!!)
                            translation = filterComma("")
                            note = filterComma(w.note!!)
                            bw.write(headword + comma + translation + comma + note)
                            bw.newLine()
                        }
                        else {
                            for (t in translations) {
                                headword = filterComma(w.headword!!)
                                translation = filterComma(t.headword!!)
                                note = filterComma(w.note!!)
                                bw.write(headword + comma + translation + comma + note)
                                bw.newLine()
                            }
                        }
                        progress!!.incrementProgressBy(1)
                    }
                    bw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                //Sending a message to the handler to notify that the thread is done
                handler.sendEmptyMessage(0)
            }
        }
        t.start()

    }

    private fun filterComma(stringToFilter: String): String {
        val result = stringToFilter
        if (stringToFilter.contains(",")) {
            result.replace(',', ';')
        }
        return result
    }


    fun setupUI(view: View) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                KeyboardUtility.hideSoftKeyboard(this@CSVExport)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0..view.childCount - 1) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }
}
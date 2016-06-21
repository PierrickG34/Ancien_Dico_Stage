package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.InputType
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DictionaryAdapterCallbackKot
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DictionaryAdapterKot
import com.antoine_charlotte_romain.dictionary.Controllers.Lib.HeaderGridView
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import java.util.*

/**
 * Created by dineen on 17/06/2016.
 */
class HomeFragmentKot: Fragment(), DictionaryAdapterCallbackKot {

    /**
     * The view corresponding to this fragment.

     * @see MainActivityKot
     */
    var v: View? = null

    /**
     * Initial dictionary list. Contains all the dictionary.
     */
    var dictionaries: ArrayList<Dictionary>? = null

    /**
     * List of displayed dictionaries according to the research performed
     */
    var dictionariesDisplay: ArrayList<Dictionary>? = null

    /**
     * Allow to display a list of Objects.
     */
    var gridView: HeaderGridView? = null

    /**
     * Button on the right corner of the screen to add dictionaries
     */
    var addButton: FloatingActionButton? = null

    /**
     * Custom ArrayAdapter to manage the different rows of the grid
     */
    var adapter: DictionaryAdapterKot? = null


    /**
     * Used to communicating with the database
     */
    var dictionaryModel: DictionarySQLITE? = null

    /**
     * Used to handle a undo action after deleting a dictionary
     */
    var undo: Boolean = false

    /**
     * Header of the gridView
     */
    var header: View? = null

    var headerButton: Button? = null

    var state: Int = 0

    /**
     * Toolbar menu
     */
    var menu: Menu? = null

    var searchBox: EditText? = null
    var inLangField: EditText? = null
    var outLangField: EditText? = null

    var myLastFirstVisibleItem: Int = 0
    var hidden: Boolean = false

    companion object {
        val CONTEXT_MENU_READ = 0
        val CONTEXT_MENU_UPDATE = 1
        val CONTEXT_MENU_DELETE = 2
        val CONTEXT_MENU_EXPORT = 3
        val NORMAL_STATE = 0
        val DELETE_STATE = 1
        val SELECT_FILE = 0
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.v = inflater!!.inflate(R.layout.fragment_home, container, false)
        super.setHasOptionsMenu(true)
        this.state = NORMAL_STATE

        this.initData()
        this.initFloatingActionButton()
//        this.initGridView()
//        this.initEditText()

        return v
    }

    /**
     * Initialising the data model and selecting all the dictionaries
     */
    private fun initData() {
        this.dictionaryModel = DictionarySQLITE(this.context)

        this.dictionaries!!.addAll(this.dictionaryModel!!.selectAll())
        this.dictionariesDisplay = ArrayList<Dictionary>(dictionaries)
    }

    /**
     * Creating the Floating Action Button to add a dictionary through a dialog window
     */
    private fun initFloatingActionButton() {
        this.addButton = (this.activity as MainActivityKot).addButton
        this.addButton!!.setOnClickListener(View.OnClickListener { create() })
    }

    /**
     * Method which allows user to create a dictionary with a unique name
     */
    fun create() {
        //Creating the dialog layout
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 30, 60, 0)

        //Creating the EditText

        //add inlang field
        this.inLangField = EditText(this.activity)
        this.inLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.inLangField!!.setHint(R.string.dictionary_name)
        this.inLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.inLangField!!.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        layout.addView(this.inLangField)

        //add outlang field
        this.outLangField = EditText(this.activity)
        this.outLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.outLangField!!.setHint(R.string.dictionary_name)
        this.outLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.outLangField!!.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        layout.addView(this.outLangField)

        //Creating the dialog builder
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_dictionary)

        //Adding the layout to the dialog
        builder.setView(layout)

        //Dialog positive action
        builder.setPositiveButton(R.string.add) { dialog, which ->
            if (!this.inLangField!!.getText().toString().isEmpty() && !this.inLangField!!.getText().toString().isEmpty()) {
                val d = DictionarySQLITE(this.context, this.inLangField!!.getText().toString(), this.outLangField!!.getText().toString())
                if (d.save() == 1) {
                    this.dictionariesDisplay!!.add(d)
                    this.dictionaries!!.add(d)
                    if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                        this.searchBox!!.setText("")
                    }
                    this.read(this.dictionariesDisplay!!.indexOf(d))
                }
                else {
                    Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_not_added, Snackbar.LENGTH_LONG)
                            .setAction(R.string.close_button) { }
                            .show()
                }
            }
            else {
                Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_not_added_empty_string, Snackbar.LENGTH_LONG)
                        .setAction(R.string.close_button) { }
                        .show()
            }
            dialog.cancel()
        }

        //Dialog negative action
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }

        // TO do
        builder.setNeutralButton(R.string.from_csv) { dialog, which ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/comma-separated-values"

            // special intent for Samsung file manager
            val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
            sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values")
            sIntent.addCategory(Intent.CATEGORY_DEFAULT)

            if (activity.packageManager.resolveActivity(sIntent, 0) != null) {
                startActivityForResult(sIntent, SELECT_FILE)
            } else {
                startActivityForResult(intent, SELECT_FILE)
            }
            if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                this.searchBox!!.setText("")
            }
        }

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        this.inLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })
        this.outLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })
        alertDialog.show()
    }

    override fun delete(position: Int) {
        val dictionary = this.dictionariesDisplay!!.get(position) //Get dictionary
        this.dictionariesDisplay!!.remove(dictionary) //delete from array

        this.adapter!!.notifyDataSetChanged()
        this.undo = false

        val snack = Snackbar.make((activity as MainActivityKot).rootLayout, """${dictionary.getNameDictionary()} ${getString(R.string.deleted)}""", Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, View.OnClickListener {
                    this.undo = true
                })

        snack.getView().addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                //Once snackbar is closed, whatever the way : undo button clicked, change activity, an other snackbar, etc.
                if (!undo) {
                    dictionaries!!.remove(dictionary)

                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setMessage(getString(R.string.delete_progress))
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.isIndeterminate = true
                    progressDialog.setCancelable(false)
                    progressDialog.window.setGravity(Gravity.BOTTOM)
                    progressDialog.show()

                    val handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            progressDialog.dismiss()
                        }
                    }

                    val t = object : Thread() {
                        override fun run() {
                            dictionaryModel!!.delete(dictionary.idDictionary!!)
                            handler.sendEmptyMessage(0)
                        }
                    }
                    t.start()
                }
                else {
                    dictionariesDisplay!!.add(position, dictionary)
                    adapter!!.notifyDataSetChanged()
                }
            }
        })
        snack.show()
    }

    override fun update(position: Int) {
        val dictionary = this.dictionariesDisplay!!.get(position)//get dictionary

        //Set popup update dictionary
        val layout = LinearLayout(this.activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 30, 60, 0)

        //Set in lang field
        this.inLangField = EditText(this.activity)
        this.inLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.inLangField!!.setText(dictionary.inLang)
        this.inLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.inLangField!!.selectAll()
        layout.addView(inLangField)

        //Set out lang field
        this.outLangField = EditText(this.activity)
        this.outLangField!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        this.outLangField!!.setText(dictionary.outLang)
        this.outLangField!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        this.outLangField!!.selectAll()
        layout.addView(outLangField)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.update_dictionary)
        builder.setView(layout)

        builder.setPositiveButton(R.string.modify) { dialog, which ->
            val title = dictionary.getNameDictionary()

            if (this.dictionaryModel!!.update(this.inLangField!!.getText().toString(), this.outLangField!!.getText().toString()) == 1) {
                this.adapter!!.notifyDataSetChanged()

                //Set dictionary object
                dictionary.inLang = this.inLangField!!.getText().toString()
                dictionary.outLang = this.outLangField!!.getText().toString()

                //Set search box
                if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
                    this.searchBox!!.setText("")
                }

                Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_renamed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.close_button) { }.show()
            }
            else {
                Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button) { }.show()
            }
            dialog.cancel()
        }

        builder.setNegativeButton(R.string.cancel) {
            dialog, which -> dialog.cancel()
        }

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        this.outLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })

        this.inLangField!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            //Simulating a positive button click. The positive action is executed.
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            true
        })

        alertDialog.show()
    }


    override fun read(position: Int) {
        //Set object on the cell
        val intent = Intent(this.getActivity(), ListWordsActivity::class.java) //TO DO
        if (position != -1) {
            intent.putExtra(MainActivityKot.EXTRA_DICTIONARY, this.dictionariesDisplay!!.get(position))
        }
        startActivity(intent)

        //set searchBox into empty
        if (this.searchBox!!.getText().toString().trim { it <= ' ' }.length > 0) {
            this.searchBox!!.setText("")
        }
    }

    //To do
    override fun export(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun notifyDeleteListChanged() {
        //Popup message to delete multi dictionaries
        val s = this.adapter!!.deleteList.size
        this.menu!!.findItem(R.id.nb_items).setTitle("""${s} ${getString(R.string.item)}""")
        this.menu!!.findItem(R.id.action_delete_list).isVisible = s > 0
        if (this.adapter!!.all_selected)
            this.headerButton!!.setText(R.string.deselect_all)
        else
            this.headerButton!!.setText(R.string.select_all)
    }

}
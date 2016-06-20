package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.InputType
import android.util.TypedValue
import android.view.*
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
    var nameBox:EditText? = null

    var myLastFirstVisibleItem: Int = 0
    var hidden: Boolean = false

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
//        val dictionary = this.dictionariesDisplay!!.get(position) //get dictionary
//
//        val layout = LinearLayout(this.activity)
//        layout.orientation = LinearLayout.VERTICAL
//        layout.setPadding(60, 30, 60, 0)
//
//        this.nameBox = EditText(this.activity)
//        this.nameBox!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//        this.nameBox!!.setText(dictionary.getNameDictionary())
//        this.nameBox!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
//        this.nameBox!!.selectAll()
//        layout.addView(nameBox)
//
//        val builder = AlertDialog.Builder(activity)
//        builder.setTitle(R.string.update_dictionary)
//        builder.setView(layout)
//
//        builder.setPositiveButton(R.string.modify) { dialog, which ->
//            val title = dictionary.getNameDictionary()
//            dictionary.setTitle(nameBox.getText().toString())
//            if (ddm.update(d) == 1) {
//                adapter.notifyDataSetChanged()
//                if (searchBox.getText().toString().trim { it <= ' ' }.length > 0) {
//                    searchBox.setText("")
//                }
//
//                Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button) { }.show()
//            } else {
//                d.setTitle(title)
//                Snackbar.make((activity as MainActivityKot).rootLayout!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button) { }.show()
//            }
//            dialog.cancel()
//        }
//
//        builder.setNegativeButton(R.string.cancel
//        ) { dialog, which -> dialog.cancel() }
//
//        //Creating the dialog and opening the keyboard
//        val alertDialog = builder.create()
//        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//
//        //Listening the keyboard to handle a "Done" action
//        nameBox.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
//            //Simulating a positive button click. The positive action is executed.
//            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
//            true
//        })
//
//        alertDialog.show()
    }

    override fun read(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun export(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun notifyDeleteListChanged() {
        throw UnsupportedOperationException()
    }

}
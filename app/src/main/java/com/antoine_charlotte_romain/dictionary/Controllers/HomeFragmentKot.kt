package com.antoine_charlotte_romain.dictionary.Controllers

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DictionaryAdapter
import com.antoine_charlotte_romain.dictionary.Controllers.Lib.HeaderGridView
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import java.util.*

/**
 * Created by dineen on 17/06/2016.
 */
class HomeFragmentKot: Fragment(), DictionaryAdapter.DictionaryAdapterCallback {
    override fun delete(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun update(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun export(position: Int) {
        throw UnsupportedOperationException()
    }


    /*---------------------------------------------------------
       *                        CONSTANTS
       *---------------------------------------------------------*/

    private val CONTEXT_MENU_READ = 0
    private val CONTEXT_MENU_UPDATE = 1
    private val CONTEXT_MENU_DELETE = 2
    private val CONTEXT_MENU_EXPORT = 3
    private val NORMAL_STATE = 0
    private val DELETE_STATE = 1
    private val SELECT_FILE = 0

    /*---------------------------------------------------------
       *                     INSTANCE VARIABLES
       *---------------------------------------------------------*/

    /**
     * The view corresponding to this fragment.

     * @see MainActivityKot
     */
    private var myView: View? = null

    /**
     * Initial dictionary list. Contains all the dictionary.
     */
//    private var dictionaries: ArrayList<Dictionary>? = null
    var dictionaries: ArrayList<com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary>? = null

    /**
     * List of displayed dictionaries according to the research performed
     */
    private var dictionariesDisplay: ArrayList<com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary>? = null

    /**
     * Allow to display a list of Objects.
     */
    private var gridView: HeaderGridView? = null

    /**
     * Button on the right corner of the screen to add dictionaries
     */
    private var addButton: FloatingActionButton? = null

    /**
     * Custom ArrayAdapter to manage the different rows of the grid
     */
    private var adapter: DictionaryAdapter? = null


    /**
     * Used to communicating with the database
     */
    //private var dictionaryModel: DictionaryDataModel? = null
    var dictionaryModel: DictionarySQLITE? = null

    /**
     * Used to handle a undo action after deleting a dictionary
     */
    private var undo:Boolean = false

    /**
     * Header of the gridView
     */
    private var header: View? = null

    private var headerButton: Button? = null

    private var state:Int = 0

    /**
     * Toolbar menu
     */
    private var menu: Menu? = null

    private var searchBox: EditText? = null
    private var nameBox: EditText? = null

    private var myLastFirstVisibleItem:Int = 0
    private var hidden:Boolean = false



    /*---------------------------------------------------------
       *                     INSTANCE METHODS
       *---------------------------------------------------------*/

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.myView = inflater!!.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        this.state = NORMAL_STATE

        this.initData()
        this.initFloatingActionButton()
        this.initGridView()
        this.initEditText()

        return this.myView
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Initialising the data model and selecting all the dictionaries
     */
    private fun initData() {
//        this.dictionaryModel = DictionaryDataModel(this.activity)
//        this.dictionaryModel!!.open()
//
//        this.dictionaries = this.dictionaryModel!!.selectAll()
//        this.dictionariesDisplay = ArrayList(dictionaries!!)

        this.dictionaryModel = DictionarySQLITE(this.context)
        this.dictionaries = ArrayList(this.dictionaryModel!!.selectAll())
        this.dictionariesDisplay = ArrayList(this.dictionaries)
    }

    /**
     * Initialising the GridView to display the dictionary list and making its clickables
     */
    private fun initGridView() {
        //Creating the GridView
        gridView = myView!!.findViewById(R.id.dictionary_list) as HeaderGridView
        gridView!!.setDrawSelectorOnTop(true)

        if (state == NORMAL_STATE)
        {
            //Adding the GridView header
            gridView!!.removeHeaderView(header)
            header = activity.layoutInflater.inflate(R.layout.grid_view_header, null)
            gridView!!.addHeaderView(header)
            val b = header!!.findViewById(R.id.button_all) as Button
            b.setText(R.string.all_dictionaries)
            b.setOnClickListener(android.view.View.OnClickListener { read(-1) })

            //Populating the GridView
            adapter = DictionaryAdapter(getActivity(), R.layout.dictionary_row, dictionariesDisplay)
            adapter!!.setCallback(this)
            gridView!!.setAdapter(adapter)

            //Adding the context menu on each rows
            registerForContextMenu(gridView!!)
        }
        else if (state == DELETE_STATE)
        {
            //Adding the GridView header
            gridView!!.removeHeaderView(header)
            header = getActivity().getLayoutInflater().inflate(R.layout.grid_view_header, null)
            gridView!!.addHeaderView(header)
            headerButton = header!!.findViewById(R.id.button_all) as Button
            headerButton!!.setText(R.string.select_all)
            headerButton!!.setOnClickListener(object: View.OnClickListener {
                public override fun onClick(v: View) {
                    adapter!!.selectAll()
                }
            })

            //Populating the GridView
            adapter = DictionaryAdapter(getActivity(), R.layout.delete_dictionary_row, dictionariesDisplay)
            adapter!!.setCallback(this)
            gridView!!.setAdapter(adapter)
        }

        //Animating the gridView on Scroll
        myLastFirstVisibleItem = 0
        hidden = false
        addButton!!.animate().translationY(0f)
        gridView!!.setOnScrollListener(object: AbsListView.OnScrollListener {
            public override fun onScrollStateChanged(view: AbsListView, scrollState:Int) {
                val currentFirstVisibleItem = gridView!!.getFirstVisiblePosition()
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                {
                    if (currentFirstVisibleItem > myLastFirstVisibleItem)
                    {
                        if (!hidden)
                        {
                            addButton!!.animate().translationY(350f)
                            hidden = true
                        }
                    }
                    else if (currentFirstVisibleItem < myLastFirstVisibleItem)
                    {
                        if (hidden)
                        {
                            addButton!!.animate().translationY(0f)
                            hidden = false
                        }
                    }
                }
                myLastFirstVisibleItem = currentFirstVisibleItem
            }

            public override fun onScroll(view: AbsListView, firstVisibleItem:Int, visibleItemCount:Int, totalItemCount:Int) {
                val lastInScreen = firstVisibleItem + visibleItemCount
                if ((lastInScreen == totalItemCount))
                {
                    if (hidden)
                    {
                        addButton!!.animate().translationY(0f)
                        hidden = false
                    }
                }
            }
        })


        //Animating the gridView on appear
        val anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left)
        gridView!!.setAnimation(anim)
        anim.start()

    }


    /**
     * Initialising the search box to dynamically researching on the dictionary list
     */
    private fun initEditText() {
        //Creating the EditText for searching inside the dictionaries list
        searchBox = myView!!.findViewById(R.id.search_field) as EditText
        searchBox!!.addTextChangedListener(object: TextWatcher {
            public override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {}

            public override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {}

            public override fun afterTextChanged(s: Editable) {
                dictionariesDisplay!!.clear()
                val search = s.toString()
                for (i in dictionaries!!.indices)
                {
                    val title = """${dictionaries!!.get(i).inLang} -> ${dictionaries!!.get(i).outLang}"""
                    if (title.toLowerCase().contains(search.toLowerCase()))
                        dictionariesDisplay!!.add(dictionaries!!.get(i))
                }
                adapter!!.notifyDataSetChanged()
            }
        })

        searchBox!!.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            public override fun onEditorAction(v: TextView, actionId:Int, event: KeyEvent):Boolean {
                if (!dictionariesDisplay!!.isEmpty())
                    read(0)
                return true
            }
        })

    }

    /**
     * Creating the Floating Action Button to add a dictionary through a dialog window
     */
    private fun initFloatingActionButton() {
        addButton = ((getActivity()) as MainActivityKot).addButton
        addButton!!.setOnClickListener(object: View.OnClickListener {

            public override fun onClick(v: View) {
                //create()
            }

        })
    }

    public override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val info = menuInfo as AdapterView.AdapterContextMenuInfo

        val title = """${(adapter!!.getItem(info.position - 1)).inLang} -> ${(adapter!!.getItem(info.position - 1)).outLang}"""
        menu.setHeaderTitle(title)

        menu.add(Menu.NONE, CONTEXT_MENU_READ, Menu.NONE, R.string.open)
        menu.add(Menu.NONE, CONTEXT_MENU_UPDATE, Menu.NONE, R.string.rename)
        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete)
        menu.add(Menu.NONE, CONTEXT_MENU_EXPORT, Menu.NONE, R.string.csvexport_export)
    }

    public override fun onContextItemSelected(item: MenuItem?):Boolean {
        val info = item!!.getMenuInfo() as AdapterView.AdapterContextMenuInfo
        when (item!!.getItemId()) {
            CONTEXT_MENU_READ -> {
                read(info.position - 1)
                return true
            }
            CONTEXT_MENU_UPDATE -> {
                update(info.position - 1)
                return true
            }
            CONTEXT_MENU_DELETE -> {
                delete(info.position - 1)
                return true
            }
            CONTEXT_MENU_EXPORT -> {
                export(info.position - 1)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }


    /**
     * Method which allows user to create a dictionary with a unique name
     */
    /*fun create() {
        //Creating the dialog layout
        val layout = LinearLayout(getActivity())
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.setPadding(60, 30, 60, 0)

        //Creating the EditText to type the dictionary name
        nameBox = EditText(getActivity())
        nameBox!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        nameBox!!.setHint(R.string.dictionary_name)
        nameBox!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        nameBox!!.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)

        //Adding the EditText to the layout
        layout.addView(nameBox)

        //Creating the dialog builder
        val builder = AlertDialog.Builder(getActivity())
        builder.setTitle(R.string.add_dictionary)

        //Adding the layout to the dialog
        builder.setView(layout)

        //Dialog positive action
        builder.setPositiveButton(R.string.add,
                object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, which:Int) {
                        if (nameBox!!.getText().toString() != "")
                        {
                            val d = Dictionary(nameBox!!.getText().toString())
                            if (dictionaryModel!!.insert(d) == 1)
                            {
                                dictionariesDisplay!!.add(d)
                                dictionaries!!.add(d)
                                if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
                                {
                                    searchBox!!.setText("")
                                }
                                read(dictionariesDisplay!!.indexOf(d))
                            }
                            else
                            {
                                Snackbar.make(((getActivity()) as MainActivityKot).rootLayout!!, R.string.dictionary_not_added, Snackbar.LENGTH_LONG).setAction(R.string.close_button, object: View.OnClickListener {
                                    public override fun onClick(v: View) {

                                    }
                                }).show()
                            }
                        }
                        else
                        {
                            Snackbar.make(((getActivity()) as MainActivityKot).rootLayout!!, R.string.dictionary_not_added_empty_string, Snackbar.LENGTH_LONG).setAction(R.string.close_button, object: View.OnClickListener {
                                public override fun onClick(v: View) {

                                }
                            }).show()
                        }
                        dialog.cancel()
                    }
                })

        //Dialog negative action
        builder.setNegativeButton(R.string.cancel,
                object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, which:Int) {
                        dialog.cancel()
                    }
                })

        builder.setNeutralButton(R.string.from_csv,
                object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, which:Int) {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.setType("text/comma-separated-values")

                        // special intent for Samsung file manager
                        val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
                        sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values")
                        sIntent.addCategory(Intent.CATEGORY_DEFAULT)

                        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null)
                        {
                            startActivityForResult(sIntent, SELECT_FILE)
                        }
                        else
                        {
                            startActivityForResult(intent, SELECT_FILE)
                        }
                        if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
                        {
                            searchBox!!.setText("")
                        }
                    }
                })

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        nameBox!!.setOnEditorActionListener(object: TextView.OnEditorActionListener {

            public override fun onEditorAction(v: TextView, actionId:Int, event: KeyEvent):Boolean {
                //Simulating a positive button click. The positive action is executed.
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                return true
            }
        })

        alertDialog.show()
    }*/


    /**
     * Method which allows user to open a dictionary. It is Redirecting to the ListWordsActivity.

     * @param position position of the dictionary to read in the dictionariesDisplay list.
     */
    public override fun read(position:Int) {
        val intent = Intent(this.getActivity(), ListWordsActivity::class.java)
        if (position != -1)
            //intent.putExtra(MainActivityKot.EXTRA_DICTIONARY, dictionariesDisplay!!.get(position))
        startActivity(intent)

        if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
        {
            searchBox!!.setText("")
        }
    }


    /**
     * Method which allows user to rename a dictionary with a unique name.

     * @param position position of the dictionary to update in the dictionariesDisplay list.
     */
    /*public override fun update(position:Int) {
        val d = dictionariesDisplay!!.get(position)

        val layout = LinearLayout(getActivity())
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.setPadding(60, 30, 60, 0)

        nameBox = EditText(getActivity())
        nameBox!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        nameBox!!.setText(d.getTitle())
        nameBox!!.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        nameBox!!.selectAll()
        layout.addView(nameBox)

        val builder = AlertDialog.Builder(getActivity())
        builder.setTitle(R.string.rename_dictionary)
        builder.setView(layout)


        builder.setPositiveButton(R.string.rename,
                object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, which:Int) {
                        val title = d.getTitle()
                        d.setTitle(nameBox!!.getText().toString())
                        if (dictionaryModel!!.update(d) == 1)
                        {
                            adapter!!.notifyDataSetChanged()
                            if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
                            {
                                searchBox!!.setText("")
                            }

                            Snackbar.make(((getActivity()) as MainActivityKot).rootLayout!!, R.string.dictionary_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button, object: View.OnClickListener {
                                public override fun onClick(v: View) {

                                }
                            }).show()
                        }
                        else
                        {
                            d.setTitle(title)
                            Snackbar.make(((getActivity()) as MainActivityKot).rootLayout!!, R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button, object: View.OnClickListener {
                                public override fun onClick(v: View) {

                                }
                            }).show()
                        }
                        dialog.cancel()
                    }
                })

        builder.setNegativeButton(R.string.cancel,
                object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, which:Int) {
                        dialog.cancel()
                    }
                })

        //Creating the dialog and opening the keyboard
        val alertDialog = builder.create()
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        //Listening the keyboard to handle a "Done" action
        nameBox!!.setOnEditorActionListener(object: TextView.OnEditorActionListener {

            public override fun onEditorAction(v: TextView, actionId:Int, event: KeyEvent):Boolean {
                //Simulating a positive button click. The positive action is executed.
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                return true
            }
        })

        alertDialog.show()

    }*/

    /**
     * Method which allows user to delete a dictionary.

     * @param position position of the dictionary to delete in the dictionariesDisplay list.
     */
    /*public override fun delete(position:Int) {
        val d = dictionariesDisplay!!.get(position)
        dictionariesDisplay!!.remove(d)
        adapter!!.notifyDataSetChanged()
        undo = false
        val snack = Snackbar.make(((getActivity()) as MainActivityKot).rootLayout, d.getTitle() + getString(R.string.deleted), Snackbar.LENGTH_LONG).setAction(R.string.undo, object: View.OnClickListener {
            public override fun onClick(v: View) {
                undo = true
            }
        })
        snack.getView().addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
            public override fun onViewAttachedToWindow(v: View) {}

            public override fun onViewDetachedFromWindow(v: View) {
                //Once snackbar is closed, whatever the way : undo button clicked, change activity, an other snackbar, etc.
                if (!undo)
                {
                    dictionaries!!.remove(d)

                    val progressDialog = ProgressDialog(getActivity())
                    progressDialog.setMessage(getString(R.string.delete_progress))
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.setIndeterminate(true)
                    progressDialog.setCancelable(false)
                    progressDialog.getWindow().setGravity(Gravity.BOTTOM)
                    progressDialog.show()
                    val handler = object: Handler() {
                        public override fun handleMessage(msg: Message) {
                            progressDialog.dismiss()
                        }
                    }

                    val t = object:Thread() {
                        public override fun run() {
                            dictionaryModel!!.delete(d.getId())
                            handler.sendEmptyMessage(0)
                        }
                    }
                    t.start()


                }
                else
                {
                    dictionariesDisplay!!.add(position, d)
                    adapter!!.notifyDataSetChanged()
                }
            }
        })
        snack.show()

    }*/

    /**
     * This function is called when the user click on the exportCsv button, it launch the view exportACsv

     */
    /*public override fun export(position:Int) {
        val exportCSVintent = Intent(getActivity(), CSVExportActivity::class.java)
        exportCSVintent.putExtra(MainActivityKot.EXTRA_DICTIONARY, dictionariesDisplay!!.get(position))
        startActivity(exportCSVintent)
        if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
        {
            searchBox!!.setText("")
        }
    }*/

    /*public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //If we are importing a file
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK)
        {
            //Creating the file
            val fileUri = data!!.getData()
            val fileName = fileUri.getLastPathSegment()

            //Creating ta dictionary named like the file (without the extension)
            val d = Dictionary(fileName.substring(0, fileName.indexOf(".")))

            val c = getActivity()

            //Handling the end of the import
            val handler = object: Handler() {
                public override fun handleMessage(msg: Message) {
                    val intent = Intent(c, ListWordsActivity::class.java)
                    intent.putExtra(MainActivityKot.EXTRA_DICTIONARY, d)
                    intent.putExtra(MainActivityKot.EXTRA_RENAME, true)
                    c.startActivity(intent)
                }
            }

            if (dictionaryModel!!.insert(d) == 1)
            {
                dictionariesDisplay!!.add(d)
                dictionaries!!.add(d)
                if (searchBox!!.getText().toString().trim({ it <= ' ' }).length > 0)
                {
                    searchBox!!.setText("")
                }

                ImportUtility.importCSV(d, data!!.getData(), c, handler)
            }
            else
                Toast.makeText(getActivity(), R.string.dictionary_not_added, Toast.LENGTH_SHORT).show()
        }
    }*/


    public override fun notifyDeleteListChanged() {
        val s = adapter!!.getDeleteList().size
        menu!!.findItem(R.id.nb_items).setTitle("""${s} ${getString(R.string.item)}""")
        menu!!.findItem(R.id.action_delete_list).setVisible(s > 0)
        if (adapter!!.isAll_selected())
            headerButton!!.setText(R.string.deselect_all)
        else
            headerButton!!.setText(R.string.select_all)
    }

    public override fun onCreateOptionsMenu(m: Menu?, inflater: MenuInflater?) {
        menu = m
        super.onCreateOptionsMenu(menu, inflater)
        showMenu()
    }

    fun showMenu() {
        menu!!.clear()
        if (state == NORMAL_STATE)
        {
            getActivity().getMenuInflater().inflate(R.menu.menu_home, menu)
        }
        else if (state == DELETE_STATE)
        {
            getActivity().getMenuInflater().inflate(R.menu.menu_home_delete, menu)
            val s = adapter!!.getDeleteList().size
            menu!!.findItem(R.id.nb_items).setTitle("""${s} ${getString(R.string.item)}""")
            menu!!.findItem(R.id.action_delete_list).setVisible(s > 0)
        }
    }

    public override fun onOptionsItemSelected(item: MenuItem?):Boolean {
        when (item!!.getItemId()) {
            R.id.action_add_dictionary -> {
                //create()
                return true
            }

            R.id.action_multiple_delete -> {
                state = DELETE_STATE
                initGridView()
                showMenu()
                return true
            }
            R.id.action_delete_list -> {
                val alert = AlertDialog.Builder(getActivity())
                val s = adapter!!.getDeleteList().size
                if (s == 1)
                {
                    alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.dictionary) + " ?")
                }
                else
                {
                    alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.dictionaries) + " ?")
                }
                alert.setPositiveButton(getString(R.string.delete), object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, whichButton:Int) {

                        val progressDialog = ProgressDialog(getActivity())
                        progressDialog.setMessage(getString(R.string.delete_progress))
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        progressDialog.setIndeterminate(true)
                        progressDialog.setCancelable(false)
                        progressDialog.getWindow().setGravity(Gravity.BOTTOM)
                        progressDialog.show()

                        val handler = object: Handler() {
                            public override fun handleMessage(msg: Message) {
                                progressDialog.dismiss()
                                state = NORMAL_STATE
                                initGridView()
                                showMenu()
                            }
                        }
                        val t = object:Thread() {
                            public override fun run() {

                                for (i in 0..s - 1)
                                {
                                    val d = adapter!!.getDeleteList().get(i)
                                    dictionaries!!.remove(d)
                                    dictionariesDisplay!!.remove(d)
                                    //dictionaryModel!!.delete(d.id)
                                }
                                handler.sendEmptyMessage(0)
                            }
                        }
                        t.start()

                    }
                })

                alert.setNegativeButton(getString(R.string.cancel), object: DialogInterface.OnClickListener {
                    public override fun onClick(dialog: DialogInterface, whichButton:Int) {}
                })

                alert.show()
                return true
            }
            R.id.action_cancel -> {
                state = NORMAL_STATE
                initGridView()
                showMenu()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


}
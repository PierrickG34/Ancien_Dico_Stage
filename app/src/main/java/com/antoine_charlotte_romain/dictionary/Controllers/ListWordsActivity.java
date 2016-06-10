package com.antoine_charlotte_romain.dictionary.Controllers;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.WordAdapter;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel;
import com.antoine_charlotte_romain.dictionary.R;
import com.antoine_charlotte_romain.dictionary.Utilities.ImportUtility;
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility;

import java.util.ArrayList;

/**
 * TODO Garder l'état des activités au changement d'orientation
 * Created by summer1 on 24/06/2015.
 */
public class ListWordsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, WordAdapter.WordAdapterCallback{

    private final int CONTEXT_MENU_MODIFY = 0;
    private final int CONTEXT_MENU_DELETE = 1;
    private final int NORMAL_STATE = 0;
    private final int DELETE_STATE = 1;
    private final int NEW_WORD = 1;
    private final int DELETE_WORD = 2;
    private final int SELECT_FILE = 0;

    private EditText filterWords;
    private GridView gridViewWords;
    private Toolbar toolbar;
    private FloatingActionButton menuButton;
    private FloatingActionButton addButton;
    private TextView addText;
    private FloatingActionButton importCsvButton;
    private TextView importText;
    private FloatingActionButton exportCsvButton;
    private TextView exportText;
    private EditText nameBox;
    private Menu menu;
    private View backgroundMenuView;
    private ProgressDialog progressDialog;

    private WordDataModel wdm;
    private DictionaryDataModel ddm;
    private Dictionary selectedDictionary;
    private ArrayList<Word> myWordsList;
    private WordAdapter myAdapter;

    private boolean open;
    private boolean undo;
    private boolean loadingMore;
    private boolean allLoaded;
    private boolean hidden;
    private int wordsLimit;
    private int wordsOffset;
    private int myLastFirstVisibleItem;
    private int actualListSize;
    private int stateMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_words);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        selectedDictionary = (Dictionary)intent.getSerializableExtra(MainActivity.EXTRA_DICTIONARY);

        filterWords = (EditText) findViewById(R.id.filterWords);
        gridViewWords = (GridView) findViewById(R.id.gridViewWords);
        menuButton = (FloatingActionButton) findViewById(R.id.floatingMenuButton);
        addButton = (FloatingActionButton) findViewById(R.id.addWordButton);
        addText = (TextView) findViewById(R.id.textAddAWord);
        importCsvButton = (FloatingActionButton) findViewById(R.id.importCsvButton);
        importText = (TextView) findViewById(R.id.textImportACsv);
        exportCsvButton = (FloatingActionButton) findViewById(R.id.exportCsvButton);
        exportText = (TextView) findViewById(R.id.textExportACsv);
        backgroundMenuView = findViewById(R.id.surfaceView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loadingWords));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);

        gridViewWords.setOnItemClickListener(this);

        setupUI(findViewById(R.id.list_words_layout));

        registerForContextMenu(gridViewWords);

        if(intent.getBooleanExtra(MainActivity.EXTRA_RENAME,false)) {
            getIntent().removeExtra(MainActivity.EXTRA_RENAME);
            renameDictionary(findViewById(R.id.list_words_layout));
        }


        menuButton.bringToFront();

        allLoaded = false;
        wordsLimit = 10;
        wordsOffset = 0;
        myLastFirstVisibleItem = 0;

        backgroundMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingMenu(v);
            }
        });

        initListView();

        showToolbarMenu();
    }

    /**
     * This function is called when a child activity back to this view or finish
     */
    @Override
    public void onResume() {
        super.onResume();

        loadingMore = false;
        stateMode = NORMAL_STATE;
        normalMode();

        backgroundMenuView.setVisibility(View.GONE);
        if(selectedDictionary != null){
            open = false;
            menuButton.animate().translationY(0);
            hidden = false;
            exportCsvButton.setVisibility(View.VISIBLE);
            importCsvButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
     public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        if(selectedDictionary != null) {
            getMenuInflater().inflate(R.menu.menu_list_words, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_export_csv:
                exportCsv(findViewById(R.id.list_words_layout));
                return true;

            case R.id.action_import_csv:
                importCsv(findViewById(R.id.list_words_layout));
                return true;

            case R.id.action_add_word:
                newWord(findViewById(R.id.list_words_layout));
                return true;

            case R.id.action_rename_dictionary:
                renameDictionary(findViewById(R.id.list_words_layout));
                return true;

            case R.id.action_delete_dictionary:
                deleteDictionary(findViewById(R.id.list_words_layout));
                return true;

            case R.id.action_delete_words:
                multipleDeleteMode();
                return true;

            case R.id.action_cancel_list:
                normalMode();
                return true;

            case R.id.action_delete_list:
                deleteSelectedWords();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Function that load all the words of a dictionary from the database and show them on the listView
     */
    private void initListView(){
        wdm = new WordDataModel(getApplicationContext());
        final boolean select;

        if(selectedDictionary == null) {
            myWordsList = wdm.selectAll(Word.ALL_DICTIONARIES, wordsLimit, wordsOffset);
            menuButton.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
            addText.setVisibility(View.GONE);
            importCsvButton.setVisibility(View.GONE);
            importText.setVisibility(View.GONE);
            exportCsvButton.setVisibility(View.GONE);
            exportText.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.allDico);
            select = false;
        }
        else{
            myWordsList = wdm.selectAll(selectedDictionary.getId(), wordsLimit, wordsOffset);
            menuButton.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(selectedDictionary.getTitle());
            select = true;
        }

        myAdapter = new WordAdapter(this, R.layout.row_word, myWordsList, select);
        myAdapter.setCallback(this);

        gridViewWords.setAdapter(myAdapter);
        gridViewWords.setTextFilterEnabled(true);

        gridViewWords.setOnScrollListener(new AbsListView.OnScrollListener() {

            int currentVisibleItemCount;
            int currentFirstVisibleItem;
            int currentScrollState;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentVisibleItemCount = visibleItemCount;
                this.currentFirstVisibleItem = firstVisibleItem;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (!open) {
                    final int currentFirstVisibleItem = gridViewWords.getFirstVisiblePosition();
                    if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
                        if (currentFirstVisibleItem > myLastFirstVisibleItem) {
                            if (!hidden) {
                                menuButton.animate().translationY(350);
                                hidden = true;
                                exportCsvButton.setVisibility(View.GONE);
                                importCsvButton.setVisibility(View.GONE);
                                addButton.setVisibility(View.GONE);
                            }
                        } else if (currentFirstVisibleItem < myLastFirstVisibleItem) {
                            if (hidden) {
                                menuButton.animate().translationY(0);
                                hidden = false;
                                exportCsvButton.setVisibility(View.VISIBLE);
                                importCsvButton.setVisibility(View.VISIBLE);
                                addButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    myLastFirstVisibleItem = currentFirstVisibleItem;
                }
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            private void isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    int lastInScreen = currentFirstVisibleItem + currentVisibleItemCount;
                    if ((lastInScreen == myWordsList.size()) && !(loadingMore) && !(allLoaded)) {
                        progressDialog.show();
                        Thread thread = new Thread(null, loadMoreListWords);
                        thread.start();
                    }
                    else if((lastInScreen == myWordsList.size()) && !(loadingMore) && (allLoaded)) {
                        menuButton.animate().translationY(0);
                        hidden = false;
                        exportCsvButton.setVisibility(View.VISIBLE);
                        importCsvButton.setVisibility(View.VISIBLE);
                        addButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        filterWords.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    myWordsList.clear();
                    ArrayList<Word> tempList;

                    if (s.length() > 0) {
                        String search = s.toString();
                        if (select) {
                            tempList = wdm.select(search, selectedDictionary.getId());
                        } else {
                            tempList = wdm.select(search, Word.ALL_DICTIONARIES);
                        }
                    }
                    else {
                        wordsOffset = 0;
                        if (select) {
                            tempList = wdm.selectAll(selectedDictionary.getId(), wordsLimit, wordsOffset);
                        } else {
                            tempList = wdm.selectAll(Word.ALL_DICTIONARIES, wordsLimit, wordsOffset);
                        }
                        allLoaded = false;
                    }

                    for (int i = 0; i < tempList.size(); i++) {
                        myWordsList.add(tempList.get(i));
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * This function is called when the user click on an item of the gridView
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!open){
            modify(position);
        }
    }

    /**
     * This function is called when the user click on the addWord button, it launch the view newWord
     * @param view
     */
    public void newWord(View view){
        if(open){
            showFloatingMenu(view);
        }
        Intent newWordIntent = new Intent(this, WordActivity.class);

        newWordIntent.putExtra(MainActivity.EXTRA_DICTIONARY, selectedDictionary);

        startActivityForResult(newWordIntent, NEW_WORD);

        if(filterWords.getText().toString().trim().length() > 0) {
            filterWords.setText("");
        }
    }

    /**
     * This function is called when the user click on the exportCsv button, it launch the view exportACsv
     * @param view
     */
    public void exportCsv(View view){
        if(open){
            showFloatingMenu(view);
        }
        Intent exportCSVintent = new Intent(this, CSVExportActivity.class);
        exportCSVintent.putExtra(MainActivity.EXTRA_DICTIONARY, selectedDictionary);

        startActivity(exportCSVintent);

        if(filterWords.getText().toString().trim().length() > 0) {
            filterWords.setText("");
        }
    }

    /**
     * This function is called when the user click on the importCsv button, it launch the view importACsv
     * @param view
     */
    public void importCsv(View view){
        //TODO : New version of the import !!!
        if(open){
            showFloatingMenu(view);
        }

        /*Intent importCSVintent = new Intent(this, CSVImportActivity.class);
        importCSVintent.putExtra(MainActivity.EXTRA_NEW_DICO_NAME, selectedDictionary.getTitle());

        startActivity(importCSVintent);*/

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if (getPackageManager().resolveActivity(sIntent, 0) != null){
            startActivityForResult(sIntent, SELECT_FILE);
        }
        else {
            startActivityForResult(intent, SELECT_FILE);
        }

        if(filterWords.getText().toString().trim().length() > 0) {
            filterWords.setText("");
        }
    }

    /**
     * This function launch the activity advancedSearch
     * @param view
     */
    public void advancedSearch(View view){
        Intent advancedSearchIntent = new Intent(this, MainActivity.class);

        advancedSearchIntent.putExtra(MainActivity.EXTRA_FRAGMENT, "advancedSearch");
        advancedSearchIntent.putExtra(MainActivity.EXTRA_DICTIONARY, selectedDictionary);

        startActivity(advancedSearchIntent);

        if(filterWords.getText().toString().trim().length() > 0) {
            filterWords.setText("");
        }
    }

    /**
     *This function is used to show or hide the buttons add a word and import a csv after the click on the floatingMenuButton (+)
     * @param view
     */
    @Override
    public void showFloatingMenu(View view) {
        if(open){
            backgroundMenuView.setVisibility(View.GONE);

            animationCloseMenu(exportCsvButton, 3);
            animationCloseMenu(exportText, 3);
            animationCloseMenu(importCsvButton, 2);
            animationCloseMenu(importText, 2);
            animationCloseMenu(addButton, 1);
            animationCloseMenu(addText, 1);

            menuButton.animate().rotation(0);
            menuButton.bringToFront();

            open = false;
        }
        else {
            backgroundMenuView.setVisibility(View.VISIBLE);

            animationOpenMenu(exportCsvButton, 3);
            animationOpenMenu(exportText, 3);
            animationOpenMenu(importCsvButton, 2);
            animationOpenMenu(importText, 2);
            animationOpenMenu(addButton, 1);
            animationOpenMenu(addText, 1);

            menuButton.animate().rotation(45);

            open = true;
        }
    }

    /**
     * This function creates the contextMenu when an item of the listView is long pressed
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(!open) {
            super.onCreateContextMenu(menu, v, menuInfo);

            String title = (myWordsList.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position)).getHeadword();
            menu.setHeaderTitle(title);

            menu.add(Menu.NONE, CONTEXT_MENU_MODIFY, Menu.NONE, R.string.modify);
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete);
        }
    }
    /**
     * This function creates the items of the contextMenu
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case CONTEXT_MENU_DELETE :
                delete(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
                return true;

            case CONTEXT_MENU_MODIFY :
                modify(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
                return true;

            default :
                return super.onContextItemSelected(item);
        }
    }

    /**
     * This function deletes the word at the selected position in the listView
     * @param position the position in the listView of the word the user want to delete
     */
    private void delete(int position){
        undo = false;
        final int pos = position;
        final Word w = myWordsList.get(position);
        myWordsList.remove(w);
        myAdapter.notifyDataSetChanged();

        Snackbar.make(findViewById(R.id.list_words_layout), w.getHeadword() + getString(R.string.deleted), Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWordsList.add(pos, w);
                myAdapter.notifyDataSetChanged();
                undo = true;
            }
        }).show();

        wdm = new WordDataModel(getApplicationContext());

        Thread timer = new Thread(){
            public void run(){
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if(!undo) {
                        wdm.delete(w.getId());
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * This function launches the view details of a word and allows to modify it
     * @param position the position in the listView of the word the user want to see more details or to modify
     */
    private void modify(int position){
        Intent wordDetailIntent = new Intent(this, WordActivity.class);

        wordDetailIntent.putExtra(MainActivity.EXTRA_WORD, myWordsList.get(position));
        if(selectedDictionary != null) {
            wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, selectedDictionary);
        }
        else{
            ddm = new DictionaryDataModel(getApplicationContext());
            wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, ddm.select(myWordsList.get(position).getDictionaryID()));
        }

        startActivityForResult(wordDetailIntent, DELETE_WORD);

        if(filterWords.getText().toString().trim().length() > 0) {
            filterWords.setText("");
        }
    }

    /**
     * This function allow to rename the current dictionary
     * @param view
     */
    public void renameDictionary(View view){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 0);

        nameBox = new EditText(this);
        nameBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameBox.setText(selectedDictionary.getTitle());
        nameBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameBox.selectAll();
        layout.addView(nameBox);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rename_dictionary);
        builder.setView(layout);


        builder.setPositiveButton(R.string.rename,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String title = selectedDictionary.getTitle();
                        selectedDictionary.setTitle(nameBox.getText().toString());
                        ddm = new DictionaryDataModel(getApplicationContext());
                        if (ddm.update(selectedDictionary) == 1) {
                            Snackbar.make(findViewById(R.id.list_words_layout), getString(R.string.dictionary_renamed), Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();

                            getSupportActionBar().setTitle(selectedDictionary.getTitle());
                        } else {
                            selectedDictionary.setTitle(title);
                            Snackbar.make(findViewById(R.id.list_words_layout), getString(R.string.dictionary_not_renamed), Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        //Creating the dialog and opening the keyboard
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        //Listening the keyboard to handle a "Done" action
        nameBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Simulating a positive button click. The positive action is executed.
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                return true;
            }
        });

        alertDialog.show();
    }

    /**
     * This function delete the current dictionary after a confirmation
     * @param view
     */
    public void deleteDictionary(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.delete_dictionary) +" ?");
        alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), selectedDictionary.getTitle() + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                ddm = new DictionaryDataModel(getApplicationContext());
                ddm.delete(selectedDictionary.getId());
                finish();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * This function is used to modify and show the toolbar menu when the stateMode changes
     */
    private void showToolbarMenu(){
        if(menu != null) {
            menu.clear();
            if (stateMode == NORMAL_STATE) {
                if(selectedDictionary != null) {
                    getMenuInflater().inflate(R.menu.menu_list_words, menu);
                    getSupportActionBar().setTitle(selectedDictionary.getTitle());
                }
                else {
                    getSupportActionBar().setTitle(R.string.allDico);
                }

            } else if (stateMode == DELETE_STATE) {
                getMenuInflater().inflate(R.menu.menu_word_delete, menu);
                getSupportActionBar().setTitle("0 " + getString(R.string.item));
                menu.findItem(R.id.action_delete_list).setVisible(false);
            }
        }
    }

    /**
     * This function leave the deletion mode
     */
    private void normalMode(){
        stateMode = NORMAL_STATE;
        myAdapter = new WordAdapter(this, R.layout.row_word, myWordsList, true);
        myAdapter.setCallback(this);
        gridViewWords.setAdapter(myAdapter);
        showToolbarMenu();
    }

    /**
     * This function allows the user to delete multiple words at the same time
     */
    private void multipleDeleteMode(){
        stateMode = DELETE_STATE;

        myAdapter = new WordAdapter(this, R.layout.row_delete_word, myWordsList, true);
        myAdapter.setCallback(this);
        gridViewWords.setAdapter(myAdapter);

        showToolbarMenu();
    }

    /**
     * This function deletes all the words selected by the user.
     */
    private void deleteSelectedWords(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final int s = myAdapter.getDeleteList().size();
        if(s == 1){
            alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.word) + " ?");
        }
        else {
            alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.words) + " ?");
        }
        alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                for (int i = 0; i < s; i++) {
                    myWordsList.remove(myAdapter.getDeleteList().get(i));
                    wdm.delete(myAdapter.getDeleteList().get(i).getId());
                }
                myAdapter.notifyDataSetChanged();
                normalMode();
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * This function is called when the user click on the option delete from the row menu of a word and it deletes it
     * @param position the position of the item to delete in the list
     */
    @Override
    public void deletePressed(int position){
        delete(position);
    }

    /**
     * This function is called when the user on the option modify from the row menu of a word and it launch the view details of this word
     * @param position the position of the item to modify in the list
     */
    @Override
    public void modifyPressed(int position){
        modify(position);
    }

    /**
     * This function retunr the value of the boolean open
     * @return
     */
    @Override
    public boolean getOpen(){
        return open;
    }

    /**
     * Method that notify the view that the deleteList has changed
     */
    @Override
    public void notifyDeleteListChanged()
    {
        int s = myAdapter.getDeleteList().size();
        getSupportActionBar().setTitle(s + " " + getString(R.string.item));
        menu.findItem(R.id.action_delete_list).setVisible(s > 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If we are importing a file
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK )
        {
            final Context c = this;

            //Handling the end of the import
            final Handler handler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    recreate();
                    //Creating an AlertDialog to show the updated words
                    AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this);
                    builder.setTitle(R.string.csv_imported);
                    builder.setMessage(ImportUtility.addedWords + " " + getString(R.string.nb_added_words) +"\n"+ ImportUtility.updatedWords.size() + " " + getString(R.string.updated_words));
                    builder.setNegativeButton(R.string.returnString,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            };

            ImportUtility.importCSV(selectedDictionary, data.getData(), c, handler);
        }
        else if ((requestCode == NEW_WORD || requestCode == DELETE_WORD) && resultCode == RESULT_OK )
        {
            System.out.println("activitySucces");
            myWordsList.clear();
            ArrayList<Word> tempList;

            wordsOffset = 0;
            tempList = wdm.selectAll(selectedDictionary.getId(), wordsLimit, wordsOffset);

            allLoaded = false;

            for (int i = 0; i < tempList.size(); i++) {
                myWordsList.add(tempList.get(i));
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    /**
     * This thread is launch when the user scroll to the end of the list and it load more words
     */
    private Runnable loadMoreListWords = new Runnable(){
        @Override
        public void run() {
            loadingMore = true;
            ArrayList<Word> tempList = new ArrayList<>();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wordsOffset += 10;
            wdm = new WordDataModel(getApplicationContext());
            if(filterWords.getText().toString().length() == 0) {
                if (selectedDictionary == null) {
                    tempList = wdm.selectAll(Word.ALL_DICTIONARIES, wordsLimit, wordsOffset);
                } else {
                    tempList = wdm.selectAll(selectedDictionary.getId(), wordsLimit, wordsOffset);
                }
            }

            actualListSize = myWordsList.size();
            for(int i = 0; i < tempList.size(); i++){
                myWordsList.add(tempList.get(i));
            }
            runOnUiThread(returnRes);
        }
    };

    /**
     * This thread tell the adapter that the more words were loaded
     */
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            myAdapter.notifyDataSetChanged();
            loadingMore = false;
            if(actualListSize == myWordsList.size()){
                allLoaded = true;
            }
            progressDialog.dismiss();
        }
    };

    /**
     * This animation is used to make the floating menu appear
     * @param v the view to make appear
     * @param i an int to put a little delay between each animation
     */
    private void animationOpenMenu(View v, int i){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -(size.y*(getApplicationContext().getResources().getInteger(R.integer.floating_menu_translation))/100)*i);
        PropertyValuesHolder pvhsX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1);
        PropertyValuesHolder pvhsY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1);

        final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(v,  pvhY,  pvhsX, pvhsY);
        animation.setDuration(500);
        animation.setInterpolator(new OvershootInterpolator(0.9f));

        // Put a slight lag between each of the menu items to make it asymmetric
        animation.setStartDelay(i * 20);
        animation.start();

        v.setEnabled(true);
    }

    /**
     * This animation is used to make the floating menu disappear
     * @param v the view to make disappear
     * @param i an int to put a little delay between each animation
     */
    private void animationCloseMenu(View v, int i){
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
        PropertyValuesHolder pvhsX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0);
        PropertyValuesHolder pvhsY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0);

        final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(v, pvhY, pvhsX, pvhsY);
        animation.setDuration(500);
        animation.setInterpolator(new OvershootInterpolator(0.9f));

        // Put a slight lag between each of the menu items to make it asymmetric
        animation.setStartDelay(i * 20);
        animation.start();

        v.setEnabled(false);
    }

    /**
     * This function is used to hide the keyBoard on click outside an editText
     * @param view
     */
    private void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    KeyboardUtility.hideSoftKeyboard(ListWordsActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }
}

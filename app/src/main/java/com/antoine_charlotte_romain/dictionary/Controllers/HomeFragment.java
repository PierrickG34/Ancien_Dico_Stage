package com.antoine_charlotte_romain.dictionary.Controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.DictionaryAdapter;
import com.antoine_charlotte_romain.dictionary.Controllers.Lib.HeaderGridView;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.R;
import com.antoine_charlotte_romain.dictionary.Utilities.ImportUtility;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements DictionaryAdapter.DictionaryAdapterCallback {


    /*---------------------------------------------------------
    *                        CONSTANTS
    *---------------------------------------------------------*/

    private final int CONTEXT_MENU_READ = 0;
    private final int CONTEXT_MENU_UPDATE = 1;
    private final int CONTEXT_MENU_DELETE = 2;
    private final int CONTEXT_MENU_EXPORT = 3;
    private final int NORMAL_STATE = 0;
    private final int DELETE_STATE = 1;
    private final int SELECT_FILE = 0;

    /*---------------------------------------------------------
    *                     INSTANCE VARIABLES
    *---------------------------------------------------------*/

    /**
     * The view corresponding to this fragment.
     *
     * @see MainActivity
     */
    private View v;

    /**
     * Initial dictionary list. Contains all the dictionary.
     */
    private ArrayList<Dictionary> dictionaries;

    /**
     * List of displayed dictionaries according to the research performed
     */
    private ArrayList<Dictionary> dictionariesDisplay;

    /**
     * Allow to display a list of Objects.
     */
    private HeaderGridView gridView;

    /**
     * Button on the right corner of the screen to add dictionaries
     */
    private FloatingActionButton addButton;

    /**
     * Custom ArrayAdapter to manage the different rows of the grid
     */
    private DictionaryAdapter adapter;


    /**
     * Used to communicating with the database
     */
    private DictionaryDataModel ddm;

    /**
     * Used to handle a undo action after deleting a dictionary
     */
    private boolean undo;

    /**
     * Header of the gridView
     */
    private View header;

    private Button headerButton;

    private int state;

    /**
     * Toolbar menu
     */
    private Menu menu;

    private EditText searchBox, nameBox;

    private int myLastFirstVisibleItem;
    private boolean hidden;


    /*---------------------------------------------------------
    *                       CONSTRUCTORS
    *---------------------------------------------------------*/

    public HomeFragment() {
        // Required empty public constructor
    }



    /*---------------------------------------------------------
    *                     INSTANCE METHODS
    *---------------------------------------------------------*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_home,container,false);
        setHasOptionsMenu(true);
        state = NORMAL_STATE;

        initData();
        initFloatingActionButton();
        initGridView();
        initEditText();

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    /**
     * Initialising the data model and selecting all the dictionaries
     */
    private void initData()
    {
        ddm = new DictionaryDataModel(getActivity());
        ddm.open();

        dictionaries = ddm.selectAll();
        dictionariesDisplay = new ArrayList<>(dictionaries);
    }

    /**
     * Initialising the GridView to display the dictionary list and making its clickables
     */
    private void initGridView()
    {
        //Creating the GridView
        gridView = (HeaderGridView) v.findViewById(R.id.dictionary_list);
        gridView.setDrawSelectorOnTop(true);

        if(state == NORMAL_STATE)
        {
            //Adding the GridView header
            gridView.removeHeaderView(header);
            header = getActivity().getLayoutInflater().inflate(R.layout.grid_view_header, null);
            gridView.addHeaderView(header);
            Button b = (Button) header.findViewById(R.id.button_all);
            b.setText(R.string.all_dictionaries);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    read(-1);
                }
            });

            //Populating the GridView
            adapter = new DictionaryAdapter(getActivity(), R.layout.dictionary_row, dictionariesDisplay);
            adapter.setCallback(this);
            gridView.setAdapter(adapter);

            //Adding the context menu on each rows
            registerForContextMenu(gridView);
        }
        else if(state == DELETE_STATE)
        {
            //Adding the GridView header
            gridView.removeHeaderView(header);
            header = getActivity().getLayoutInflater().inflate(R.layout.grid_view_header, null);
            gridView.addHeaderView(header);
            headerButton = (Button) header.findViewById(R.id.button_all);
            headerButton.setText(R.string.select_all);
            headerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.selectAll();
                }
            });

            //Populating the GridView
            adapter = new DictionaryAdapter(getActivity(), R.layout.delete_dictionary_row, dictionariesDisplay);
            adapter.setCallback(this);
            gridView.setAdapter(adapter);
        }

        //Animating the gridView on Scroll
        myLastFirstVisibleItem = 0;
        hidden = false;
        addButton.animate().translationY(0);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int currentFirstVisibleItem = gridView.getFirstVisiblePosition();
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
                    if (currentFirstVisibleItem > myLastFirstVisibleItem) {
                        if (!hidden) {
                            addButton.animate().translationY(350);
                            hidden = true;
                        }
                    } else if (currentFirstVisibleItem < myLastFirstVisibleItem) {
                        if (hidden) {
                            addButton.animate().translationY(0);
                            hidden = false;
                        }
                    }
                }
                myLastFirstVisibleItem = currentFirstVisibleItem;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount)) {
                    if (hidden) {
                        addButton.animate().translationY(0);
                        hidden = false;
                    }
                }
            }
        });


        //Animating the gridView on appear
        Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        gridView.setAnimation(anim);
        anim.start();

    }


    /**
     * Initialising the search box to dynamically researching on the dictionary list
     */
    private void initEditText()
    {
        //Creating the EditText for searching inside the dictionaries list
        searchBox = (EditText) v.findViewById(R.id.search_field);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                dictionariesDisplay.clear();
                String search = s.toString();
                for (int i = 0; i < dictionaries.size(); i++) {
                    if (dictionaries.get(i).getTitle().toLowerCase().contains(search.toLowerCase()))
                        dictionariesDisplay.add(dictionaries.get(i));
                }
                adapter.notifyDataSetChanged();
            }
        });

        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!dictionariesDisplay.isEmpty())
                    read(0);
                return true;
            }
        });

    }

    /**
     * Creating the Floating Action Button to add a dictionary through a dialog window
     */
    private void initFloatingActionButton()
    {
        addButton = ((MainActivity)(getActivity())).getAddButton();
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                create();
            }

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo  info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        String title = (adapter.getItem(info.position - 1)).getTitle();
        menu.setHeaderTitle(title);

        menu.add(Menu.NONE, CONTEXT_MENU_READ, Menu.NONE, R.string.open);
        menu.add(Menu.NONE, CONTEXT_MENU_UPDATE, Menu.NONE, R.string.rename);
        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete);
        menu.add(Menu.NONE, CONTEXT_MENU_EXPORT, Menu.NONE, R.string.csvexport_export);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_MENU_READ:
                read(info.position - 1);
                return true;
            case CONTEXT_MENU_UPDATE:
                update(info.position - 1);
                return true;
            case CONTEXT_MENU_DELETE:
                delete(info.position - 1);
                return true;
            case CONTEXT_MENU_EXPORT:
                export(info.position - 1);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    /**
     * Method which allows user to create a dictionary with a unique name
     */
    public void create()
    {
        //Creating the dialog layout
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 0);

        //Creating the EditText to type the dictionary name
        nameBox = new EditText(getActivity());
        nameBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameBox.setHint(R.string.dictionary_name);
        nameBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameBox.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        //Adding the EditText to the layout
        layout.addView(nameBox);

        //Creating the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_dictionary);

        //Adding the layout to the dialog
        builder.setView(layout);

        //Dialog positive action
        builder.setPositiveButton(R.string.add,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nameBox.getText().toString().equals("")) {
                            Dictionary d = new Dictionary(nameBox.getText().toString());
                            if (ddm.insert(d) == 1) {
                                dictionariesDisplay.add(d);
                                dictionaries.add(d);
                                if(searchBox.getText().toString().trim().length() > 0) {
                                    searchBox.setText("");
                                }
                                read(dictionariesDisplay.indexOf(d));
                            } else {
                                Snackbar.make(((MainActivity)(getActivity())).getRootLayout(), R.string.dictionary_not_added, Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            }
                        } else {
                            Snackbar.make(((MainActivity)(getActivity())).getRootLayout(), R.string.dictionary_not_added_empty_string, Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }
                        dialog.cancel();
                    }
                });

        //Dialog negative action
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.setNeutralButton(R.string.from_csv,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("text/comma-separated-values");

                            // special intent for Samsung file manager
                            Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                            sIntent.putExtra("CONTENT_TYPE", "text/comma-separated-values");
                            sIntent.addCategory(Intent.CATEGORY_DEFAULT);

                            if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null){
                                startActivityForResult(sIntent, SELECT_FILE);
                            }
                            else {
                                startActivityForResult(intent, SELECT_FILE);
                            }
                        if(searchBox.getText().toString().trim().length() > 0) {
                            searchBox.setText("");
                        }
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
     * Method which allows user to open a dictionary. It is Redirecting to the ListWordsActivity.
     *
     * @param position position of the dictionary to read in the dictionariesDisplay list.
     */
    @Override
    public void read(int position)
    {
        Intent intent = new Intent(HomeFragment.this.getActivity(),ListWordsActivity.class);
        if(position != -1)
            intent.putExtra(MainActivity.EXTRA_DICTIONARY, dictionariesDisplay.get(position));
        startActivity(intent);

        if(searchBox.getText().toString().trim().length() > 0) {
            searchBox.setText("");
        }
    }


    /**
     * Method which allows user to rename a dictionary with a unique name.
     *
     * @param position position of the dictionary to update in the dictionariesDisplay list.
     */
    @Override
    public void update(int position)
    {
        final Dictionary d = dictionariesDisplay.get(position);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 0);

        nameBox = new EditText(getActivity());
        nameBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameBox.setText(d.getTitle());
        nameBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameBox.selectAll();
        layout.addView(nameBox);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.rename_dictionary);
        builder.setView(layout);


        builder.setPositiveButton(R.string.rename,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String title = d.getTitle();
                        d.setTitle(nameBox.getText().toString());
                        if (ddm.update(d) == 1) {
                            adapter.notifyDataSetChanged();
                            if(searchBox.getText().toString().trim().length() > 0) {
                                searchBox.setText("");
                            }

                            Snackbar.make(((MainActivity)(getActivity())).getRootLayout(), R.string.dictionary_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        } else {
                            d.setTitle(title);
                            Snackbar.make(((MainActivity)(getActivity())).getRootLayout(), R.string.dictionary_not_renamed, Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
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
     * Method which allows user to delete a dictionary.
     *
     * @param position position of the dictionary to delete in the dictionariesDisplay list.
     */
    @Override
    public void delete(final int position)
    {
        final Dictionary d = dictionariesDisplay.get(position);
        dictionariesDisplay.remove(d);
        adapter.notifyDataSetChanged();
        undo = false;
        Snackbar snack = Snackbar.make(((MainActivity)(getActivity())).getRootLayout(), d.getTitle() + getString(R.string.deleted), Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo = true;
            }
        });
        snack.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //Once snackbar is closed, whatever the way : undo button clicked, change activity, an other snackbar, etc.
                if (!undo) {
                    dictionaries.remove(d);

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.delete_progress));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.getWindow().setGravity(Gravity.BOTTOM);
                    progressDialog.show();
                    final Handler handler = new Handler()
                    {
                        @Override
                        public void handleMessage(Message msg) {
                            progressDialog.dismiss();
                        }
                    };

                    final Thread t = new Thread() {
                        @Override
                        public void run() {
                            ddm.delete(d.getId());
                            handler.sendEmptyMessage(0);
                        }
                    };
                    t.start();


                } else {
                    dictionariesDisplay.add(position, d);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        snack.show();

    }

    /**
     * This function is called when the user click on the exportCsv button, it launch the view exportACsv
     *
     */
    public void export(int position)
    {
        Intent exportCSVintent = new Intent(getActivity(), CSVExportActivity.class);
        exportCSVintent.putExtra(MainActivity.EXTRA_DICTIONARY, dictionariesDisplay.get(position));
        startActivity(exportCSVintent);
        if(searchBox.getText().toString().trim().length() > 0) {
            searchBox.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If we are importing a file
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK )
        {
            //Creating the file
            Uri fileUri = data.getData();
            String fileName = fileUri.getLastPathSegment();

            //Creating ta dictionary named like the file (without the extension)
            final Dictionary d = new Dictionary(fileName.substring(0, fileName.indexOf(".")));

            final Context c = getActivity();

            //Handling the end of the import
            final Handler handler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    Intent intent = new Intent(c,ListWordsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_DICTIONARY, d);
                    intent.putExtra(MainActivity.EXTRA_RENAME, true);
                    c.startActivity(intent);
                }
            };

            if (ddm.insert(d) == 1) {
                dictionariesDisplay.add(d);
                dictionaries.add(d);
                if(searchBox.getText().toString().trim().length() > 0) {
                    searchBox.setText("");
                }

                ImportUtility.importCSV(d, data.getData(), c, handler);
            }
            else
                Toast.makeText(getActivity(), R.string.dictionary_not_added, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void notifyDeleteListChanged()
    {
        int s = adapter.getDeleteList().size();
        menu.findItem(R.id.nb_items).setTitle(s + " " + getString(R.string.item));
        menu.findItem(R.id.action_delete_list).setVisible(s > 0);
        if (adapter.isAll_selected())
            headerButton.setText(R.string.deselect_all);
        else
            headerButton.setText(R.string.select_all);
    }

    @Override
    public void onCreateOptionsMenu(Menu m, MenuInflater inflater)
    {
        menu = m;
        super.onCreateOptionsMenu(menu, inflater);
        showMenu();
    }

    public void showMenu()
    {
        menu.clear();
        if(state == NORMAL_STATE)
        {
            getActivity().getMenuInflater().inflate(R.menu.menu_home, menu);
        }
        else if (state == DELETE_STATE)
        {
            getActivity().getMenuInflater().inflate(R.menu.menu_home_delete, menu);
            int s = adapter.getDeleteList().size();
            menu.findItem(R.id.nb_items).setTitle(s + " " + getString(R.string.item));
            menu.findItem(R.id.action_delete_list).setVisible(s > 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_add_dictionary:
                create();
                return true;

            case R.id.action_multiple_delete:
                state = DELETE_STATE;
                initGridView();
                showMenu();
                return true;
            case R.id.action_delete_list:
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final int s = adapter.getDeleteList().size();
                if(s == 1){
                    alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.dictionary) + " ?");
                }
                else {
                    alert.setMessage(getString(R.string.delete) + " " + s + " " + getString(R.string.dictionaries) + " ?");
                }
                alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage(getString(R.string.delete_progress));
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
                        progressDialog.show();

                        final Handler handler = new Handler()
                        {
                            @Override
                            public void handleMessage(Message msg) {
                                progressDialog.dismiss();
                                state = NORMAL_STATE;
                                initGridView();
                                showMenu();
                            }
                        };
                        final Thread t = new Thread() {
                            @Override
                            public void run() {

                                for (int i = 0; i < s ; i++)
                                {
                                    Dictionary d = adapter.getDeleteList().get(i);
                                    dictionaries.remove(d);
                                    dictionariesDisplay.remove(d);
                                    ddm.delete(d.getId());
                                }
                                handler.sendEmptyMessage(0);
                            }
                        };
                        t.start();

                    }
                });

                alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
                return true;
            case R.id.action_cancel:
                state = NORMAL_STATE;
                initGridView();
                showMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
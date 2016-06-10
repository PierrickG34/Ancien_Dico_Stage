package com.antoine_charlotte_romain.dictionary.Controllers;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.antoine_charlotte_romain.dictionary.Business.SearchDate;
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.SearchDateAdapter;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.DataModel.SearchDateDataModel;
import com.antoine_charlotte_romain.dictionary.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private View thisView;
    private EditText historySearch;
    private GridView gridViewHistory;
    private ProgressDialog progressDialog;
    private Button advancedSearchButton;
    private Button resetButton;

    private SearchDateDataModel sddm;
    private ArrayList<SearchDate> mySearchDateList;
    private SearchDateAdapter myAdapter;

    private boolean loadingMore;
    private boolean allLoaded;
    private int historyLimit;
    private int historyOffset;
    private int actualListSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_history,container,false);

        historySearch = (EditText) thisView.findViewById(R.id.historySearch);
        gridViewHistory = (GridView) thisView.findViewById(R.id.gridViewHistory);
        advancedSearchButton = (Button) thisView.findViewById(R.id.buttonAdvancedSearch);
        resetButton = (Button) thisView.findViewById(R.id.buttonReset);

        initListView();

        setHasOptionsMenu(true);

        return thisView;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu m, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_history, m);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_clear_history:
                clearHistory();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Function that load all the search history on the database and show it on the listView
     */
    private void initListView(){
        historyLimit = 10;
        historyOffset = 0;
        allLoaded = false;

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loadingHistory));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);

        sddm = new SearchDateDataModel(getActivity());
        mySearchDateList = sddm.selectAll(historyLimit, historyOffset);

        myAdapter = new SearchDateAdapter(getActivity(), R.layout.row_history, mySearchDateList);

        gridViewHistory.setAdapter(myAdapter);
        gridViewHistory.setTextFilterEnabled(true);

        gridViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                seeWord(position);
            }
        });

        gridViewHistory.setOnScrollListener(new AbsListView.OnScrollListener() {

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
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            private void isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    int lastInScreen = currentFirstVisibleItem + currentVisibleItemCount;
                    if ((lastInScreen == mySearchDateList.size()) && !(loadingMore) && !(allLoaded)) {
                        progressDialog.show();
                        Thread thread = new Thread(null, loadMoreHistory);
                        thread.start();
                    }
                }
            }
        });

        historySearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    mySearchDateList.clear();
                    ArrayList<SearchDate> tempList;

                    if (s.length() > 0) {
                        String search = s.toString();
                        tempList = sddm.select(search);
                    } else {
                        historyOffset = 0;
                        tempList = sddm.selectAll(historyLimit, historyOffset);
                        allLoaded = false;
                    }

                    for (int i = 0; i < tempList.size(); i++) {
                        mySearchDateList.add(tempList.get(i));
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
        });

        advancedSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.history_advanced_search_dialog, null);
                dialogBuilder.setView(dialogView);

                final EditText dateBeforeEditText = (EditText) dialogView.findViewById(R.id.editTextBefore);
                final EditText dateAfterEditText = (EditText) dialogView.findViewById(R.id.editTextAfter);
                dateBeforeEditText.setInputType(InputType.TYPE_NULL);
                dateAfterEditText.setInputType(InputType.TYPE_NULL);

                final Calendar myCalendar = Calendar.getInstance();

                final DatePickerDialog.OnDateSetListener dateBeforeCalendar = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String myFormat = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                        dateBeforeEditText.setText(sdf.format(myCalendar.getTime()));
                    }

                };

                dateBeforeEditText.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(getActivity(), dateBeforeCalendar, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                final DatePickerDialog.OnDateSetListener dateAfterCalendar = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String myFormat = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                        dateAfterEditText.setText(sdf.format(myCalendar.getTime()));
                    }

                };

                dateAfterEditText.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(getActivity(), dateAfterCalendar, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });


                dialogBuilder.setTitle(R.string.advanced_search);

                dialogBuilder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mySearchDateList.clear();
                        ArrayList<SearchDate> tempList = sddm.select(dateBeforeEditText.getText().toString(), dateAfterEditText.getText().toString());
                        if (tempList != null) {
                            for (int i = 0; i < tempList.size(); i++) {
                                mySearchDateList.add(tempList.get(i));
                            }
                        }
                        myAdapter.notifyDataSetChanged();
                        allLoaded = true;
                        dialog.cancel();
                    }
                });

                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initListView();
            }
        });
    }

    /**
     * This function launches the view details of a word and allows to modify it
     * @param position the position in the listView of the word the user want to see more details or to modify
     */
    public void seeWord(int position){
        Intent wordDetailIntent = new Intent(getActivity(), WordActivity.class);

        wordDetailIntent.putExtra(MainActivity.EXTRA_WORD, mySearchDateList.get(position).getWord());
        DictionaryDataModel ddm = new DictionaryDataModel(getActivity());
        wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, ddm.select(mySearchDateList.get(position).getWord().getDictionaryID()));

        startActivity(wordDetailIntent);

        if(historySearch.getText().toString().trim().length() > 0) {
            historySearch.setText("");
        }
    }

    /**
     * This function is used to clear the search history of the app
     */
    private void clearHistory(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(getString(R.string.clearHistory) + " ?");
        alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getActivity(), getString(R.string.historyCleared), Toast.LENGTH_SHORT).show();
                sddm.deleteAll();
                initListView();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * This thread is launch when the user scroll to the end of the list and it load more history
     */
    private Runnable loadMoreHistory = new Runnable(){
        @Override
        public void run() {
            loadingMore = true;
            ArrayList<SearchDate> tempList = new ArrayList<>();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            historyOffset += 10;
            sddm = new SearchDateDataModel(getActivity());
            if(historySearch.getText().toString().length() == 0) {
                tempList = sddm.selectAll(historyLimit, historyOffset);
            }

            actualListSize = mySearchDateList.size();
            for(int i = 0; i < tempList.size(); i++){
                mySearchDateList.add(tempList.get(i));
            }
            getActivity().runOnUiThread(returnRes);
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
            if(actualListSize == mySearchDateList.size()){
                allLoaded = true;
            }
            progressDialog.dismiss();
        }
    };

}

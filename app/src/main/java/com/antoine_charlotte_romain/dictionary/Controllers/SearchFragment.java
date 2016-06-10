package com.antoine_charlotte_romain.dictionary.Controllers;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private View thisView;

    private Dictionary selectedDictionary;

    private EditText beginningText;
    private EditText containsText;
    private EditText endText;
    private EditText targetDictionary;
    private EditText searchIn;
    private RadioGroup radioGroup;
    private RadioButton partWord;
    private RadioButton wholeWord;
    private MenuItem searchTabButton;

    private boolean isReady;

    private String[] searchOptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_search,container,false);

        Intent intent = getActivity().getIntent();
        selectedDictionary = (Dictionary)intent.getSerializableExtra(MainActivity.EXTRA_DICTIONARY);

        beginningText = ((EditText) thisView.findViewById(R.id.beginString));
        containsText = ((EditText) thisView.findViewById(R.id.middleString));
        endText = ((EditText) thisView.findViewById(R.id.endString));
        targetDictionary = ((EditText) thisView.findViewById(R.id.targetDico));
        searchIn = ((EditText) thisView.findViewById(R.id.searchin));
        radioGroup =((RadioGroup) thisView.findViewById(R.id.boutonsradio));
        partWord = ((RadioButton) thisView.findViewById(R.id.part));
        wholeWord = ((RadioButton) thisView.findViewById(R.id.whole));

        searchOptions = new String[4];
        // Fill array with search options
        searchOptions[0] = getString(R.string.headword_only);
        searchOptions[1] = getString(R.string.translation_meaning_only);
        searchOptions[2] = getString(R.string.notes_only);
        searchOptions[3] = getString(R.string.all_data);

        setHasOptionsMenu(true);

        initSearchView();

        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();

        isReady = false;
        if(beginningText.getText().toString().trim().length() > 0 || containsText.getText().toString().trim().length() > 0 || endText.getText().toString().trim().length() > 0) {
            isReady = true;
        }

        if(radioGroup.getCheckedRadioButtonId() == R.id.part){
            // Set hint string of the end string EditText
            endText.setHint(getString(R.string.ends_with));
            // Show all EditText (if they were gone)
            beginningText.setVisibility(View.VISIBLE);
            containsText.setVisibility(View.VISIBLE);
        }
        else {
            // Set hint string of the end string EditText
            endText.setHint(getString(R.string.Word));
            // Hide contain and end EditText (if they were displayed)
            beginningText.setVisibility(View.GONE);
            containsText.setVisibility(View.GONE);
        }
    }

    /**
     * This function creates the buttons on the toolBar
     * @param menu
     * @return
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        menuInflater.inflate(R.menu.menu_search_fragment, menu);
        searchTabButton = menu.findItem(R.id.action_search);

        searchTabButton.setVisible(isReady);

        initTextFields();
    }

    /**
     * This function is called when the user click on a button of the toolBar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_search:
                advancedSearch(thisView);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initSearchView(){
        // set dictionary
        if(selectedDictionary == null) {
            targetDictionary.setText(getString(R.string.target_dico) + " : " + getString(R.string.allDico));
        }
        else {
            targetDictionary.setText(getString(R.string.target_dico) + " : " + selectedDictionary.getTitle());
        }

        // set search option
        searchIn.setText(getString(R.string.search_in) + " : " + searchOptions[0]);

        searchIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySearchOptions(v);
            }
        });

        partWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set hint string of the end string EditText
                endText.setHint(getString(R.string.ends_with));
                // Show all EditText (if they were gone)
                beginningText.setVisibility(View.VISIBLE);
                containsText.setVisibility(View.VISIBLE);
            }
        });

        wholeWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set hint string of the end string EditText
                endText.setHint(getString(R.string.Word));
                // Hide contain and end EditText (if they were displayed)
                beginningText.setVisibility(View.GONE);
                containsText.setVisibility(View.GONE);
            }
        });

        targetDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDictionaries(v);
            }
        });
    }

    private void initTextFields(){
        beginningText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                boolean isReady = beginningText.getText().toString().trim().length() > 0;
                searchTabButton.setVisible(isReady);
            }

        });

        containsText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                boolean isReady = containsText.getText().toString().trim().length() > 0;
                searchTabButton.setVisible(isReady);
            }

        });

        endText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable arg0) {
                boolean isReady = endText.getText().toString().trim().length() > 0;
                searchTabButton.setVisible(isReady);
            }

        });
    }

    /**
     * This function is called when the user click the search option text
     * @param v
     */
    public void displaySearchOptions(View v){
        final String[] display = searchOptions.clone();

        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.search_in)
                .setItems(display, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        searchIn.setText(getString(R.string.search_in) + " : " + searchOptions[which]);
                    }
                })
                .setNegativeButton(R.string.returnString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alert = ad.create();
        alert.show();
    }

    /**
     * This function is called when the user click the dictionary text
     * @param v
     */
    public void displayDictionaries(View v){
        DictionaryDataModel ddm = new DictionaryDataModel(this.getActivity());
        ArrayList<Dictionary> dico = ddm.select();
        String[] nameDico = new String[dico.size()+1];
        nameDico[0] = getString(R.string.allDico);
        for (int i=0; i<dico.size(); i++){
            nameDico[i+1] = dico.get(i).getTitle();
        }
        final String[] names = nameDico.clone();

        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_dictionary)
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        targetDictionary.setText(getString(R.string.target_dico) + " : " + names[which]);
                    }
                })
                .setNegativeButton(R.string.returnString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alert = ad.create();
        alert.show();
    }

    /**
     * This function is called when the user click the search button
     * @param v
     */
    public void advancedSearch(View v){
        Intent intent = new Intent(getActivity(), AdvancedSearchResultActivity.class);

        intent.putExtra(MainActivity.EXTRA_BEGIN_STRING, beginningText.getText().toString());
        intent.putExtra(MainActivity.EXTRA_MIDDLE_STRING, containsText.getText().toString());
        intent.putExtra(MainActivity.EXTRA_END_STRING, endText.getText().toString());

        // Let's see if the search has to be done on part or whole word
        switch (((RadioGroup)thisView.findViewById(R.id.boutonsradio)).getCheckedRadioButtonId()) {
            case R.id.part:
                intent.putExtra(MainActivity.EXTRA_PART_OR_WHOLE, MainActivity.PART_WORD);
                break;
            case R.id.whole:
                intent.putExtra(MainActivity.EXTRA_PART_OR_WHOLE, MainActivity.WHOLE_WORD);
                break;
        }

        // Let's get the chosen search option
        String tmp = searchIn.getText().toString();
        String searchChoice = tmp.replace((getString(R.string.search_in) + " : "), "");

        switch (getSearchChoiceRank(searchChoice)) {
            case 0:
                intent.putExtra(MainActivity.EXTRA_SEARCH_DATA, MainActivity.HEADWORD_ONLY);
                break;
            case 1:
                intent.putExtra(MainActivity.EXTRA_SEARCH_DATA, MainActivity.MEANING_ONLY);
                break;
            case 2:
                intent.putExtra(MainActivity.EXTRA_SEARCH_DATA, MainActivity.NOTES_ONLY);
                break;
            case 3:
                intent.putExtra(MainActivity.EXTRA_SEARCH_DATA, MainActivity.ALL_DATA);
                break;
        }

        // Let's get the targeted dictionary
        tmp = targetDictionary.getText().toString();
        String dico = tmp.replace((getString(R.string.target_dico) + " : "), "");
        intent.putExtra(MainActivity.EXTRA_DICTIONARY, dico);

        startActivity(intent);

        beginningText.setText("");
        containsText.setText("");
        endText.setText("");
    }

    /**
     * This function finds which search option has been chosen
     * @param searchChoice
     *          The string representing the search choice of the user
     * @return
     *          The index number of the search choice in the searchChoice array
     */
    private int getSearchChoiceRank(String searchChoice){
        for (int i=0; i<searchOptions.length; i++){
            if (searchChoice.equals(searchOptions[i])){
                return i;
            }
        }
        return -1;
    }
}

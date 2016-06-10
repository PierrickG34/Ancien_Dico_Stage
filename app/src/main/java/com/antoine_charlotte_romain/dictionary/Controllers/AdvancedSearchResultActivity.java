package com.antoine_charlotte_romain.dictionary.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.Controllers.Adapter.AdvancedSearchResultsAdapter;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel;
import com.antoine_charlotte_romain.dictionary.R;

import java.util.ArrayList;

public class AdvancedSearchResultActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private GridView listResults;

    private ArrayList<Word> results;
    private WordDataModel wdm;
    private AdvancedSearchResultsAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search_result);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data associated to the advanced search
        Intent intent = getIntent();
        if (intent != null){
            String begin = intent.getStringExtra(MainActivity.EXTRA_BEGIN_STRING);
            String middle = intent.getStringExtra(MainActivity.EXTRA_MIDDLE_STRING);
            String end = intent.getStringExtra(MainActivity.EXTRA_END_STRING);
            String searchOption = intent.getStringExtra(MainActivity.EXTRA_SEARCH_DATA);
            String dico = intent.getStringExtra(MainActivity.EXTRA_DICTIONARY);
            String partWhole = intent.getStringExtra(MainActivity.EXTRA_PART_OR_WHOLE);

            // find id of the dictionary
            long id;
            DictionaryDataModel ddm = new DictionaryDataModel(this);
            if(!dico.equals(getString(R.string.allDico))) {
                id = ddm.select(dico).getId();
            } else {
                id = Word.ALL_DICTIONARIES;
            }

            // search
            wdm = new WordDataModel(this);
            if (partWhole.equals(MainActivity.PART_WORD)){
                if(searchOption.equals(MainActivity.HEADWORD_ONLY)){
                    results = wdm.selectHeadword(begin, middle, end, id);
                } else if (searchOption.equals(MainActivity.ALL_DATA)){
                    results = wdm.selectWholeWord(begin, middle, end, id);
                } else if (searchOption.equals(MainActivity.MEANING_ONLY)){
                    results = wdm.selectTranslation(begin, middle, end, id);
                } else if (searchOption.equals(MainActivity.NOTES_ONLY)){
                    results = wdm.selectNote(begin, middle, end, id);
                }
            } else {
                if(searchOption.equals(MainActivity.HEADWORD_ONLY)){
                    results = wdm.selectWholeHeadword(end, id);
                } else if (searchOption.equals(MainActivity.ALL_DATA)){
                    results = wdm.selectWholeAllData(end, id);
                } else if (searchOption.equals(MainActivity.MEANING_ONLY)){
                    results = wdm.selectWholeTranslation(end, id);
                } else if (searchOption.equals(MainActivity.NOTES_ONLY)){
                    results = wdm.selectWholeNote(end, id);
                }
            }

        }

        // Display results
        listResults = (GridView) findViewById(R.id.resultsList);
        if(results.size()>0) {

            myAdapter = new AdvancedSearchResultsAdapter(this, R.layout.row_advanced_search_result, results);

            listResults.setAdapter(myAdapter);

            listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent wordDetailIntent = new Intent(AdvancedSearchResultActivity.this, WordActivity.class);
                    wordDetailIntent.putExtra(MainActivity.EXTRA_WORD, results.get(position));

                    DictionaryDataModel ddm = new DictionaryDataModel(getApplicationContext());
                    wordDetailIntent.putExtra(MainActivity.EXTRA_DICTIONARY, ddm.select(results.get(position).getDictionaryID()));

                    startActivity(wordDetailIntent);
                }
            });
        }

        else {
            LinearLayout advancedSearchLayout = (LinearLayout)findViewById(R.id.advanced_search);

            advancedSearchLayout.removeView(listResults);

            TextView textResult = new TextView(this);
            textResult.setText(getString(R.string.no_result));
            textResult.setGravity(Gravity.CENTER);
            textResult.setPadding(0, 10, 0, 0);
            advancedSearchLayout.addView(textResult);
        }
    }
}

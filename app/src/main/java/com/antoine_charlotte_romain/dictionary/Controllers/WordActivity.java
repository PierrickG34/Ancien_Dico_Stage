package com.antoine_charlotte_romain.dictionary.Controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.Business.SearchDate;
import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot;
import com.antoine_charlotte_romain.dictionary.DataModel.SearchDateDataModel;
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel;
import com.antoine_charlotte_romain.dictionary.R;
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility;


public class WordActivity  extends AppCompatActivity {

    private EditText dictionaryText;
    private EditText headwordText;
    private EditText translationText;
    private EditText noteText;
    private Toolbar toolbar;
    private MenuItem saveButton;
    private Button addButton;

    private RelativeLayout layoutTranslations;
    private FloatingActionButton addTranslationButton;
    private RelativeLayout word_layout;

    private WordDataModel wdm;
    private Word selectedWord;
    private Dictionary selectedDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        selectedWord = (Word)intent.getSerializableExtra(MainActivityKot.Companion.getEXTRA_WORD());
        selectedDictionary = (Dictionary)intent.getSerializableExtra(MainActivityKot.Companion.getEXTRA_DICTIONARY());

        dictionaryText = (EditText) findViewById(R.id.editTextDictionary);
        headwordText = (EditText) findViewById(R.id.editTextHeadword);
        translationText = (EditText) findViewById(R.id.editTextTranslation1);
        noteText = (EditText) findViewById(R.id.editTextNote);
        word_layout = (RelativeLayout) findViewById(R.id.word_layout) ;



        layoutTranslations = (RelativeLayout) findViewById(R.id.layoutTranslations);

//    addTranslationButton = (FloatingActionButton) findViewById(R.id.add_button1);
//    addTranslationButton.setOnClickListener(new OnClickListener() {
//        /** This function is called when the user clicks on the add Button.
//         *  It adds a new EditText unless the number of EditText is superior to 5.
//         */
//
//        @Override
//        public void onClick(View v) {
//            EditText lEditText = new EditText(getApplicationContext());
//            int enfants = layoutTranslations.getChildCount();
//            if(enfants<6) {
//                @IdRes int id = enfants + 1;
//                lEditText.setId(id);
//                RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(headwordText.getWidth(), headwordText.getHeight());
//                lEditText.setTextColor(Color.BLACK);
//                lEditText.getBackground().setColorFilter(Color.parseColor("#6d6d6d"), PorterDuff.Mode.SRC_ATOP);
//
//                if (enfants == 2) {
//                    relativeParams.addRule(RelativeLayout.BELOW, R.id.editTextTranslation1);
//                    lEditText.setHintTextColor(Color.parseColor("#6d6d6d"));
//                    lEditText.setHint(getResources().getString(R.string.translation_children) + " " + enfants);
//
//                } else {
//                    relativeParams.addRule(RelativeLayout.BELOW, layoutTranslations.getChildAt(enfants - 1).getId());
//                    lEditText.setHintTextColor(Color.parseColor("#777777"));
//                    lEditText.setHint(getResources().getString(R.string.translation_children) + " " + enfants);
//                }
//                layoutTranslations.addView(lEditText, relativeParams);
//            } else {
//                Toast.makeText(getApplicationContext(),R.string.maximum_translate, Toast.LENGTH_SHORT).show();
//            }
//
//        }
//    });

        dictionaryText.setEnabled(false);
        dictionaryText.setText(selectedDictionary.getTitle());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setupUI(findViewById(R.id.word_layout));
    }

    public void onClick(View v) {
        int enfants = layoutTranslations.getChildCount();
        System.out.println("enfants - " + enfants);
        EditText lEditText = new EditText(getApplicationContext());
        View removeButton = findViewById(R.id.remove_button1);
        if (enfants == 3) {
            removeButton.setVisibility(View.INVISIBLE);
        }
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(headwordText.getWidth(), headwordText.getHeight());
        switch (v.getId()) {
            case R.id.add_button1: {
                if(enfants<6) {
                    @IdRes int id = enfants + 1;
                    lEditText.setId(id);

                    lEditText.setTextColor(Color.BLACK);
                    lEditText.getBackground().setColorFilter(Color.parseColor("#6d6d6d"), PorterDuff.Mode.SRC_ATOP);

                    if (enfants == 2) {
                        relativeParams.addRule(RelativeLayout.BELOW, R.id.editTextTranslation1);
                        lEditText.setHintTextColor(Color.parseColor("#6d6d6d"));
                        lEditText.setHint(getResources().getString(R.string.translation_children) + " " + enfants);
                        removeButton.setVisibility(View.INVISIBLE);

                    } else {
                        relativeParams.addRule(RelativeLayout.BELOW, layoutTranslations.getChildAt(enfants - 1).getId());
                        lEditText.setHintTextColor(Color.parseColor("#777777"));
                        lEditText.setHint(getResources().getString(R.string.translation_children) + " " + enfants);
                    }
                    layoutTranslations.addView(lEditText, relativeParams);
                    removeButton.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(getApplicationContext(),R.string.maximum_translate, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.remove_button1: {
                @IdRes int id = enfants;
                if (enfants != 2){
                    View test = (View)findViewById(id);
                    ((ViewManager)test.getParent()).removeView(test);
                    test.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    /**
     * This function is called when a child activity back to this view or finish
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(selectedWord != null){
            getMenuInflater().inflate(R.menu.menu_word_details, menu);
            showDetails();
        }
        else{
            getMenuInflater().inflate(R.menu.menu_new_word, menu);
            saveButton = menu.findItem(R.id.action_add_word);
            newWord();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_add_word:
                addWord();
                return true;

            case R.id.action_update_word:
                updateWord();
                return true;

            case R.id.action_delete_word:
                deleteWord(findViewById(R.id.word_layout));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function show the details of a word and allow the user to update or delete it.
     * This function is called after the onCreate if a word was selected by the user.
     */
    private void showDetails(){
        if(!(headwordText.getText().toString().trim().length() > 0)){
            headwordText.setText(selectedWord.getHeadword());
        }
        headwordText.setEnabled(false);
        if(!(translationText.getText().toString().trim().length() > 0)){
            translationText.setText(selectedWord.getTranslation());
        }
        if(!(noteText.getText().toString().trim().length() > 0)){
            noteText.setText(selectedWord.getNote());
        }

        getSupportActionBar().setTitle(getString(R.string.details) + " : " + selectedWord.getHeadword());

        SearchDateDataModel sddm = new SearchDateDataModel(getApplicationContext());
        SearchDate sd = new SearchDate(selectedWord);
        sddm.insert(sd);
    }

    /**
     *  This function allow the user to create a new word.
     *  This function is called after the onCreate if no word was selected by the user.
     */
    private void newWord(){
        boolean isReady = false;
        if(!(headwordText.getText().toString().trim().length() > 0)){
            headwordText.setText("");
        }
        else {
            isReady = true;
        }
        headwordText.setFocusable(true);
        if(!(translationText.getText().toString().trim().length() > 0)){
            translationText.setText("");
        }
        if(!(noteText.getText().toString().trim().length() > 0)){
            noteText.setText("");
        }

        getSupportActionBar().setTitle(R.string.new_word);

        saveButton.setVisible(isReady);

        headwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                boolean isReady = headwordText.getText().toString().trim().length() > 0;
                saveButton.setVisible(isReady);
            }

        });
    }

    /**
     * This function is called on click on the saveButton, it update the selected word with the new values enter by the user.
     */
    private void updateWord(){
        wdm = new WordDataModel(getApplicationContext());
        selectedWord.setTranslation(translationText.getText().toString());
        selectedWord.setNote(noteText.getText().toString());
        wdm.update(selectedWord);

        Toast.makeText(this, selectedWord.getHeadword() + getString(R.string.updated), Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * This function is called on click on the addWordButton, it insert a new word with the values enter by the user.
     */
    private void addWord(){
        wdm = new WordDataModel(getApplicationContext());
        Word w = new Word();
        w.setDictionaryID(selectedDictionary.getId());
        w.setHeadword(headwordText.getText().toString());
        w.setTranslation(translationText.getText().toString());
        w.setNote(noteText.getText().toString());
        int i = wdm.insert(w);

        switch (i){
            case 0:
                Toast.makeText(this, w.getHeadword() + getString(R.string.created), Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            case 1:
                Toast.makeText(this, getString(R.string.error) + " : " + w.getHeadword() + " " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, getString(R.string.error) + " " + getString(R.string.dico_not_exists), Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, getString(R.string.error) + " " + getString(R.string.no_selected_dico), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * This function is called on click on the deleteButton, it asks for a confirmation and then delete the selected word.
     * @param view the view which launched this function
     */
    public void deleteWord(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.delete_word) + " ?");
        alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), selectedWord.getHeadword() + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                wdm = new WordDataModel(getApplicationContext());
                wdm.delete(selectedWord.getId());
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * This function is used to hide the keyBoard on click outside an editText
     * @param view the view which launched this function
     */
    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    KeyboardUtility.hideSoftKeyboard(WordActivity.this);
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

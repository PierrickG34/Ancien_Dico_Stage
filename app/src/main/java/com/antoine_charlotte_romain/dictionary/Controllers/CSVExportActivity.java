package com.antoine_charlotte_romain.dictionary.Controllers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel;
import com.antoine_charlotte_romain.dictionary.R;
import com.antoine_charlotte_romain.dictionary.Utilities.KeyboardUtility;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;


public class CSVExportActivity extends AppCompatActivity {


    private final int REQUEST_DIRECTORY = 0;

    private Toolbar toolbar;

    /**
     * Used to enter the name of the exported file
     */
    private EditText fileName;

    /**
     * Used to choose the directory on the device where the file will be exported
     */
    private EditText directory;

    /**
     * The dictionary which will be exported
     */
    private Dictionary dictionary;

    /**
     * Used to show the advancement of the export
     */
    private ProgressDialog progress;

    /**
     * The dictionary choosed by the user
     */
    private String selectedDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csvexport);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fileName = (EditText) findViewById(R.id.editTextFile);
        selectedDirectory = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name);

        //Creating a new file named like the application if not exists
        File initialDir = new File (selectedDirectory);
        initialDir.mkdir();

        directory = (EditText) findViewById(R.id.editTextDirectory);
        directory.setText(selectedDirectory);
        directory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDirectory();
            }
        });

        // Getting the dictionary to export
        Intent intent = getIntent();
        if (intent != null){
            dictionary = (Dictionary)intent.getSerializableExtra(MainActivity.EXTRA_DICTIONARY);

            fileName.setText(dictionary.getTitle() + ".csv");
            fileName.setSelection(0,dictionary.getTitle().length());
            getSupportActionBar().setTitle(getString(R.string.exporting) + " " +  dictionary.getTitle());
        }


        //Setting the EditText to always have the suffix .csv
        fileName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().endsWith(".csv")){
                    fileName.setText(".csv");
                    fileName.setSelection(0);
                }

            }
        });

        //Displaying the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        setupUI(findViewById(R.id.export_layout));
    }

    /**
     * Method called when the user click on the directory EditText
     */
    private void chooseDirectory()
    {
        final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
                "DirChooserSample");

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY,
                selectedDirectory);

        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                selectedDirectory = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                directory.setText(selectedDirectory);
            } else {
                // Nothing selected
            }
        }
    }

    /**
     * Method called when the user click on the export button
     * @param v
     */
    public void export(View v)
    {
        //Creating the file
        final File file = new File(selectedDirectory + "/" + fileName.getText().toString());

        //If the file doesn't already exists it is exported
        if (!file.exists())
        {
            exportCSV(file);

        }
        //Else the user can overwrite the existing file
        else
        {
            new AlertDialog.Builder(CSVExportActivity.this)
                    .setTitle(R.string.file_already_exists)
                    .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            exportCSV(file);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


    }

    /**
     * Method which create a CSV file containing the word list of the current dictionary.
     * If the file already exists it is overwritted.
     *
     * @param file Exported .csv file
     */
    private void exportCSV(final File file)
    {
        final WordDataModel wdm = new WordDataModel(this);

        //Initialising the progressBar
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.export_progress));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false);
        progress.show();

        //Handling the end of the export
        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                progress.dismiss();
                new AlertDialog.Builder(CSVExportActivity.this)
                        .setTitle(R.string.success)
                        .setMessage(R.string.dictionary_exported)
                        .setNegativeButton(R.string.returnString, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.open_it, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent viewDoc = new Intent(Intent.ACTION_VIEW);
                                viewDoc.setDataAndType(
                                        Uri.fromFile(file),
                                        "text/csv");
                                try {
                                    startActivity(viewDoc);
                                } catch (ActivityNotFoundException e) {
                                    Snackbar.make(findViewById(R.id.export_layout), getString(R.string.no_apps), Snackbar.LENGTH_LONG).setAction(R.string.close_button, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).show();

                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        };

        final Thread t = new Thread() {
            @Override
            public void run() {
                //Selecting the words
                List<Word> words = wdm.selectAll(dictionary.getId());
                progress.setMax(words.size());

                //Initialising the variables
                BufferedWriter bw;
                String comma = ",";
                String headword = "";
                String translation = "";
                String note = "";

                try
                {
                    file.createNewFile();
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file, false));
                    // ISO-8859-1 interprets accents correctly
                    bw = new BufferedWriter(new OutputStreamWriter(os, "ISO-8859-1"));

                    // For each word in the dictionary
                    for (Word w : words){

                        headword = filterComma(w.getHeadword());
                        translation = filterComma(w.getTranslation());
                        note = filterComma(w.getNote());

                        bw.write(headword + comma + translation + comma + note);
                        bw.newLine();
                        progress.incrementProgressBy(1);
                    }
                    bw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //Sending a message to the handler to notify that the thread is done
                handler.sendEmptyMessage(0);
            }
        };
        t.start();

    }

    private String filterComma (String stringToFilter){
        String result = stringToFilter;
        if (stringToFilter.contains(",")){
            result.replace(',',';');
        }
        return result;
    }


    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    KeyboardUtility.hideSoftKeyboard(CSVExportActivity.this);
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


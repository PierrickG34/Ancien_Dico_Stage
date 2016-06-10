package com.antoine_charlotte_romain.dictionary.Controllers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.antoine_charlotte_romain.dictionary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Activity enabling the user to choose the application language
 */
public class LanguageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display results
        languages = (ListView) findViewById(R.id.languagesList);

        ArrayList<String> al = new ArrayList<>();
        al.add("English");
        al.add("Français");

        ArrayList<String> correspondingCode = new ArrayList<>();
        correspondingCode.add("en");
        correspondingCode.add("fr");

        List<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> element;

        for (int i = 0; i < al.size(); i++) {
            // we add each language of the previous list in this new list
            element = new HashMap<>();
            element.put("language", al.get(i));
            element.put("code", correspondingCode.get(i));
            list.add(element);
        }

        ListAdapter adapter = new SimpleAdapter(this,
                list,
                android.R.layout.simple_list_item_1,
                new String[]{"language"},
                new int[]{android.R.id.text1});

        // Give ListView to the SimpleAdapter
        languages.setAdapter(adapter);

        languages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);

                // Change application language with the selected language
                Locale locale = new Locale(item.get("code"));
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                getApplicationContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                // Come back to the Home
                Intent refresh = new Intent(LanguageActivity.this, MainActivity.class);
                startActivity(refresh);
            }
        });
    }
}

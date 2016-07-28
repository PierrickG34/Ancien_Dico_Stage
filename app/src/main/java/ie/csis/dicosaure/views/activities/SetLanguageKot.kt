package ie.csis.dicosaure.views.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ListView
import android.widget.SimpleAdapter
import ie.csis.dicosaure.views.R
import ie.csis.dicosaure.views.adapters.ItemClickListenerLanguage
import java.util.*

/**
 * Created by dineen on 16/06/2016.
 */
class SetLanguageKot : AppCompatActivity() {

    var toolbar: Toolbar? = null
    var languages: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = findViewById(R.id.tool_bar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.title_activity_language)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Display results
        languages = findViewById(R.id.languagesList) as ListView?

        val al = ArrayList<String>()
        al.add("English")
        al.add("Français")

        val correspondingCode = ArrayList<String>()
        correspondingCode.add("en")
        correspondingCode.add("fr")

        val list = ArrayList<HashMap<String, String>>()
        var element: HashMap<String, String>

        for (i in al.indices) {
            // we add each language of the previous list in this new list
            element = HashMap<String, String>()
            element.put("language", al[i])
            element.put("code", correspondingCode[i])
            list.add(element)
        }

        val adapter = SimpleAdapter(this,
                list,
                android.R.layout.simple_list_item_1,
                arrayOf("language"),
                intArrayOf(android.R.id.text1))

        // Give ListView to the SimpleAdapter
        languages!!.adapter = adapter
        languages!!.setOnItemClickListener(ItemClickListenerLanguage(this.baseContext, this.applicationContext, this))
    }

    //Refresh the view and open main activity
    fun startMainActivity() {
        val refresh = Intent(this.getApplicationContext(), MainActivity::class.java)
        this.startActivity(refresh)
    }
}
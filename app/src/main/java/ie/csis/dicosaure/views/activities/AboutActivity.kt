package ie.csis.dicosaure.views.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import ie.csis.dicosaure.Controllers.R

/**
 * Created by dineen on 14/06/2016.
 */

class AboutActivityKot : AppCompatActivity() {

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = findViewById(R.id.tool_bar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}

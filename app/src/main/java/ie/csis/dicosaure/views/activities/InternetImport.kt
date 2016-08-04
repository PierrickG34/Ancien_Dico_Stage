package ie.csis.dicosaure.views.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.NotificationCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import ie.csis.dicosaure.model.dictionary.Dictionary
import ie.csis.dicosaure.model.dictionary.DictionarySQLITE
import ie.csis.dicosaure.views.R
import ie.csis.dicosaure.views.csv.CSVImport
import org.jetbrains.anko.ctx
import org.json.JSONObject
import java.net.URL
import java.util.*

/**
 * Created by dineen on 28/07/2016.
 */
class InternetImport() : AppCompatActivity() {

    companion object {
        val APIURL = "http://dicosaure.granetlucas.fr/api/"
        val TASK_GET_ALL_DICO = 1
        val TASK_GET_ONE_DICO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.import_internet)

        //Set the toolbar on the view
        var toolbar = super.findViewById(R.id.tool_bar) as Toolbar
        super.setSupportActionBar(toolbar)
        this.supportActionBar!!.setTitle(R.string.download_dictionary)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        this.tryConnection(null)
    }

    fun launchConnection() {
        HTTPAsyncTask(this, TASK_GET_ALL_DICO).execute(URL(APIURL + "getlist"))
    }

    fun tryConnection(view: View? ) {
        if (this.isConnected()) {
            this.startSpinner(this.getString(R.string.loading))
            this.findViewById(R.id.connection_error_layout).visibility = View.INVISIBLE
            this.launchConnection()
        }
        else {
            this.stopSpinner()
            this.findViewById(R.id.connection_error_layout).visibility = View.VISIBLE
        }
    }

    fun isConnected() : Boolean {
        val connectingServiceNetwork = this.ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectingServiceNetwork.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    fun initListView(APIResult : String) {
        val dictionaries = JSONObject(APIResult).getJSONArray("list")
        var listDictionary = ArrayList<Dictionary>()
        var dictionary : Dictionary
        var rowJSON : JSONObject
        var i = 0
        while (i < dictionaries.length()) {
            rowJSON = dictionaries.getJSONObject(i)
            dictionary = Dictionary(inLang = rowJSON.getString("inLang"), outLang = rowJSON.getString("outLang"), id = rowJSON.getString("id"))
            listDictionary.add(dictionary)
            i++
        }
        val listDicoView = this.findViewById(R.id.listView) as ListView
        listDicoView.setOnItemClickListener { adapterView, view, i, l ->
            if (isConnected()) {
                val dic = adapterView.getItemAtPosition(i) as Dictionary
                this.startSpinner(this.getString(R.string.downloading))
                HTTPAsyncTask(this, TASK_GET_ONE_DICO).execute(URL(APIURL + "getdico/" + dic.idDictionary))
            }
            else {
                this.findViewById(R.id.connection_error_layout).visibility = View.VISIBLE
            }
        }
        listDicoView.adapter = ArrayAdapter<Dictionary>(this, android.R.layout.simple_list_item_1, listDictionary)
    }

    fun startSpinner(txtSpinner : String) {
        val spinner = this.findViewById(R.id.spinner_layout)
        spinner.visibility = View.VISIBLE
        (this.findViewById(R.id.text_load) as TextView).setText(txtSpinner)
    }

    fun stopSpinner() {
        val spinner = this.findViewById(R.id.spinner_layout)
        spinner.visibility = View.INVISIBLE
    }

    fun downloadDictionary(APIResult : String) {
        val json = JSONObject(APIResult)
        val csv = json.getString("content")
        val dtm = DictionarySQLITE(this, json.getString("inLang"), json.getString("outLang"))

        val handler = Handler(object : Handler.Callback {
            override fun handleMessage(p0: Message?): Boolean {
                val intent = Intent(ctx, ListWordsActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_DICTIONARY, dtm)
                intent.putExtra(MainActivity.EXTRA_RENAME, true)
                startActivity(intent)
                return false
            }
        })

        Thread(object : Runnable {
            override fun run() {
                var notification = NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Dicosaure")
                        .setContentText(getString(R.string.importing))
                        .setProgress(0,0,true)

                val mNotificationId = 1
                val mNotifyMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                mNotifyMgr.notify(mNotificationId, notification.build())

                if (dtm.save() < 0) {
                    dtm.readByInLangOutLang()
                }
                val import = CSVImport()
                import.importCSV(dtm, ctx, null, csv)
                mNotifyMgr.cancel(mNotificationId)

                notification = NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Dicosaure")
                        .setContentText(getString(R.string.end_import))
                mNotifyMgr.notify(mNotificationId, notification.build())

                handler.sendEmptyMessage(0)
            }
        }).start()
    }

    class HTTPAsyncTask(view : InternetImport, taskID : Int ) : AsyncTask<URL, Integer, Long>() {

        var result : String? = null
        var view : InternetImport = view
        val taskID : Int = taskID

        override fun doInBackground(vararg urls: URL?): Long? {
            this.result = urls[0]!!.readText()
            return 0
        }

        override fun onPostExecute(result: Long?) {
            if (this.taskID == TASK_GET_ALL_DICO) {
                this.view.initListView(this.result!!)
            }
            else if (this.taskID == TASK_GET_ONE_DICO) {
                this.view.downloadDictionary(this.result!!)
            }
            this.view.stopSpinner()
        }
    }
}

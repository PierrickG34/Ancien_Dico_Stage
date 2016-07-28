package ie.csis.dicosaure.views.activities

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import ie.csis.dicosaure.views.R
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import java.io.InputStream
import java.net.URI
import java.net.URL

/**
 * Created by dineen on 28/07/2016.
 */
class InternetImport() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.import_internet)
        HTTPAsyncTask(this).execute(URL("http://dicosaure.granetlucas.fr/api/getlist.json"))
    }

    fun setTextView(txt : String) {
        (this.findViewById(R.id.test) as TextView).text = txt
    }

    class HTTPAsyncTask(view : InternetImport) : AsyncTask<URL, Integer, Long>() {

        var result : String? = null
        var view : InternetImport = view

        override fun doInBackground(vararg urls: URL?): Long? {
            this.result = urls[0]!!.readText()
            return 0
        }

        override fun onPostExecute(result: Long?) {
            this.view.setTextView(this.result!!)
        }

        fun GET(url : String) : String? {
            var inputStream : InputStream? = null
            var result : String? = null
            try {
                val httpClient = HttpClientBuilder.create().build()
                val httpRequest = HttpGetHC4(url)
                val httpResponse = httpClient.execute(httpRequest)

                // receive response as inputStream
                inputStream = httpResponse.entity.content

                // convert inputstream to string
                if(inputStream != null) {
                    result = IOUtils.toString(inputStream, "utf-8");
                    println(result)
                }
                else {
                    result = "Did not work!";
                }
            }
            catch (e : Exception) {
                Log.d("InputStream", result)
            }
            return result
        }
    }

}

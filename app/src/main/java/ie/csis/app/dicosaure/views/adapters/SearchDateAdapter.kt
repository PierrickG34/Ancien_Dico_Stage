package ie.csis.app.dicosaure.views.adapters

//import com.antoine_charlotte_romain.dictionary.business.old.SearchDate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ie.csis.app.dicosaure.model.word.Word
import ie.csis.app.dicosaure.views.R

/**
 * Created by summer1 on 30/06/2015.
 */
class SearchDateAdapter(layoutResourceId: Int?, asd: List<Word>?, context: Context?, resource: Int, objects: MutableList<Word>?) : ArrayAdapter<Word>(context, resource, objects) {

    var layoutResourceId: Int? = layoutResourceId
    var asd: List<Word>? = asd

    /**
     * This method returns the count of words
     * @return the count
     */
    override fun getCount(): Int {
        return this.asd!!.size
    }

    /**
     * This method returns the word at the wanted position
     * @param position the position of the word you want
     * @return the word
     */
    override fun getItem(position: Int) : Word {
        return this.asd!!.get(position)
    }

    /**
     * This method returns the id at the wanted position
     * @param position the position of the id you want
     * @return the id
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * This function is used to show the word in the listView each word in a custom layout "row_word"
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        // Get the data item for this position
        val sd = getItem(position)

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResourceId!!, parent, false)
        }

        // Lookup view for data population
        val textHeadword = convertView!!.findViewById(R.id.textHeadword) as TextView
        val textDate = convertView.findViewById(R.id.textDate) as TextView

        // Populate the data into the template view using the data object
        textHeadword.setText(sd.headword)
        //textHeadword.setText(sd.getWord().getHeadword())
        textDate.setText(sd.dateView.toString())
//        textDate.setText(sd.getDate())

        // Return the completed view to render on screen
        return convertView
    }
}

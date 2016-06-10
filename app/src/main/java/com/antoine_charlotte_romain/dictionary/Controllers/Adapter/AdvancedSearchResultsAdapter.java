package com.antoine_charlotte_romain.dictionary.Controllers.Adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.antoine_charlotte_romain.dictionary.Business.Word;
import com.antoine_charlotte_romain.dictionary.DataModel.DictionaryDataModel;
import com.antoine_charlotte_romain.dictionary.R;

import java.util.ArrayList;

public class AdvancedSearchResultsAdapter extends ArrayAdapter<Word> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Word> aw = null;

    /**
     * This function creates a custom ArrayAdapter of words
     * @param context the context of the application
     * @param resource the layout to inflate
     * @param data the ArrayList of words
     */
    public AdvancedSearchResultsAdapter(Context context, int resource, ArrayList<Word> data) {
        super(context, resource, data);
        this.layoutResourceId = resource;
        this.context = context;
        this.aw = data;
    }

    @Override
    public int getCount() {

        return aw.size();
    }

    @Override
    public Word getItem(int position) {

        return aw.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    /**
     * This function is used to show the word in the listView each word in a custom layout
     * @param position the position of the item the user is interacting with
     * @param convertView the rowView
     * @param parent the listView
     * @return the rowView completed
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        final Word word = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false);
        }

        // Lookup view for data population
        TextView mainItem = (TextView) convertView.findViewById(R.id.textHeader);
        TextView subItem = (TextView) convertView.findViewById(R.id.textSub);

        // Populate the data into the template view using the data object
        mainItem.setText(word.getHeadword());
        subItem.setText(word.getTranslation());

        // Return the completed view to render on screen
        return convertView;
    }
}

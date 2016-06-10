package com.antoine_charlotte_romain.dictionary.Controllers.Adapter;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import com.antoine_charlotte_romain.dictionary.R;

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

import java.util.ArrayList;

public class DictionaryAdapter extends ArrayAdapter<Dictionary>{

    private Context context;
    private int layoutResourceId;
    private ArrayList<Dictionary> data = null;
    private ArrayList<Dictionary> deleteList = new ArrayList<>();
    private DictionaryAdapterCallback callback;
    private boolean all_selected;

    public DictionaryAdapter(Context context, int layoutResourceId, ArrayList<Dictionary> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.all_selected = false;
    }

    public ArrayList<Dictionary> getDeleteList() {
        return deleteList;
    }

    public boolean isAll_selected() {
        return all_selected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Dictionary dictionary = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.dictionary_title);
        // Populate the data into the template view using the data object
        title.setText(dictionary.getTitle());

        //configuring more_button if exists
        if(layoutResourceId == R.layout.dictionary_row)
        {
            ImageButton menuButton = (ImageButton) convertView.findViewById(R.id.dico_more_button);
            menuButton.setColorFilter(R.color.textColor, PorterDuff.Mode.MULTIPLY);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    callback.read(position);
                }
            });
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.dico_more_button:
                            PopupMenu popup = new PopupMenu(context, v);
                            popup.getMenuInflater().inflate(R.menu.context_menu_dictionary, popup.getMenu());
                            popup.show();
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.open:
                                            callback.read(position);
                                            break;

                                        case R.id.rename:
                                            callback.update(position);
                                            break;

                                        case R.id.delete:
                                            callback.delete(position);
                                            break;

                                        case R.id.export:
                                            callback.export(position);
                                            break;
                                        default:
                                            break;
                                    }
                                    return true;
                                }
                            });
                            break;

                        default:
                            break;
                    }
                }
            });
        }
        //Configuring the checkbox if exists
        else if(layoutResourceId == R.layout.delete_dictionary_row)
        {
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.delete_box);

            checkBox.setChecked(deleteList.contains(dictionary));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        addToDeleteList(dictionary);
                    } else {
                        removeFromDeleteList(dictionary);
                    }
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if(checkBox.isChecked())
                    {
                        checkBox.setChecked(false);
                        removeFromDeleteList(dictionary);
                    }
                    else
                    {
                        checkBox.setChecked(true);
                        addToDeleteList(dictionary);
                    }
                }
            });
        }

        // Return the completed view to render on screen
        return convertView;
    }

    private void addToDeleteList(Dictionary d)
    {
        if(!deleteList.contains(d)) {
            deleteList.add(d);
            callback.notifyDeleteListChanged();
        }
    }

    private void removeFromDeleteList(Dictionary d)
    {
        deleteList.remove(d);
        all_selected = false;
        callback.notifyDeleteListChanged();
    }

    public void selectAll()
    {
        all_selected = !all_selected;
        if(all_selected)
        {
            for (int i = 0; i < data.size(); i++) {
                addToDeleteList(data.get(i));
            }
        }
        else {
            deleteList.clear();
            callback.notifyDeleteListChanged();
        }

        notifyDataSetChanged();
    }

    public void setCallback(DictionaryAdapterCallback callback){
        this.callback = callback;
    }

    public interface DictionaryAdapterCallback {
        void delete(int position);
        void update(int position);
        void read(int position);
        void export(int position);
        void notifyDeleteListChanged();
    }

}

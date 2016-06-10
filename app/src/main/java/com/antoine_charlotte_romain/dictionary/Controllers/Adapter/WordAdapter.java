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

public class WordAdapter extends ArrayAdapter<Word> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Word> aw = null;
    private ArrayList<Word> deleteList = new ArrayList<>();
    private boolean selectedDictionary;
    private boolean allSelected;
    private WordAdapterCallback callback;

    /**
     * This function creates a custom ArrayAdapter of words
     * @param context the context of the application
     * @param resource the layout to inflate
     * @param data the ArrayList of words
     * @param selectedDictionary if true the row will show the headWord and its translation else it will show the headWord and the dictionary title instead
     */
    public WordAdapter(Context context, int resource, ArrayList<Word> data, boolean selectedDictionary) {
        super(context, resource, data);
        this.layoutResourceId = resource;
        this.context = context;
        this.aw = data;
        this.selectedDictionary = selectedDictionary;
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

    public ArrayList<Word> getDeleteList() {
        return deleteList;
    }

    public boolean isAllSelected() {
        return allSelected;
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
        if (selectedDictionary) {
            subItem.setText(word.getTranslation());
        } else {
            DictionaryDataModel ddm = new DictionaryDataModel(context);
            subItem.setText((ddm.select(word.getDictionaryID())).getTitle());
        }

        if(layoutResourceId == R.layout.row_word) {
            ImageButton menuButton = (ImageButton) convertView.findViewById(R.id.imageButtonWord);
            menuButton.setColorFilter(R.color.textColor, PorterDuff.Mode.MULTIPLY);
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!callback.getOpen()) {
                        switch (v.getId()) {
                            case R.id.imageButtonWord:
                                PopupMenu popup = new PopupMenu(context, v);
                                popup.getMenuInflater().inflate(R.menu.context_menu_word, popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.modify:
                                                callback.modifyPressed(position);
                                                break;

                                            case R.id.delete:
                                                callback.deletePressed(position);
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
                    } else {
                        callback.showFloatingMenu(v);
                    }
                }
            });
        }
        else if(layoutResourceId == R.layout.row_delete_word)
        {
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.deleteWordBox);

            checkBox.setChecked(deleteList.contains(word));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        addToDeleteList(word);
                    } else {
                        removeFromDeleteList(word);
                    }
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        removeFromDeleteList(word);
                    } else {
                        checkBox.setChecked(true);
                        addToDeleteList(word);
                    }
                }
            });
        }

        // Return the completed view to render on screen
        return convertView;
    }

    private void addToDeleteList(Word w)
    {
        if(!deleteList.contains(w)) {
            deleteList.add(w);
            callback.notifyDeleteListChanged();
        }
    }

    private void removeFromDeleteList(Word w)
    {
        deleteList.remove(w);
        allSelected = false;
        callback.notifyDeleteListChanged();
    }

    public void setCallback(WordAdapterCallback callback){
        this.callback = callback;
    }

    public interface WordAdapterCallback {
        void deletePressed(int position);
        void modifyPressed(int position);
        boolean getOpen();
        void showFloatingMenu(View v);
        void notifyDeleteListChanged();
    }

}

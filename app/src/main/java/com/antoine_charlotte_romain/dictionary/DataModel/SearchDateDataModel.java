package com.antoine_charlotte_romain.dictionary.DataModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.antoine_charlotte_romain.dictionary.Business.SearchDate;
import com.antoine_charlotte_romain.dictionary.Business.Word;

import java.util.ArrayList;

/**
 * Created by summer1 on 08/07/2015.
 */
public class SearchDateDataModel extends DAOBase{

    public static final String SQL_CREATE_SEARCH_DATE =
            "CREATE TABLE " + SearchDateEntry.TABLE_NAME + " (" +
                    SearchDateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SearchDateEntry.COLUMN_NAME_WORD_ID + " INTEGER NOT NULL, " +
                    SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " DEFAULT CURRENT_TIMESTAMP, "+
                    "FOREIGN KEY (" + SearchDateEntry.COLUMN_NAME_WORD_ID + ") REFERENCES " + WordDataModel.WordEntry.TABLE_NAME + " (" + WordDataModel.WordEntry._ID + ")" +
                    ");";

    public static final String SQL_DELETE_SEARCH_DATE= "DROP TABLE IF EXISTS " + SearchDateEntry.TABLE_NAME + ";";

    public static abstract class SearchDateEntry implements BaseColumns {
        public static final String TABLE_NAME = "searchDate";
        public static final String COLUMN_NAME_WORD_ID = "wordID";
        public static final String COLUMN_NAME_SEARCH_DATE= "dateOfSearch";
    }

    private static final String SQL_SELECT_SEARCH_DATE_FROM_ID = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " WHERE " + SearchDateEntry._ID + " = ?;";

    private static final String SQL_WORD_ADDED_IN_THE_HOUR = "SELECT * FROM " + SearchDateEntry.TABLE_NAME +
            " WHERE " + SearchDateEntry.COLUMN_NAME_WORD_ID + " = ?" +
            " AND " + "(strftime('%s', CURRENT_TIMESTAMP) - strftime('%s', " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + "))<3600;";

    private static final String SQL_SELECT_SEARCH_DATE_FROM_WORD_OR_DATE = "SELECT sd." + SearchDateEntry._ID + " FROM " + SearchDateEntry.TABLE_NAME +
            " sd INNER JOIN " + WordDataModel.WordEntry.TABLE_NAME + " w ON sd." + SearchDateEntry.COLUMN_NAME_WORD_ID + "=w." + WordDataModel.WordEntry._ID +
            " WHERE w." + WordDataModel.WordEntry.COLUMN_NAME_HEADWORD + " LIKE ?" +
            " OR sd." + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " LIKE ? ORDER BY " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " DESC;";

    private static final String SQL_SELECT_SEARCH_DATE_BEFORE = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " WHERE " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " < ?;";

    private static final String SQL_SELECT_SEARCH_DATE_AFTER = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " WHERE " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " > ?;";

    private static final String SQL_SELECT_SEARCH_DATE_BETWEEN = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " WHERE " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " < ?"
            + " AND " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " > ?;";

    private static final String SQL_SELECT_ALL_SEARCH_DATE = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " ORDER BY " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " DESC" +
           " LIMIT ? OFFSET ?;";

    public SearchDateDataModel(Context context){
        super(context);
    }

    /**
     * Insert a searchDate of a word in the database
     * @param sd the searchDate to insert
     * @return 0 if it's a success, 1 if there were a previous search in this word less than an hour ago and 2 if the word searched doesn't exists
     */
    public int insert(SearchDate sd){
        // Gets the data repository in write mode
        SQLiteDatabase db = open();

        WordDataModel wdm = new WordDataModel(context);
        Word w = wdm.select(sd.getWord().getId());
        if(w != null) {
            if (!alreadyAdded(sd.getWord())) {
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(SearchDateEntry.COLUMN_NAME_WORD_ID, sd.getWord().getId());

                // Insert the new row, returning the primary key value of the new row
                long newSearchDateID = db.insert(SearchDateEntry.TABLE_NAME, null, values);

                sd.setId(newSearchDateID);
                return 0;
            }
            return 1;
        }
        return 2;
    }

    /**
     * Select a searchDate by its id in the database
     * @param id the id of the searchDate we are wanted to find
     * @return the searchDate if it exists
     */
    public SearchDate select(long id){
        SQLiteDatabase db = open();

        Cursor c = db.rawQuery(SQL_SELECT_SEARCH_DATE_FROM_ID, new String[]{String.valueOf(id)});

        SearchDate sd;
        if(c.getCount() == 1) {
            c.moveToFirst();
            long wordID = c.getLong(c.getColumnIndexOrThrow(SearchDateEntry.COLUMN_NAME_WORD_ID));
            String date = c.getString(c.getColumnIndexOrThrow(SearchDateEntry.COLUMN_NAME_SEARCH_DATE));

            WordDataModel wdm = new WordDataModel(context);
            Word w = wdm.select(wordID);

            sd = new SearchDate(w, date);
        }
        else {
            sd = null;
        }

        c.close();
        return sd;
    }

    /**
     * Select all the searchDate where the headword starts with the string in param or the date contains this string
     * @param search the string in which we are wanted to find
     * @return all the searchDate in which the headword starts with the search string or the date contains this search string
     */
    public ArrayList<SearchDate> select(String search){
        SQLiteDatabase db = open();

        Cursor c = db.rawQuery(SQL_SELECT_SEARCH_DATE_FROM_WORD_OR_DATE, new String[]{String.valueOf(search) + "%", "%" + String.valueOf(search) + "%"});

        ArrayList<SearchDate> listDate = new ArrayList<>();
        while (c.moveToNext()) {
            SearchDate sd = select(c.getLong(c.getColumnIndexOrThrow(SearchDateEntry._ID)));
            listDate.add(sd);
        }
        c.close();

        return listDate;
    }

    /**
     * Select all the searchDate registered before the first date and after the second one
     * @param before the date we are wanted to find the history before
     * @param after the date we are wanted to find the history after
     * @return all the searchDate before the first string, after the second string or (if none is null) between the 2 strings
     */
    public ArrayList<SearchDate> select(String before, String after){
        SQLiteDatabase db = open();

        Cursor c;
        if(!before.isEmpty()) {
            // Search between two dates
            if(!after.isEmpty()) {
                c  = db.rawQuery(SQL_SELECT_SEARCH_DATE_BETWEEN, new String[]{String.valueOf(before), String.valueOf(after)});
            }
            // Search before only
            else {
                c  = db.rawQuery(SQL_SELECT_SEARCH_DATE_BEFORE, new String[]{String.valueOf(before)});
            }
        }
        // Search after only
        else if(!after.isEmpty()) {
            c  = db.rawQuery(SQL_SELECT_SEARCH_DATE_AFTER, new String[]{String.valueOf(after)});
        }
        // If the two strings are null we return null
        else {
            return null;
        }

        ArrayList<SearchDate> listDate = new ArrayList<>();
        while (c.moveToNext()) {
            SearchDate sd = select(c.getLong(c.getColumnIndexOrThrow(SearchDateEntry._ID)));
            listDate.add(sd);
        }
        c.close();

        return listDate;
    }

    /**
     * This function is used to know if a word was already added in the history less than an hour ago
     * @param w the word we are wanted to know if it was added recently
     * @return true if the word was added recently, false if not
     */
    public boolean alreadyAdded(Word w){
        SQLiteDatabase db = open();

        Cursor c = db.rawQuery(SQL_WORD_ADDED_IN_THE_HOUR, new String[]{String.valueOf(w.getId())});

        if(c.getCount() == 0){
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Select all the searchDate of the database ORDER_BY the date DESC
     * @param limit the limit of row to return
     * @param offset the first row to return
     * @return all the selected searchDate
     */
    public ArrayList<SearchDate> selectAll(int limit, int offset){
        SQLiteDatabase db = open();

        Cursor c = db.rawQuery(SQL_SELECT_ALL_SEARCH_DATE, new String[]{String.valueOf(limit), String.valueOf(offset)});

        ArrayList<SearchDate> listDate = new ArrayList<>();
        while(c.moveToNext()) {
            SearchDate sd = select(c.getLong(c.getColumnIndexOrThrow(SearchDateEntry._ID)));
            listDate.add(sd);
        }

        return listDate;
    }

    /**
     * Delete a row in the searchDate database
     * @param id the id of the searchDate we want to delete
     */
    public void delete(long id){
        // Gets the data repository in write mode
        SQLiteDatabase db = open();

        // Define 'where' part of query.
        String selection = SearchDateEntry._ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };

        // Issue SQL statement.
        db.delete(SearchDateEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Delete the search history of a word
     * @param id the id of the word we want to clear the history
     */
    public void deleteAll(long id){
        // Gets the data repository in write mode
        SQLiteDatabase db = open();

        // Define 'where' part of query.
        String selection = SearchDateEntry.COLUMN_NAME_WORD_ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(id)};

        // Issue SQL statement.
        db.delete(SearchDateEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Delete all the row of the searchDate table in the database
     */
    public void deleteAll(){
        SQLiteDatabase db = open();

        db.delete(SearchDateEntry.TABLE_NAME, null, null);
    }
}

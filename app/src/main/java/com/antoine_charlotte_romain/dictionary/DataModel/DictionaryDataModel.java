package com.antoine_charlotte_romain.dictionary.DataModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;
import java.util.ArrayList;


/**
 * Created by summer1 on 22/06/2015.
 * Updated by summer3 on 22/06/2015.
 */
public class DictionaryDataModel extends DAOBase{

    public static final String SQL_CREATE_DICTIONARY =
            "CREATE TABLE " + DictionaryEntry.TABLE_NAME + " (" +
                    DictionaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DictionaryEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL"
                    + ");";

    public static final String SQL_DELETE_DICTIONARY = "DROP TABLE IF EXISTS " + DictionaryEntry.TABLE_NAME + ";";

    public static abstract class DictionaryEntry implements BaseColumns {
        public static final String TABLE_NAME = "dictionary";
        public static final String COLUMN_NAME_TITLE = "title";
    }

    public DictionaryDataModel(Context context){
        super(context);
    }

    /**
     * Insert a dictionary in the DataBase
     *
     * @param d
     *          The dictionary to insert
     * @return 1 if the dictionary was inserted
     *         0 if the dictionary already exists
     */
    public int insert(Dictionary d){
        // Look if this dictionary (with this name) already exists
        if(select(d.getTitle()) == null){
            // Gets the data repository in write mode
            this.open();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(DictionaryEntry.COLUMN_NAME_TITLE, d.getTitle());

            // Insert the new row, returning the primary key value of the new row
            long newdictionaryID;
            newdictionaryID = myDb.insert(DictionaryEntry.TABLE_NAME, null, values);
            d.setId(newdictionaryID);

            return 1;
        }
        return 0;

    }

    /**
     * Find all dictionaries in the DataBase
     *
     * @return The dictionary, or null if it was not found
     */
    public ArrayList<Dictionary> select(){
        // Gets the data repository in write mode
        this.open();

        // Query
        Cursor c = myDb.rawQuery("select * from " + DictionaryEntry.TABLE_NAME, new String[]{});

        // Creating object found
        ArrayList<Dictionary> d = new ArrayList<Dictionary>();
        while (c.moveToNext()) {
            // Obtained row is made of :
            // / id / title /
            d.add(new Dictionary(c.getLong(0), c.getString(1)));
        }
        c.close();
        // if Cursor object has not only one element, something wrong happened
        return d;
    }

    /**
     * Find a dictionary in the DataBase with its id
     *
     * @param id
     *          The id of the dictionary to find
     * @return The dictionary, or null if it was not found
     */
    public Dictionary select(long id){
        // Gets the data repository in write mode
        this.open();

        // Query
        Cursor c = myDb.rawQuery("select * from " + DictionaryEntry.TABLE_NAME + " where " + DictionaryEntry._ID + " = ?", new String[]{String.valueOf(id)});

        // Creating object found
        Dictionary d;
        if (c.getCount() == 1) {
           c.moveToNext();
            // Obtained row is made of :
            // / id / title /
            d = new Dictionary(c.getLong(0), c.getString(1));
        } else {
            d = null;
        }
        c.close();
        // if Cursor object has not only one element, something wrong happened
        return d;
    }

    /**
     * Find a dictionary in the DataBase with its name/title
     *
     * @param title
     *          The title of the dictionary to find
     * @return The dictionary, or null if it was not found
     */
    public Dictionary select(String title){
        // Gets the data repository in write mode
        this.open();

        Cursor c = myDb.rawQuery("select * from " + DictionaryEntry.TABLE_NAME + " where " + DictionaryEntry.COLUMN_NAME_TITLE + " = ?", new String[]{title});

        Dictionary d;
        if (c.getCount() == 1) {
            c.moveToNext();
            // Obtained row is made of :
            // / id / title /
            d = new Dictionary(c.getLong(0), c.getString(1));
        } else {
            d = null;
        }
        c.close();
        return d;
    }

    /**
     * Update the specified dictionary
     *
     * @param d
     *          The dictionary to update
     *
     * @return 1 if the dictionary was updated
     *         0 if the new dictionary already exists
     */
    public int update(Dictionary d){
        // Look if this dictionary (with this name) already exists
        if(select(d.getTitle()) == null)
        {
            // Gets the data repository in write mode
            this.open();

            ContentValues value = new ContentValues();
            value.put(DictionaryEntry.COLUMN_NAME_TITLE, d.getTitle());

            myDb.update(DictionaryEntry.TABLE_NAME, value, DictionaryEntry._ID + " = ?", new String[]{String.valueOf(d.getId())});
            return 1;
        }
        return 0;
    }

    /**
     * Delete a dictionary with its id
     *
     * @param id
     *          The id of the dictionary to delete
     */
    public void delete(long id){
        // Gets the data repository in write mode
        this.open();

        // suppress its words
        WordDataModel wdm = new WordDataModel(context);
        wdm.deleteAll(id);

        // delete words with dictionaryID in word table ? Or automatic with foreign key
        myDb.delete(DictionaryEntry.TABLE_NAME, DictionaryEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Delete a dictionary with its name
     *
     * @param dictionaryName
     *          The name of the dictionary to delete
     */
    public void delete(String dictionaryName){
        // Find the dictionary associated to this name
        Dictionary result = select(dictionaryName);
        if (result != null){
            // if there is a dictionary with this name, suppress its words and then suppress the dictionary
            delete(result.getId());
        }

    }

    public ArrayList<Dictionary> selectAll(){
        // Gets the data repository in write mode
        this.open();

        // Query
        Cursor c = myDb.rawQuery("select * from " + DictionaryEntry.TABLE_NAME, null);

        // Creating object found
        ArrayList<Dictionary> listDictionaries = new ArrayList<Dictionary>();
        while (c.moveToNext()) {
            Dictionary d = select(c.getLong(c.getColumnIndexOrThrow(DictionaryEntry._ID)));
            listDictionaries.add(d);
        }
        c.close();
        // if Cursor object has not only one element, something wrong happened
        return listDictionaries;
    }
}

package com.antoine_charlotte_romain.dictionary.Business;

import java.io.Serializable;

/**
 * Created by summer1 on 22/06/2015.
 */
public class Word implements Serializable{

    private long id;
    private long dictionaryID;
    private String headword;
    private String translation;
    private String note;

    public static int ALL_DICTIONARIES = -1;

    public Word(){}

    public Word(long dictionaryID, String headword, String translation){
        this.dictionaryID = dictionaryID;
        this.headword = headword;
        this.translation = translation;
    }

    public Word(long dictionaryID, String headword, String translation, String note){
        this.dictionaryID = dictionaryID;
        this.headword = headword;
        this.translation = translation;
        this.note = note;
    }

    public Word(long id, long dictionaryID, String headword, String translation, String note){
        this.id = id;
        this.dictionaryID = dictionaryID;
        this.headword = headword;
        this.translation = translation;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDictionaryID() {
        return dictionaryID;
    }

    public void setDictionaryID(long dictionaryID) {
        this.dictionaryID = dictionaryID;
    }

    public String getHeadword() {
        return headword;
    }

    public void setHeadword(String headword) {
        this.headword = headword;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

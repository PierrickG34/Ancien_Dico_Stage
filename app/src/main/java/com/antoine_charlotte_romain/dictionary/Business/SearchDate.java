package com.antoine_charlotte_romain.dictionary.Business;

/**
 * Created by summer1 on 08/07/2015.
 */
public class SearchDate {

    private long id;
    private Word word;
    private String date;

    public SearchDate(Word w) {
        this.word = w;
    }

    public SearchDate(Word w, String date) {
        this.word = w;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word w) {
        this.word = w;
    }
}

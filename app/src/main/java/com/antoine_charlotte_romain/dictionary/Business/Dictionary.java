package com.antoine_charlotte_romain.dictionary.Business;

import java.io.Serializable;

/**
 * Created by summer1 on 22/06/2015.
 * Updated by summer3 on 22/06/2015.
 */
public class Dictionary implements Serializable {

    private long id;
    private String title;

    public Dictionary() {}

    public Dictionary (String title){
        this.title = title;
    }

    public Dictionary (long id, String title){
        this.title = title;
        this.id = id;
    }

    @Override
    public String toString() {
        return title;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return this.id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }
}

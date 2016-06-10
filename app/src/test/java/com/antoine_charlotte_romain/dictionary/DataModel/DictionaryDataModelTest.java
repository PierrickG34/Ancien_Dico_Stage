package com.antoine_charlotte_romain.dictionary.DataModel;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.antoine_charlotte_romain.dictionary.Business.Dictionary;

/**
 * Created by summer3 on 23/06/2015.
 */
public class DictionaryDataModelTest extends AndroidTestCase {
    private DictionaryDataModel ddm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        ddm = new DictionaryDataModel(context);
        ddm.open();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        //ddm.close();
        //ddm = null;
    }

    //@Test
    public void testInsert() throws Exception {
        assertNotNull(ddm);
        assertNotNull(ddm.getDb());
        String title = "Dicococonono";
        Dictionary dico = new Dictionary(title);
        long ind = ddm.insert(dico);
        assertNotNull(dico.getId());
    }
}
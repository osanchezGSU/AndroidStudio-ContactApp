package com.example.contacts2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ContactDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mycontacts.db";
    private static final int DATABASE_VERSION = 2;

    private static final String CREATE_TABLE_CONTACT = "create table contact (_id INTEGER PRIMARY KEY autoincrement, "
            + "contactname text not null, streetaddress text, "
            + "city text, state text, zipcode text, "
            + "homenumber text, cellnumber text, "
            + "email text, birthday text, image text) ; ";
    public ContactDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(
                ContactDBHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all oll data");
        db.execSQL("DROP TABLE IF EXISTS contact");
        onCreate(db);
    }



}
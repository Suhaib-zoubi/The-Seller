package com.seller.android.theseller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SaveSettings {

    Context context;
    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "MyPrefs3";
    public static String UserID = "0";
    public static int Distance;

    public SaveSettings(Context context) {
        this.context = context;
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    }

    public void SaveData() {

        try {

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("UserID", String.valueOf(UserID));
            editor.putInt("Distance", Distance);
            editor.commit();
            LoadData();
        } catch (Exception e) {
        }
    }

    public void LoadData() {

        String TempUserID = sharedpreferences.getString("UserID", "empty");
        Distance = sharedpreferences.getInt("Distance", 500000); // add defaul distance
        if (!TempUserID.equals("empty"))
            UserID = TempUserID;// load user name
        else {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        }
    }
}


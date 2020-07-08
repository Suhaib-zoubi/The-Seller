package com.seller.android.theseller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    // Constants and Variables
    public static final String LOG_TAG = Login.class.getSimpleName();
    private AQuery aq;
    private ProgressDialog progressDialog;
    private RelativeLayout loginBT;
    private RelativeLayout signUpBT;
    private TextView errorDebug;
    EditText userName;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialization
        aq = new AQuery(this);
        progressDialog = new ProgressDialog(this);
        signUpBT = findViewById(R.id.buSignUp);
        loginBT = findViewById(R.id.buLogin);
        errorDebug = findViewById(R.id.error_debug);
        userName = findViewById(R.id.EDTUserName);
        password = findViewById(R.id.EDTpassword);
    }

    public void buLogin(View view) {
        errorDebug.setVisibility(View.INVISIBLE);
        loginBT.setClickable(false);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();

        String url = Networking.ServerURL + Networking.WebService + Networking.WebMethod_Login
                + Networking.Users_UserName + userName.getText()
                + "&" + Networking.Users_Password + password.getText();

        aq.ajax(url, JSONObject.class, this, "jsonCallback");
    }

    public void jsonCallback(String url, JSONObject json, AjaxStatus status) {

        if (json != null) {
            //successful ajax call
            String msg = null;
            int userID;
            try {
                msg = json.getString("Message");
                userID = json.getInt("UserID");
                if (userID != 0) { // successful login
                    SaveSettings.UserID = String.valueOf(userID);
                    SaveSettings saveSettings = new SaveSettings(this);
                    saveSettings.SaveData();

                    Intent intent = new Intent(this, ControlPanel.class);
                    startActivity(intent);
                    progressDialog.hide();
                } else {
                    errorDebug.setVisibility(View.VISIBLE);
                    errorDebug.setText(msg);
                    progressDialog.hide();
                    loginBT.setClickable(true);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.hide();
                loginBT.setClickable(true);
            }
        } else {
            //ajax error
            errorDebug.setVisibility(View.VISIBLE);
            errorDebug.setText(R.string.no_internet_access);
            progressDialog.hide();
            loginBT.setClickable(true);
        }

    }


    public void buSignUp(View view) {
        signUpBT.setClickable(false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        signUpBT.setClickable(true);
        errorDebug.setVisibility(View.INVISIBLE);
        loginBT.setClickable(true);
        userName.setText("");
        password.setText("");
    }
}


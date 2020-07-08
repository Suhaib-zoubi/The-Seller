package com.seller.android.theseller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;


public class SignUpActivity extends AppCompatActivity {

    // Constants and Variables
    public static final String LOG_TAG = SignUpActivity.class.getSimpleName();
    private SharedPreferences sharedpreferences;
    private static final String LOGIT = "logitKey";
    private static final String LATIT = "latitKey";
    private static final String MYPREFERENCE = "mypref";
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private CountryCodePicker ccp; // to get country code in phone number
    private int gender = 1; // Male by default
    private AQuery aq; // access to server
    private ProgressDialog progressDialog;
    private RelativeLayout saveAccountBT;
    private TextView errorDebug; // alert user to any error
    private AwesomeValidation validation; // validate input argument for register


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        aq = new AQuery(this);
        ccp = findViewById(R.id.ccp);
        progressDialog = new ProgressDialog(this);
        saveAccountBT = findViewById(R.id.buSaveAccount);
        errorDebug = findViewById(R.id.error_debug);
        validation = new AwesomeValidation(ValidationStyle.BASIC);
        Spinner spinner = (Spinner) findViewById(R.id.gender_sign_up); //select gender
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemIdAtPosition(position) ==
                        (long) getResources().obtainTypedArray(R.array.gender)
                                .getIndex(0)) {
                    gender = 1; // Male
                } else {
                    gender = 0; // Female
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void buSaveAccount(View view) {
        errorDebug.setVisibility(View.INVISIBLE);
        saveAccountBT.setClickable(false);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();

        EditText userName = (EditText) findViewById(R.id.EDTUserName);
        EditText email = (EditText) findViewById(R.id.EDemail);
        EditText password = (EditText) findViewById(R.id.EDTpassword);
        EditText phoneNumber = (EditText) findViewById(R.id.EDTPhoneNumber);
        Location myLocation = getLocation();

        validation.addValidation(this, R.id.EDTUserName, RegexTemplate.NOT_EMPTY,
                R.string.valid_username);
        validation.addValidation(this, R.id.EDemail, Patterns.EMAIL_ADDRESS,
                R.string.valid_email);
        validation.addValidation(this, R.id.EDTpassword, ".{6,}",
                R.string.valid_password);
        validation.addValidation(this, R.id.EDTPhoneNumber, "[5-9]{1}[0-9]{8}$",
                R.string.valid_number);

        sharedpreferences = getSharedPreferences(MYPREFERENCE,
                Context.MODE_PRIVATE);
        if (myLocation != null) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(LOGIT, String.valueOf(myLocation.getLatitude()));
            editor.putString(LATIT, String.valueOf(myLocation.getLatitude()));
            editor.commit();
        }
        String url = Networking.ServerURL + Networking.WebService + Networking.WebMethod_Register
                + Networking.Users_UserName + userName.getText()
                + "&" + Networking.Users_Password + password.getText().toString().trim()
                + "&" + Networking.Users_Email + email.getText().toString().trim()
                + "&" + Networking.Users_PhoneNumber + ccp.getFullNumber() + phoneNumber.getText().toString().trim()
                + "&" + Networking.Users_Logtit + sharedpreferences.getString(LOGIT, "0")
                + "&" + Networking.Users_Latitle + sharedpreferences.getString(LATIT, "0")
                + "&" + Networking.Users_Gender + gender;
        if (Utilities.isGps_enabled(this)
                & !Utilities.isGps_permission(this)
                & validation.validate())
            aq.ajax(url, JSONObject.class, this, "jsonCallback");
        else {
            progressDialog.hide();
            saveAccountBT.setClickable(true);
        }
    }


    public void jsonCallback(String url, JSONObject json, AjaxStatus status) {
        if (json != null) {
            //successful ajax call
            String msg = null;
            try {
                msg = json.getString("Message");

                if (Integer.parseInt(json.getString("IsAdded")) == 1) // successful new account
                    finish(); // back to Login screen
                else {
                    saveAccountBT.setClickable(true);
                    errorDebug.setVisibility(View.VISIBLE);
                    errorDebug.setText(msg);
                }
                progressDialog.hide();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
                progressDialog.hide();
                saveAccountBT.setClickable(true);
            }
        } else {
            //ajax error
            errorDebug.setVisibility(View.VISIBLE);
            errorDebug.setText(R.string.no_internet_access);
            progressDialog.hide();
            saveAccountBT.setClickable(true);
        }

    }

    // register user location
    Location getLocation() {

        Location myLocation = null;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            progressDialog.hide();
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION
                            , ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            if (locationManager != null) {
                if (!Utilities.isGps_enabled(this)) {
                    Utilities.setGps_enabled(this);
                    saveAccountBT.setClickable(true);
                } else {
                    myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (myLocation == null) {
                        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                            myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        } else {
                            Log.v("MainActivity", "TEST*: NO");
                        }
                    }
                }
                return myLocation;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveAccountBT.setClickable(true);
        errorDebug.setVisibility(View.INVISIBLE);
    }
}


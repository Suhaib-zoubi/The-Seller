package com.seller.android.theseller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddTool extends AppCompatActivity {

    // Constants and Variables
    public static final String LOG_TAG = AddTool.class.getSimpleName();
    private String TempToolID;
    private GridView gridView;
    private MyCustomAdapter myAdapter;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private TextInputLayout ToolName, ToolDes, ToolPrice;
    private ArrayList<ToolType> ToolType = new ArrayList<ToolType>();
    private ArrayList<AddToolItemImage> addToolItemImages = new ArrayList<AddToolItemImage>();
    private Spinner spinner;
    private AQuery aq;
    private int RESULT_LOAD_IMAGE = 1; //responde image in onActivityResult
    private Bitmap bitmap; // the selected image from gallery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tool);

        // Initialization
        ToolName = findViewById(R.id.EDTToolName);
        ToolDes = findViewById(R.id.EDToolDes);
        ToolPrice = findViewById(R.id.EDTToolPrice);

        // generate random number for the tool to salve all associate images
        final Random rand = new Random();
        String diceRoll = String.valueOf(rand.nextInt(7000000) + 5000);
        TempToolID = SaveSettings.UserID + "000000" + diceRoll;

        // initialize the gridView
        addToolItemImages.add(new AddToolItemImage("NewImage", R.drawable.loadimage, null));
        gridView = (GridView) findViewById(R.id.gridView);
        myAdapter = new MyCustomAdapter(this, addToolItemImages);
        gridView.setAdapter(myAdapter);

        //initialize spanner
        spinner = (Spinner) findViewById(R.id.spinner);

        // query tool types
        aq = new AQuery(this);
        String url = Networking.ServerURL + Networking.WebService + Networking.WebMethod_GetToolType;
        aq.ajax(url, JSONObject.class, this, "jsonCallbackGetToolType");
    }

    public void jsonCallbackGetToolType(String url, JSONObject json, AjaxStatus status) throws JSONException {

        if (json != null) {

            //successful ajax call
            JSONArray ToolData = json.getJSONArray("ToolData");
            String[] ToolTypeArray = new String[ToolData.length()];
            for (int i = 0; i < ToolData.length(); i++) {
                JSONObject jsToolData = ToolData.getJSONObject(i);
                ToolType.add(new ToolType(jsToolData.getString("ToolTypeName")
                        , jsToolData.getInt("ToolTypeID")));
                ToolTypeArray[i] = jsToolData.getString("ToolTypeName");
            }

            //add spanner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ToolTypeArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            spinner.setAdapter(spinnerArrayAdapter);
            Log.v(LOG_TAG, "TEST GetType: done2");
        } else {
            //ajax error
            Log.v(LOG_TAG, "TEST GetType: error");
        }

    }

    public void buAddTool(View view) {

        if (!validateText(ToolName) | !validateText(ToolDes) | !validateText(ToolPrice)) {
            return;
        }
        ToolName.setError(null);

        String url = Networking.ServerURL + Networking.WebService + Networking.WebMethod_AddTools
                + Networking.AddTools_UserID + SaveSettings.UserID
                + "&" + Networking.AddTools_ToolName + ToolName.getEditText().getText().toString().trim()
                + "&" + Networking.AddTools_ToolDes + ToolDes.getEditText().getText().toString().trim()
                + "&" + Networking.AddTools_ToolPrice + ToolPrice.getEditText().getText().toString().trim()
                + "&" + Networking.AddTools_ToolTypeID + String.valueOf(ToolType.get(spinner.getSelectedItemPosition()).ToolTypeID)
                + "&" + Networking.AddTools_TempToolID + String.valueOf(TempToolID);

        //query add tool
        aq.ajax(url, JSONObject.class, this, "jsonCallback");

    }

    private boolean validateText(TextInputLayout textInputLayout) {
        String emailInput = textInputLayout.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputLayout.setError(getString(R.string.field_cant_empty));
            return false;
        } else {
            textInputLayout.setError(null);
            return true;
        }
    }

    public void jsonCallback(String url, JSONObject json, AjaxStatus status) throws JSONException {

        if (json != null) {
            //successful ajax call
            String msg = json.getString("Message");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            int IsAdded = json.getInt("IsAdded");
            if (IsAdded != 0) { // mean the tool is added go to manage tool later
                finish();
            }
        } else {
            //ajax error
            Log.e(LOG_TAG, "add tool failed");
        }
    }

    // when select image from gallery it will loaded here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageView addPic = new ImageView(this);
            Uri selectedImage = data.getData();

            //loading images
            int index = addToolItemImages.size() - 1;
            if (index < 0)
                index = 0;
            addToolItemImages.remove(index);
            addToolItemImages.add(index, new AddToolItemImage("Loading",
                    R.drawable.add, null));
            myAdapter.notifyDataSetChanged();


            if (Build.VERSION.SDK_INT >= 29) {
                String[] projection = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.MIME_TYPE,
                        MediaStore.Images.ImageColumns.DISPLAY_NAME,
                };

                final Cursor cursor = this.getContentResolver().query(selectedImage,
                        projection, null, null
                        , MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(projection[1]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    // now that you have the media URI, you can decode it to a bitmap
                    try (
                            ParcelFileDescriptor pfd = this.getContentResolver()
                                    .openFileDescriptor(selectedImage, "r")) { // r: read only
                        if (pfd != null) {
                            bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                            addPic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                        }
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, ex.getMessage());
                    }
                }
            } else {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                addPic.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                //load images
                BitmapDrawable drawable = (BitmapDrawable) addPic.getDrawable();
                Bitmap largeBitmap = drawable.getBitmap();

                int h = 400; // height in pixels
                int w = 400; // width in pixels
                bitmap = Bitmap.createScaledBitmap(largeBitmap, w, h, true);
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            byte[] ImageData = baos.toByteArray();
            String encodedImageData = Base64.encodeToString(ImageData, 0);

            Map<String, Object> params = new HashMap<>();
            params.put("image", encodedImageData);
            params.put("TempToolID", TempToolID);
            String url = "https://the-seller20200630093320.azurewebsites.net/UsersWS.asmx";
            new MyAsyncGetNewNews().execute(url, encodedImageData, TempToolID);
        }
    }

    // send image to the webservice
    public class MyAsyncGetNewNews extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            final String NAMESPACE = "http://tempuri.org/";
            final String URL = params[0];
            final String METHOD_NAME = "UploadImage";
            final String SOAP_ACTION = "http://tempuri.org/UploadImage";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("image", params[1]);
            request.addProperty("TempToolID", params[2]);


            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            try {
                androidHttpTransport.call(SOAP_ACTION, envelope);
                Object obj = envelope.bodyIn;
                SoapObject result = (SoapObject) envelope.getResponse();
                Log.v(LOG_TAG, "TEST result: " + result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            int index = addToolItemImages.size() - 1;
            if (index < 0)
                index = 0;
            addToolItemImages.remove(index);
            addToolItemImages.add(index, new AddToolItemImage("NewImage", R.drawable.loadimage, null));
            addToolItemImages.add(index, new AddToolItemImage("path", R.drawable.add, bitmap));
            myAdapter.notifyDataSetChanged();
        }
    }

    // add loading images into grid view
    private class MyCustomAdapter extends BaseAdapter {

        private ArrayList<AddToolItemImage> listNewsDataAdapter;
        private Context context;

        public MyCustomAdapter(Context context, ArrayList<AddToolItemImage> listNewsDataAdapter) {
            this.context = context;
            this.listNewsDataAdapter = listNewsDataAdapter;
        }


        @Override
        public int getCount() {
            return listNewsDataAdapter.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, final View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();

            final AddToolItemImage addToolItemImage = listNewsDataAdapter.get(position);

            if (addToolItemImage.ImagePath.equals("Loading")) {
                View myView = mInflater.inflate(R.layout.add_tool_ticket_loading, null);

                return myView;
            } else {
                final View myView = mInflater.inflate(R.layout.add_tool_ticket, null);
                ImageView loadImage = (ImageView) myView.findViewById(R.id.IVloadimage);
                if (addToolItemImage.ImagePath.equals("NewImage"))
                    loadImage.setImageResource(addToolItemImage.Image);
                else
                    loadImage.setImageBitmap(addToolItemImage.bitmap);

                loadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addToolItemImage.ImagePath.equals("NewImage")) {
                            //permission for data storage
                            if (ActivityCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED &
                                    ActivityCompat.checkSelfPermission(context, ACCESS_NETWORK_STATE)
                                            != PackageManager.PERMISSION_GRANTED &
                                    ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(AddTool.this, new String[]{READ_EXTERNAL_STORAGE, ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_REQUEST_CODE);
                            } else {
                                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                            }
                        }
                    }
                });
                return myView;
            }
        }
    }
}
package com.seller.android.theseller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class ControlPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        SaveSettings saveSettings = new SaveSettings(this);
        saveSettings.LoadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                SaveSettings.UserID = "empty";
                SaveSettings saveSettings = new SaveSettings(this);
                saveSettings.SaveData();
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    public void buttAddTool(View view) {
        Intent intent=new Intent(this,AddTool.class);
        startActivity(intent);
    }
}

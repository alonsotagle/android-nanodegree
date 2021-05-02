package com.alonsotagle.nanodegree;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alonsotagle.nanodegree.spotify.SpotifyActivity;
import com.alonsotagle.nanodegree.utils.Utils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button main_spotify;
    Button main_scores;
    Button main_library;
    Button main_bigger;
    Button main_reader;
    Button main_capstone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getResources().getBoolean(R.bool.isTablet)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        main_spotify = (Button)findViewById(R.id.main_spotify);
        main_scores = (Button)findViewById(R.id.main_scores);
        main_library = (Button)findViewById(R.id.main_library);
        main_bigger = (Button)findViewById(R.id.main_bigger);
        main_reader = (Button)findViewById(R.id.main_reader);
        main_capstone = (Button)findViewById(R.id.main_capstone);

        main_spotify.setOnClickListener(this);
        main_scores.setOnClickListener(this);
        main_library.setOnClickListener(this);
        main_bigger.setOnClickListener(this);
        main_reader.setOnClickListener(this);
        main_capstone.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        String output = "";
        Button btn = (Button)view;
        switch (view.getId()) {
            case R.id.main_spotify:
                startActivity(new Intent(MainActivity.this, SpotifyActivity.class));
                break;

            // TODO Uncomment while I go forward
            /*
            case R.id.main_scores:
                output = getString(R.string.toast_scores);
                break;

            case R.id.main_library:
                output = getString(R.string.toast_library);
                break;

            case R.id.main_bigger:
                output = getString(R.string.toast_bigger);
                break;

            case R.id.main_reader:
                output = getString(R.string.toast_reader);
                break;

            case R.id.main_capstone:
                output = getString(R.string.toast_capstone);
                break;
            */

            default:
                output = (String) btn.getText();
                break;
        }
        showToast(output);
    }

    public void showToast(String s) {
        if (!s.equals("")) {
            Utils.showToast(MainActivity.this, getString(R.string.main_toast) + " " + s, Toast.LENGTH_SHORT);
        }
    }
}

package com.udacity.gradle.builditbigger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.androidtalks.androidjokeslibrary.JokeActivity;
import com.udacity.gradle.builditbigger.managers.AsyncJokeManager;

public class MainActivity extends AppCompatActivity implements AsyncJokeManager.AsyncJokeDownloadListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main
        );
    }

    public void tellJoke(View view){
        AsyncJokeManager asyncJokeManager = new AsyncJokeManager(MainActivity.this);
        asyncJokeManager.showProgressDialog(true);
        asyncJokeManager.setAsyncJokeDownloadListener(MainActivity.this);
        asyncJokeManager.downloadRandomJoke();
    }

    @Override
    public void onAsyncJokeDownloadCompleted(String joke) {
        if(!TextUtils.isEmpty(joke))
            startActivity(JokeActivity.createIntent(MainActivity.this,joke));
        else
            Toast.makeText(MainActivity.this,getString(R.string.message_error_joke_service),Toast.LENGTH_SHORT).show();
    }
}

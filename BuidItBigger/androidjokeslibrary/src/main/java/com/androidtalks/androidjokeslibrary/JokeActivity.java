package com.androidtalks.androidjokeslibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class JokeActivity extends AppCompatActivity {

    private static final String EXTRA_JOKE = "extra_joke";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.joke_activity_layout);

        if(getIntent() != null){
           ((TextView)findViewById(R.id.text_joke)).setText(getIntent().getStringExtra(EXTRA_JOKE));
        }
    }

    public static Intent createIntent(Context context, String joke){
        Intent intent = new Intent(context,JokeActivity.class);
        intent.putExtra(EXTRA_JOKE,joke);
        return intent;
    }
}

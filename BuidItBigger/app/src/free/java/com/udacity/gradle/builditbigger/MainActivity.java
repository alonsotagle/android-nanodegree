package com.udacity.gradle.builditbigger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.androidtalks.androidjokeslibrary.JokeActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.udacity.gradle.builditbigger.managers.AsyncJokeManager;

public class MainActivity extends AppCompatActivity implements AsyncJokeManager.AsyncJokeDownloadListener{

    private PublisherInterstitialAd mPublisherInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mPublisherInterstitialAd = new PublisherInterstitialAd(MainActivity.this);
        mPublisherInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
    }

    public void tellJoke(View view){
        AsyncJokeManager asyncJokeManager = new AsyncJokeManager(MainActivity.this);
        asyncJokeManager.showProgressDialog(true);
        asyncJokeManager.setAsyncJokeDownloadListener(MainActivity.this);
        asyncJokeManager.downloadRandomJoke();
        requestInterstitialAd();
    }

    @Override
    public void onAsyncJokeDownloadCompleted(final String joke) {

        if(mPublisherInterstitialAd.isLoaded()){
            mPublisherInterstitialAd.show();
        }else{
            startJokeActivity(joke);
        }

        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestInterstitialAd();
                startJokeActivity(joke);
            }
        });

    }

    private void startJokeActivity(String joke){

        if(!TextUtils.isEmpty(joke))
            startActivity(JokeActivity.createIntent(MainActivity.this,joke));
        else
            Toast.makeText(MainActivity.this,getString(R.string.message_error_joke_service),Toast.LENGTH_SHORT).show();
    }

    private void requestInterstitialAd() {
        PublisherAdRequest publisherAdRequest = new PublisherAdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("0BDDC4D5B47F74077E60C9E764140E75")
                .build();
        mPublisherInterstitialAd.loadAd(publisherAdRequest);
    }
}

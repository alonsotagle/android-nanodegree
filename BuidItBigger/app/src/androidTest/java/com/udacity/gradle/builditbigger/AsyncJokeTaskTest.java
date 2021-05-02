package com.udacity.gradle.builditbigger;

import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.udacity.gradle.builditbigger.managers.AsyncJokeManager;

/**
 * Created by leonelmendez on 09/07/15.
 */
public class AsyncJokeTaskTest extends AndroidTestCase {

    public void testAsyncJokeDownloadResponse(){
        AsyncJokeManager asyncJokeManager = new AsyncJokeManager(getContext());
        asyncJokeManager.downloadRandomJoke();
        asyncJokeManager.showProgressDialog(false);
        asyncJokeManager.setAsyncJokeDownloadListener(new AsyncJokeManager.AsyncJokeDownloadListener() {
            @Override
            public void onAsyncJokeDownloadCompleted(String joke) {
                assertEquals(true,!TextUtils.isEmpty(joke));
            }
        });
    }


}



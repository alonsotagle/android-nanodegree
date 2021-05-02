package com.udacity.gradle.builditbigger.managers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.leonelmendez.jokes.backend.myApi.MyApi;
import com.udacity.gradle.builditbigger.R;

import java.io.IOException;

/**
 * Created by leonelmendez on 09/07/15.
 */
public class AsyncJokeManager {

    private Context mContext;
    private AsyncJokeDownloadListener mAsyncJokeDownloadListener;
    private boolean isProgressDialogActivated = false;

    public AsyncJokeManager(Context context){
        this.mContext = context;
    }

    public void showProgressDialog(boolean activateProgressDialog){
        this.isProgressDialogActivated = activateProgressDialog;
    }
    public void downloadRandomJoke(){
        JokeAsyncTask jokeAsyncTask = new JokeAsyncTask();
        jokeAsyncTask.execute();
    }

    private class JokeAsyncTask extends AsyncTask<Void,Void,String> {

        private MyApi mService = null;
        private ProgressDialog progressDialog;

        public JokeAsyncTask(){
            progressDialog = new ProgressDialog(mContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isProgressDialogActivated){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(mContext.getString(R.string.message_downloading_joke));
            progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            if(mService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport()
                        , new AndroidJsonFactory(), null)
                        .setRootUrl("https://myjokes-1252.appspot.com/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                mService = builder.build();
            }


            try {
                return mService.randomJoke().execute().getData();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isProgressDialogActivated)
                progressDialog.dismiss();
            mAsyncJokeDownloadListener.onAsyncJokeDownloadCompleted(s);
        }
    }

    public void setAsyncJokeDownloadListener(AsyncJokeDownloadListener mAsyncJokeDownloadListener) {
        this.mAsyncJokeDownloadListener = mAsyncJokeDownloadListener;
    }

    public interface AsyncJokeDownloadListener {
        void onAsyncJokeDownloadCompleted(String joke);
    }

}

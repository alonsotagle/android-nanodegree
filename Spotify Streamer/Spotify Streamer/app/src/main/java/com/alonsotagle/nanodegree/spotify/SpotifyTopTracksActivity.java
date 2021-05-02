package com.alonsotagle.nanodegree.spotify;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.alonsotagle.nanodegree.R;

public class SpotifyTopTracksActivity extends AppCompatActivity {

    String artistName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_top_tracks);

        if(savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                SpotifyTopTracksActivityFragment artistSongsFragment = (SpotifyTopTracksActivityFragment) getFragmentManager().findFragmentById(R.id.spotify_fragment_container_top_tracks);
                artistSongsFragment.showArtistTopTrack(getIntent().getExtras().getString("artist_id"));
                artistName = getIntent().getExtras().getString("artist_name");
            }
        } else {
            artistName = savedInstanceState.getString("subtitle");
        }

        getSupportActionBar().setSubtitle(artistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("subtitle", artistName);
    }
}

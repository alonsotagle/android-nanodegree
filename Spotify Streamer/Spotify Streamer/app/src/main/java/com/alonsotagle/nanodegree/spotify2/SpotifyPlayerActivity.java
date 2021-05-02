package com.alonsotagle.nanodegree.spotify2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.alonsotagle.nanodegree.R;

public class SpotifyPlayerActivity extends AppCompatActivity {

    private SpotifyPlayerFragment spotifyPlayerFragment;
    static AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_player);

        getSupportActionBar().hide();

        activity = this;

        if (savedInstanceState == null) {

            this.spotifyPlayerFragment = new SpotifyPlayerFragment();
            this.spotifyPlayerFragment.setTwoPane(false);

            if(getIntent().getExtras() != null) {

                Bundle arguments = new Bundle();

                arguments.putInt(SpotifyPlayerFragment.TRACKINDEX, getIntent().getExtras().getInt("track_selected_position"));
                arguments.putString(SpotifyPlayerFragment.TOPTENTRACKS_PARCELABLE, getIntent().getExtras().getString("list_items"));
                this.spotifyPlayerFragment.setArguments(arguments);
            }

            getFragmentManager().beginTransaction().add(R.id.spotify_player_container, spotifyPlayerFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_player, menu);
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
}

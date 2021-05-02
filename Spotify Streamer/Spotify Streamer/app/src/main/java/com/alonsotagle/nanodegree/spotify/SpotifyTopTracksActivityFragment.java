package com.alonsotagle.nanodegree.spotify;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alonsotagle.nanodegree.R;
import com.alonsotagle.nanodegree.spotify2.SettingsActivity;
import com.alonsotagle.nanodegree.spotify2.SpotifyPlayerActivity;
import com.alonsotagle.nanodegree.spotify2.SpotifyPlayerFragment;
import com.alonsotagle.nanodegree.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifyTopTracksActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArtistTracksAdapter artistTracksAdapter;
    private boolean isTablet;
    private String mCountryCode;
    private String mArtistID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistTracksAdapter = new ArtistTracksAdapter();
        setRetainInstance(true);

        isTablet = getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spotify_fragment_top_tracks, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView artistTracksResults = (ListView)view.findViewById(R.id.lv_spotify_songs_results);
        artistTracksResults.setAdapter(artistTracksAdapter);
        artistTracksResults.setOnItemClickListener(this);
    }

    public void showArtistTopTrack(String artistId) {

        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showToast(getActivity(), getString(R.string.spotify_no_internet), Toast.LENGTH_LONG);
            return;
        }

        mArtistID = artistId;
        mCountryCode = GetCountryCodeFromPreference();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.spotify_dialog_wait));

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", mCountryCode);
        spotifyService.getArtistTopTrack(artistId, parameters, new Callback<Tracks>() {
            @Override
            public void success(final Tracks tracks, Response response) {

                if (response.getStatus() == 200) {

                    if (tracks != null && getActivity() != null && !tracks.tracks.isEmpty()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                artistTracksAdapter.setItems(tracks.tracks);
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                artistTracksAdapter.clearData();
                                progressDialog.dismiss();
                                Utils.showToast(getActivity().getApplicationContext(), getString(R.string.spotify_no_tracks), Toast.LENGTH_LONG);

                                if (!isTablet) {
                                    getActivity().finish();
                                }
                            }
                        });

                    }
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Gson gson = new GsonBuilder().create();
        Type trackAdapterType = new TypeToken<List<Track>>() {}.getType();

        if (isTablet) {
            SpotifyPlayerFragment spotifyPlayerFragment = new SpotifyPlayerFragment();
            spotifyPlayerFragment.setTwoPane(true);
            spotifyPlayerFragment.onPlayTracks(gson.toJson(((ArtistTracksAdapter) adapterView.getAdapter()).getItems(), trackAdapterType), i);
            spotifyPlayerFragment.show(getActivity().getFragmentManager(), "spotify_player");
        } else {

            Intent playerIntent = new Intent(getActivity(), SpotifyPlayerActivity.class);

            playerIntent.putExtra("list_items", gson.toJson(((ArtistTracksAdapter) adapterView.getAdapter()).getItems(), trackAdapterType));
            playerIntent.putExtra("track_selected_position", i);
            startActivity(playerIntent);
        }
    }

    public void UpdateTopTenTracksOnPreferenceUpdate() {
        if(artistTracksAdapter != null) {
            String selectecCountryCode = GetCountryCodeFromPreference();

            if (!selectecCountryCode.equals(mCountryCode) && mArtistID != null) {
                showArtistTopTrack(mArtistID);
            }
        }
    }

    public String GetCountryCodeFromPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String tempCountryCode = sharedPrefs.getString(SettingsActivity.COUNTRY_PREFERENCE_ID, "");

        if(tempCountryCode.length() == 0) {
            return getActivity().getResources().getConfiguration().locale.getCountry();
        } else {
            return tempCountryCode;
        }
    }
}

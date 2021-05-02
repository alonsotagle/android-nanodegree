package com.alonsotagle.nanodegree.spotify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.alonsotagle.nanodegree.R;
import com.alonsotagle.nanodegree.utils.Utils;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifySearcherFragment extends Fragment {

    private ArtistSearchAdapter artistSearchAdapter;
    private ArtistSelectedListener artistSelectedListener;

    public interface ArtistSelectedListener {
        void onSearchArtistTopTracks(String artistId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        artistSearchAdapter = new ArtistSearchAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spotify_fragment_search, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView artistSearchResults = (ListView)view.findViewById(R.id.lv_spotify_artist_results);
        artistSearchResults.setAdapter(artistSearchAdapter);
        artistSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getAdapter().getItem(position);
                ((ArtistSelectedListener) getActivity()).onSearchArtistTopTracks(artist.id);
            }
        });

        final SearchView svSearchArtist = (SearchView) view.findViewById(R.id.et_spotify_search);
        svSearchArtist.setIconifiedByDefault(false);
        svSearchArtist.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startArtistSearch(view.getContext(), svSearchArtist.getQuery().toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startArtistSearch(final Context context, String artist) {

        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showToast(context, getString(R.string.spotify_no_internet), Toast.LENGTH_LONG);
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.spotify_dialog_wait));

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        spotifyService.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager artistsPager, Response response) {

                if (response.getStatus() == 200) {
                    if (artistsPager != null && getActivity() != null && artistsPager.artists.items.size() > 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    artistSearchAdapter.setItems(artistsPager.artists.items);
                                }
                            });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                artistSearchAdapter.clearData();
                                progressDialog.dismiss();
                                Utils.showToast(context, getString(R.string.spotify_searcher_not_found), Toast.LENGTH_LONG);
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
                        Utils.showToast(context, getString(R.string.spotify_no_internet), Toast.LENGTH_LONG);
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ArtistSelectedListener) {
            artistSelectedListener = (ArtistSelectedListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implemenet ArtistSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        artistSelectedListener = null;
    }
}

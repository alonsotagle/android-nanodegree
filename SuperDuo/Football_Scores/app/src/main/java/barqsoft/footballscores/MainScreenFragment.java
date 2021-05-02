package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import barqsoft.footballscores.data.DatabaseContract;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_DATE = "date";

    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;

    @Bind(R.id.scores_list)
    RecyclerView mScoresList;

    public MainScreenFragment() {
    }
    public static MainScreenFragment newInstance(String date) {
        MainScreenFragment result = new MainScreenFragment();

        Bundle bundle = new Bundle();
        bundle.putString(KEY_DATE, date);
        result.setArguments(bundle);

        return result;
    }

//    private void updateScores() {
//        final Intent serviceStart = new Intent(getActivity(), MyFetchService.class);
//        getActivity().startService(serviceStart);
//    }

//    public void setFragmentDate(String date) {
//        fragmentdate[0] = date;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            fragmentdate = new String[]{args.getString(KEY_DATE)};
        }
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        mScoresList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ScoresAdapter(getActivity(), new ScoresAdapter.ScoreAdapterOnClickHandler() {
            @Override
            public void onClick(final double matchId) {
                mAdapter.mDetailMatchId = matchId;
                MainActivity.selected_match_id = (int) matchId;
                mAdapter.notifyDataSetChanged();
            }
        });
        mScoresList.setAdapter(mAdapter);
        mAdapter.mDetailMatchId = MainActivity.selected_match_id;
        /*
        scoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.mDetailMatchId = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        */
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        getLoaderManager().restartLoader(SCORES_LOADER, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        /*int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            i++;
            cursor.moveToNext();
        }*/
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


}

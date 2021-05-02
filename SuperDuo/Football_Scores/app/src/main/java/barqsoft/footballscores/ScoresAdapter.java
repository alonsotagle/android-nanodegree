package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder> {

    private Cursor mCursor;
    private final Context mContext;

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double mDetailMatchId = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private final ScoreAdapterOnClickHandler mClickHandler;

    public ScoresAdapter(Context context, ScoreAdapterOnClickHandler clickHandler/*, int choiceMode*/) {
        mContext = context;
        mClickHandler = clickHandler;
//        mICM = new ItemChoiceManager(this);
//        mICM.setChoiceMode(choiceMode);
    }

    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (!(viewGroup instanceof RecyclerView )) {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }

        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.scores_list_item, viewGroup, false);
        v.setFocusable(true);
        return new ScoreViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ScoreViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final String homeName = mCursor.getString(COL_HOME);
        holder.home_name.setText(homeName);
        final String awayName = mCursor.getString(COL_AWAY);
        holder.away_name.setText(awayName);
        holder.date.setText(mCursor.getString(COL_MATCHTIME));
        holder.score.setText(Utilies.getScores(mCursor.getInt(COL_HOME_GOALS), mCursor.getInt(COL_AWAY_GOALS)));
        holder.matchId = mCursor.getDouble(COL_ID);
        holder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName(homeName));
        holder.home_crest.setContentDescription(homeName);
        holder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName(awayName));
        holder.away_crest.setContentDescription(awayName);

        final LayoutInflater vi = (LayoutInflater) mContext.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);

        if (holder.matchId == mDetailMatchId) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");
            holder.container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(mContext, mCursor.getInt(COL_MATCHDAY),
                    mCursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilies.getLeague(mContext, mCursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    mContext.startActivity(createShareForecastIntent(
                            holder.home_name.getText() + " "
                            + holder.score.getText() + " "
                            + holder.away_name.getText() + " "
                    ));
                }
            });
        } else {
            holder.container.removeAllViews();
        }
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

    public class ScoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.home_name) TextView home_name;
        @Bind(R.id.away_name) TextView away_name;
        @Bind(R.id.score_textview) TextView score;
        @Bind(R.id.data_textview) TextView date;
        @Bind(R.id.home_crest) ImageView home_crest;
        @Bind(R.id.away_crest) ImageView away_crest;
        @Bind(R.id.details_fragment_container) ViewGroup container;
        double matchId;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(matchId);
        }
    }

    public static interface ScoreAdapterOnClickHandler {
        void onClick(double matchId);
    }

}

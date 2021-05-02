package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by alonsotagle on 14.01.16.
 */
public class NearestGameWidgetIntentService extends IntentService {

    // these indices must match the projection
    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;

    public NearestGameWidgetIntentService() {
        super("NearestGameWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                NearestGameWidgetProvider.class));

        Date todayDate = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Cursor data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null,
                new String[]{format.format(todayDate)}, null);
        if (data == null) {
            hideAllWidgets(appWidgetManager, appWidgetIds);
            return;
        }
        if (!data.moveToFirst()) {
            hideAllWidgets(appWidgetManager, appWidgetIds);
            data.close();
            return;
        }

        // Extract the match data from the Cursor
        final String homeName = data.getString(COL_HOME);
        final String awayName = data.getString(COL_AWAY);
        final String matchTime = data.getString(COL_MATCHTIME);
        final int homeGoals = data.getInt(COL_HOME_GOALS);
        final int awayGoals = data.getInt(COL_AWAY_GOALS);
        final double matchId = data.getDouble(COL_ID);
        data.close();

        // Perform this loop procedure for each nearest widget
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_nearest_layout);
            views.setViewVisibility(R.id.home_crest, View.VISIBLE);
            views.setViewVisibility(R.id.away_crest, View.VISIBLE);
            views.setViewVisibility(R.id.score_textview, View.VISIBLE);
            views.setViewVisibility(R.id.widget_no_data_textview, View.GONE);
            views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(homeName));
            views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(awayName));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.home_crest, homeName);
                setRemoteContentDescription(views, R.id.away_name, awayName);
            }
            views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awayGoals));

            // Create an Intent to launch MainActivity
            // TODO
//            Intent launchIntent = new Intent(this, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
//            views.setOnClickPendingIntent(R.id., pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void hideAllWidgets(AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_nearest_layout);
            views.setViewVisibility(R.id.home_crest, View.GONE);
            views.setViewVisibility(R.id.away_crest, View.GONE);
            views.setViewVisibility(R.id.score_textview, View.GONE);
            views.setViewVisibility(R.id.widget_no_data_textview, View.VISIBLE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int widgetId, String description) {
        views.setContentDescription(widgetId, description);
    }
}

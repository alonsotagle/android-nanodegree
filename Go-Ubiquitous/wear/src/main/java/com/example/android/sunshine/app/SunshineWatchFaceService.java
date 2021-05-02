/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineWatchFaceService extends CanvasWatchFaceService {

    private final String LOG_TAG = SunshineWatchFaceService.class.getSimpleName();

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineWatchFaceService.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private static final String KEY_WEATHER_ICON = "com.example.android.sunshine.app.key.icon";
        private static final String KEY_TEMPERATURE_HIGH = "com.example.android.sunshine.app.key.high";
        private static final String KEY_TEMPERATURE_LOW = "com.example.android.sunshine.app.key.low";
        private static final String WEARABLE_REQUEST_PATH = "/wearable";

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        boolean mRegisteredTimeZoneReceiver;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        Paint mBackgroundPaint;
        Paint mTextTimePaint;
        Paint mTextDatePaint;
        Paint mTextDateAmbientPaint;
        Paint mTextTemperatureHighPaint;
        Paint mTextTemperatureLowPaint;
        Paint mTextTemperatureLowAmbientPaint;

        Calendar mCalendar;

        float mTimeYOffSet;
        float mDateYOffset;
        float mWeatherYOffset;
        float mDividerYOffset;

        float mPaddingRightWheater;
        float mPaddingRightHighTemp;

        Bitmap mWeatherIcon;
        String mHighTemperature;
        String mLowTemperature;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private GoogleApiClient mGoogleApiClient;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mGoogleApiClient = new GoogleApiClient.Builder(SunshineWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            final Resources resources = SunshineWatchFaceService.this.getResources();

            final int primaryColor = resources.getColor(R.color.colorPrimary);
            final int textPrimaryColor = resources.getColor(R.color.textColorPrimary);
            final int textSecondaryColor = resources.getColor(R.color.textColorSecondary);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(primaryColor);

            mTextTimePaint = createTextPaint(textPrimaryColor);
            mTextDatePaint = createTextPaint(textSecondaryColor);

            mTextTemperatureHighPaint = createTextPaint(textPrimaryColor);
            mTextTemperatureLowPaint = createTextPaint(textSecondaryColor);

            /* Ambient mode */
            mTextDateAmbientPaint = createTextPaint(Color.WHITE);
            mTextTemperatureLowAmbientPaint = createTextPaint(Color.WHITE);

            /* Y Offset */
            mTimeYOffSet = resources.getDimension(R.dimen.digital_time_y_offset);
            mDateYOffset = resources.getDimension(R.dimen.digital_date_y_offset);
            mWeatherYOffset = resources.getDimension(R.dimen.digital_weather_y_offset);
            mDividerYOffset = resources.getDimension(R.dimen.digital_divider_y_offset);

            /* Padding */
            mPaddingRightWheater = resources.getDimension(R.dimen.padding_right_weather_icon);
            mPaddingRightHighTemp = resources.getDimension(R.dimen.padding_right_high_temperature);

            mCalendar = Calendar.getInstance();

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                registerWearListener();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
                unregisterWearListener();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void registerWearListener() {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

        private void unregisterWearListener() {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            final Resources resources = SunshineWatchFaceService.this.getResources();

            final float timeTextSize = resources.getDimension(R.dimen.text_time_size);
            final float dateTextSize = resources.getDimension(R.dimen.text_date_size);
            final float tempTextSize = resources.getDimension(R.dimen.text_temperature_size);

            mTextTimePaint.setTextSize(timeTextSize);
            mTextDatePaint.setTextSize(dateTextSize);
            mTextTemperatureHighPaint.setTextSize(tempTextSize);
            mTextTemperatureLowPaint.setTextSize(tempTextSize);

			/* Ambient Mode */
            mTextDateAmbientPaint.setTextSize(dateTextSize);
            mTextTemperatureLowAmbientPaint.setTextSize(tempTextSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mLowBitAmbient) {
                mTextTimePaint.setAntiAlias(!inAmbientMode);
                mTextDatePaint.setAntiAlias(!inAmbientMode);
                mTextTemperatureHighPaint.setAntiAlias(!inAmbientMode);
                mTextTemperatureLowPaint.setAntiAlias(!inAmbientMode);

                mTextDateAmbientPaint.setAntiAlias(!inAmbientMode);
                mTextTemperatureLowAmbientPaint.setAntiAlias(!inAmbientMode);
            }

            invalidate();
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Update the time
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            final int centerX = bounds.centerX();
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
                drawTime(canvas, centerX, getTextTimeInAmbientMode());
                drawDate(canvas, centerX, mTextDateAmbientPaint);
                drawSeparator(canvas, centerX, mTextDateAmbientPaint);
                drawWeather(canvas, centerX, mHighTemperature, mLowTemperature);

            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
                drawTime(canvas, centerX, getTextTime());
                drawDate(canvas, centerX, mTextDatePaint);
                drawSeparator(canvas, centerX, mTextDatePaint);
                drawWeather(canvas, centerX, mWeatherIcon, mHighTemperature, mLowTemperature);
            }
        }

        /**
         * Draw utility methods
         **/

        private void drawTime(Canvas canvas, int centerX, String textTime) {
            final float timeXOffset = centerX - calculateHalfSize(mTextTimePaint.measureText(textTime));
            canvas.drawText(textTime, timeXOffset, mTimeYOffSet, mTextTimePaint);
        }

        private void drawDate(Canvas canvas, int centerX, Paint datePaint) {
            final Date date = mCalendar.getTime();
            final String textDate = String.format("%ta %tb %td %tY",
                    date,
                    date,
                    date,
                    date);

            final float dateXOffset = centerX - calculateHalfSize(datePaint.measureText(textDate));
            canvas.drawText(textDate, dateXOffset, mDateYOffset, datePaint);
        }

        private void drawSeparator(Canvas canvas, float centerX, Paint textDatePaint) {
            canvas.drawLine(centerX - 20, mDividerYOffset, centerX + 20, mDividerYOffset, textDatePaint);
        }

        /*
         * Draw weather fields in Ambient mode
         */
        private void drawWeather(Canvas canvas, int centerX, String highTemperature, String lowTemperature) {
            if (mHighTemperature != null && mLowTemperature != null) {

                final float fieldSizeHighTemp = mTextTemperatureHighPaint.measureText(highTemperature)
                        + mPaddingRightHighTemp;
                final float fieldSizeLowTemp = mTextTemperatureLowAmbientPaint.measureText(lowTemperature);

                final float highTempXOffset = centerX - calculateHalfSize(fieldSizeHighTemp, fieldSizeLowTemp);

                final float lowTempXOffset = highTempXOffset + fieldSizeHighTemp;

                drawTemperature(canvas, highTempXOffset, lowTempXOffset,
                        mTextTemperatureHighPaint, highTemperature,
                        mTextTemperatureLowAmbientPaint, lowTemperature);
            }
        }

        /*
         * Draw weather fields in Interactive mode
         */
        private void drawWeather(Canvas canvas, float centerX, Bitmap weatherIcon,
                                 String highTemperature, String lowTemperature) {

            if (weatherIcon != null && highTemperature != null && lowTemperature != null) {

                final float scaledWidth = (mTextTemperatureHighPaint.getTextSize() /
                        weatherIcon.getHeight()) * weatherIcon.getWidth();

                final Bitmap icon = Bitmap.createScaledBitmap(weatherIcon, (int) scaledWidth,
                        (int) mTextTemperatureHighPaint.getTextSize(), true);

                final float fieldSizeIcon = icon.getWidth() + mPaddingRightWheater;
                final float fieldSizeHighTemp = mTextTemperatureHighPaint.measureText(highTemperature) + mPaddingRightHighTemp;
                final float fieldSizeLowTemp = mTextTemperatureLowPaint.measureText(lowTemperature);

                final float xOffset = centerX - calculateHalfSize(fieldSizeIcon, fieldSizeHighTemp, fieldSizeLowTemp);

                canvas.drawBitmap(icon, xOffset, mWeatherYOffset - icon.getHeight(), null);

                final float highTempXOffset = xOffset + fieldSizeIcon;
                final float lowTempXOffset = highTempXOffset + fieldSizeHighTemp;

                drawTemperature(canvas, highTempXOffset, lowTempXOffset, mTextTemperatureHighPaint,
                        mHighTemperature, mTextTemperatureLowPaint, mLowTemperature);
            }
        }

        private void drawTemperature(Canvas canvas, float xHighOffSet, float xLowOffSet,
                                     Paint highTemperaturePaint, String highTemperature,
                                     Paint lowTemperaturePaint, String lowTemperature) {

            canvas.drawText(highTemperature, xHighOffSet, mWeatherYOffset, highTemperaturePaint);
            canvas.drawText(lowTemperature, xLowOffSet, mWeatherYOffset, lowTemperaturePaint);
        }

        private String getTextTime() {
            final int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            final int minute = mCalendar.get(Calendar.MINUTE);
            final int second = mCalendar.get(Calendar.SECOND);
            return String.format("%d:%02d:%02d", hour, minute, second);
        }

        private String getTextTimeInAmbientMode() {
            final int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            final int minute = mCalendar.get(Calendar.MINUTE);
            return String.format("%d:%02d", hour, minute);
        }

        private float calculateHalfSize(float... sizes) {
            int length = 0;
            for (float size : sizes) {
                length += size;
            }

            return length / 2;
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(LOG_TAG, "Connection was suspended (code " + i + ")");
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent event : dataEvents) {
                if (DataEvent.TYPE_CHANGED == event.getType()) {
                    final DataItem item = event.getDataItem();
                    if (item.getUri().getPath().equals(WEARABLE_REQUEST_PATH)) {
                        final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                        if (dataMap.containsKey(KEY_TEMPERATURE_HIGH)) {
                            mHighTemperature = dataMap.getString(KEY_TEMPERATURE_HIGH);
                        }

                        if (dataMap.containsKey(KEY_TEMPERATURE_LOW)) {
                            mLowTemperature = dataMap.getString(KEY_TEMPERATURE_LOW);
                        }

                        if (dataMap.containsKey(KEY_WEATHER_ICON)) {
                            new GetWeatherBitmapTask().execute(dataMap.getAsset(KEY_WEATHER_ICON));
                        }
                    }
                    invalidate();
                }
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(LOG_TAG, "Connection has failed :" + connectionResult.getErrorMessage());
        }

        private class GetWeatherBitmapTask extends AsyncTask<Asset, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(Asset... assets) {
                if (assets != null && assets[0] != null) {
                    // convert asset into a file descriptor and block until it's ready
                    final InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                            mGoogleApiClient, assets[0]).await().getInputStream();

                    if (assetInputStream == null) {
                        return null;
                    }
                    // decode the stream into a bitmap
                    return BitmapFactory.decodeStream(assetInputStream);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mWeatherIcon = bitmap;
            }
        }
    }
}
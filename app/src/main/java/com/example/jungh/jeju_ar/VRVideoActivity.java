package com.example.jungh.jeju_ar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.util.Locale;

public class VRVideoActivity extends AppCompatActivity {

    String themename;
    String vr_path;

    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    private static final String STATE_PROGRESS_TIME = "progressTime";

    private VrVideoView videoWidgetView;

    private SeekBar seekBar;
    private TextView statusText;

    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 가로모드 고정
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_vr_video);

        Intent intent = getIntent();
        themename = intent.getStringExtra("themename");
        vr_path = intent.getStringExtra("vr_path");

        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        statusText = (TextView)findViewById(R.id.status_text);
        videoWidgetView = (VrVideoView)findViewById(R.id.video_view);

        if (savedInstanceState != null) {
            long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
            videoWidgetView.seekTo(progressTime);
            seekBar.setMax((int)savedInstanceState.getLong(STATE_VIDEO_DURATION));
            seekBar.setProgress((int) progressTime);

            isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
            if (isPaused) {
                videoWidgetView.pauseVideo();
            }
        } else {
            seekBar.setEnabled(false);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // if the user changed the position, seek to the new position.
                if (fromUser) {
                    videoWidgetView.seekTo(progress);
                    updateStatusText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ignore for now.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ignore for now.
            }
        });

        videoWidgetView.setEventListener(new VrVideoEventListener() {
            /**
             * Called by video widget on the UI thread when it's done loading the video.
             */
            @Override
            public void onLoadSuccess() {
                Log.i("VRVideoActivity", "Successfully loaded video " + videoWidgetView.getDuration());
                seekBar.setMax((int) videoWidgetView.getDuration());
                seekBar.setEnabled(true);
                updateStatusText();
            }

            /**
             * Called by video widget on the UI thread on any asynchronous error.
             */
            @Override
            public void onLoadError(String errorMessage) {
                Toast.makeText(getApplicationContext(), "Error loading video: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e("VRVideoActivity", "Error loading video: " + errorMessage);
            }

            @Override
            public void onClick() {
                if (isPaused) {
                    videoWidgetView.playVideo();
                } else {
                    videoWidgetView.pauseVideo();
                }

                isPaused = !isPaused;
                updateStatusText();
            }

            /**
             * Update the UI every frame.
             */
            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
            }

            /**
             * Make the video play in a loop. This method could also be used to move to the next video in
             * a playlist.
             */
            @Override
            public void onCompletion() {
                videoWidgetView.seekTo(0);
            }
        });

        Uri uri = Uri.parse(vr_path);

        try {
            if (videoWidgetView.getDuration() <= 0) {
                videoWidgetView.loadVideo(uri, new VrVideoView.Options());
                videoWidgetView.playVideo();
            }
        } catch (Exception e) {
            Toast.makeText(this.getApplicationContext(), "동영상 오류 : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void updateStatusText() {
        String status = (isPaused ? "Paused: " : "Playing: ") +
                String.format(Locale.getDefault(), "%.2f", videoWidgetView.getCurrentPosition() / 1000f) +
                " / " +
                videoWidgetView.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video was playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    public void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
    }
}

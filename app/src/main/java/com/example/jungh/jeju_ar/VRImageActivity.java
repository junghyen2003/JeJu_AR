package com.example.jungh.jeju_ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.jungh.jeju_ar.VRImage.ImageLoaderTask;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

public class VRImageActivity extends AppCompatActivity {

    VrPanoramaView vrPanoramaView;
    ImageLoaderTask backgroundImageLoaderTask;

    String themename;
    String vr_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_image);

        Intent intent = getIntent();
        themename = intent.getStringExtra("themename");
        vr_path = intent.getStringExtra("vr_path");

        vrPanoramaView = (VrPanoramaView)findViewById(R.id.vr_PanoramaView);

        loadPanoImages();

    }

    @Override
    public void onPause(){
        vrPanoramaView.pauseRendering();
        super.onPause();
    }

    @Override
    public void onResume(){
        vrPanoramaView.resumeRendering();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        vrPanoramaView.shutdown();
        super.onDestroy();
    }

    private synchronized void loadPanoImages(){
        ImageLoaderTask task = backgroundImageLoaderTask;
        if(task != null && !task.isCancelled()){
            task.cancel(true);
        }

        VrPanoramaView.Options viewOptions = new VrPanoramaView.Options();
        viewOptions.inputType = VrPanoramaView.Options.TYPE_MONO;

        task = new ImageLoaderTask(vrPanoramaView, viewOptions, vr_path);
        task.execute(this.getAssets());
    }
}

package org.activityrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.measure.MeasurePublisherService;
import org.activityrecognition.user.SessionManager;

public class CollectActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        int collectionTime = getIntent().getIntExtra("COLLECTION_TIME_SEC", 60);
        String userId = getIntent().getStringExtra("USER_ID");

        // start measure collection service
        Intent intent = new Intent(CollectActivity.this, MeasurePublisherService.class);
        intent.putExtra("COLLECTION_TIME_SEC", collectionTime);
        intent.putExtra("USER_ID", userId);
        startService(intent);

        // run for certain indicated time
        Handler handler = new Handler();
        handler.postDelayed(this::finish, collectionTime * 1000);
    }
}

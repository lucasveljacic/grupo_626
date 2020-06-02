package org.activityrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.measure.MeasurePublisherService;
import org.activityrecognition.measure.PacketListenerPredict;
import org.activityrecognition.measure.PredictionService;
import org.activityrecognition.user.SessionManager;

public class PredictActivity extends AppCompatActivity {

    private SessionManager session;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> backMenu());

        int collectionTime = getIntent().getIntExtra("COLLECTION_TIME_SEC", 60);
        String userId = getIntent().getStringExtra("USER_ID");

        // start measure collection service
        Intent intent = new Intent(PredictActivity.this, PredictionService.class);
        intent.putExtra("COLLECTION_TIME_SEC", collectionTime);
        startService(intent);

        // run for certain indicated time
        Handler handler = new Handler();
        handler.postDelayed(this::finish, collectionTime * 1000);
    }

    private void backMenu() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}

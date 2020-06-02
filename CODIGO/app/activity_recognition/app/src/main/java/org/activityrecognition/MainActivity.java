package org.activityrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.user.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private Button collectUser1Button;
    private Button collectUser2Button;
    private Button trainButton;
    private Button predictButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        collectUser1Button = findViewById(R.id.btn_collect_user_1);
        collectUser2Button = findViewById(R.id.btn_collect_user_2);
        trainButton = findViewById(R.id.btn_train);
        predictButton = findViewById(R.id.btn_predict);

        collectUser1Button.setOnClickListener(v -> collectUserMetrics("1"));
        collectUser2Button.setOnClickListener(v -> collectUserMetrics("2"));
        predictButton.setOnClickListener(v -> startPrediction());
    }

    private void collectUserMetrics(String id) {
        Intent intent = new Intent(getApplicationContext(), CollectActivity.class);
        intent.putExtra("USER_ID", id);
        intent.putExtra("COLLECTION_TIME_SEC", 10);
        startActivity(intent);
    }

    private void startPrediction() {
        Intent intent = new Intent(getApplicationContext(), PredictActivity.class);
        startActivity(intent);
    }
}

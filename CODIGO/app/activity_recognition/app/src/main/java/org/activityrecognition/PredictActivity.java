package org.activityrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
    }

    private void backMenu() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}

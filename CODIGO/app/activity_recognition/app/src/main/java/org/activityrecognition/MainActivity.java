package org.activityrecognition;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.recognition.MenuActivity;
import org.activityrecognition.user.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}

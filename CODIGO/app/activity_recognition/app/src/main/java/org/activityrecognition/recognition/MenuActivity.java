package org.activityrecognition.recognition;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.MainActivity;
import org.activityrecognition.R;
import org.activityrecognition.user.LoginActivity;
import org.activityrecognition.user.SessionManager;

public class MenuActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }


    }
}

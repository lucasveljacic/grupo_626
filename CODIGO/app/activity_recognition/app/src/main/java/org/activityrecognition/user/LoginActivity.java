package org.activityrecognition.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.activityrecognition.BaseActivity;
import org.activityrecognition.MainActivity;
import org.activityrecognition.R;
import org.activityrecognition.client.auth.AuthClient;
import org.activityrecognition.client.auth.AuthClientFactory;
import org.activityrecognition.client.auth.AuthResponse;
import org.activityrecognition.client.auth.LoginDTO;
import org.activityrecognition.event.EventTrackerService;
import org.activityrecognition.event.EventType;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "ACTREC_LOGIN";
    private EventTrackerService eventTrackerService;
    private AuthClient userClient;

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private Button loginButton;
    private TextView signUpLink;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userClient = AuthClientFactory.getClient();
        session = new SessionManager(getApplicationContext());

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        signUpLink = findViewById(R.id.link_signup);

        signUpLink.setOnClickListener(v -> {
            signUpLink.setEnabled(false);
            loginButton.setEnabled(false);
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        });
        loginButton.setOnClickListener(v -> login());

        eventTrackerService = new EventTrackerService(session);
    }

    @Override
    protected void disableActions() {

    }

    @Override
    protected void updateView() {

    }

    @Override
    public void onResume() {
        super.onResume();
        signUpLink.setEnabled(true);
        loginButton.setEnabled(true);
    }

    public void login() {
        Log.d(TAG, "Login");
        if (isOffline()) {
            return;
        }

        loginButton.setEnabled(false);

        if (!validate()) {
            onLoginFailed("Error de validación");
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Realizando el login");
        progressDialog.show();

        String email = Objects.requireNonNull(this.inputEmail.getEditText()).getText().toString();
        String password = Objects.requireNonNull(this.inputPassword.getEditText()).getText().toString();

        // launch a thread with the http call to the external service
        LoginDTO loginDTO = new LoginDTO("DEV", email, password);
        Call<AuthResponse> call = userClient.login(loginDTO);

        Log.i(TAG, String.format("Login request: %s", loginDTO.toString()));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.i(TAG, String.format("User logged in successfully! %s", response.body().toString()));

                    String email = loginDTO.getEmail();
                    String model = email
                            .replace("@", "_at_")
                            .replace(".", "_");

                    session.createLoginSession(response.body().getToken(), email, model);

                    onLoginSuccess(email);
                } else {
                    Log.i(TAG, String.format("User logged in failure! %s", response.raw().toString()));
                    onLoginFailed(response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Unable to Login User."+ t.getMessage());
                t.printStackTrace();
                onLoginFailed(t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String email) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        eventTrackerService.pushEvent(EventType.LOGIN, String.format("User %s logged In successfully", email));
        finish();
    }

    public void onLoginFailed(String errMessage) {
        Log.e(TAG, errMessage);
        Toast.makeText(getBaseContext(), "Credenciales incorrectas!", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        inputEmail.setError(null);
        inputPassword.setError(null);

        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();


        if (email.isEmpty()) {
            inputEmail.setError("El email es requerido");
            valid = false;
        }

        if (password.isEmpty()) {
            inputPassword.setError("El password es requerido");
            valid = false;
        }

        return valid;
    }
}

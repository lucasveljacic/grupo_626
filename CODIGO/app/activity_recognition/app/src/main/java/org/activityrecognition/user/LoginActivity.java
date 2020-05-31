package org.activityrecognition.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.activityrecognition.R;
import org.activityrecognition.client.user.LoginDTO;
import org.activityrecognition.client.user.UserClient;
import org.activityrecognition.client.user.UserClientFactory;
import org.activityrecognition.client.user.UserDTO;
import org.activityrecognition.client.user.UserResponse;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private UserClient client;

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private Button loginButton;
    private TextView signUpLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);

        signUpLink = findViewById(R.id.link_signup);

        loginButton.setOnClickListener(v -> login());

        signUpLink.setOnClickListener(v -> {
            // Start the Sign Up activity
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
        });

        client = UserClientFactory.getClient();
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed("Error de validación");
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Realizando el login");
        progressDialog.show();

        String email = Objects.requireNonNull(this.inputEmail.getEditText()).getText().toString();
        String password = Objects.requireNonNull(this.inputPassword.getEditText()).getText().toString();

        // launch a thread with the http call to the external service
        LoginDTO loginDTO = new LoginDTO("DEV", email, password);
        Call<UserResponse> call = client.login(loginDTO);

        Log.i(TAG, String.format("Login request: %s", loginDTO.toString()));

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.i(TAG, String.format("User logged in successfully! %s", response.body().toString()));
                    onLoginSuccess();
                } else {
                    Log.i(TAG, String.format("User logged in failure! %s", response.raw().toString()));
                    onLoginFailed(response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Unable to Login User."+ t.getMessage());
                t.printStackTrace();
                onLoginFailed(t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed(String errMessage) {
        Toast.makeText(getBaseContext(), errMessage, Toast.LENGTH_LONG).show();

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

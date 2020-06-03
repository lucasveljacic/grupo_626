package org.activityrecognition.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.activityrecognition.MainActivity;
import org.activityrecognition.R;
import org.activityrecognition.client.auth.AuthClient;
import org.activityrecognition.client.auth.AuthClientFactory;
import org.activityrecognition.client.auth.SignUpDTO;
import org.activityrecognition.client.auth.AuthResponse;
import org.activityrecognition.event.EventTrackerService;
import org.activityrecognition.event.EventType;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "ACTREC_SIGNUP";

    private AuthClient client;

    private TextInputLayout inputName;
    private TextInputLayout inputLastName;
    private TextInputLayout inputDni;
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private TextInputLayout inputPasswordRepeat;
    private Button signUpButton;
    private SessionManager session;
    private EventTrackerService eventTrackerService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        inputName = findViewById(R.id.input_name);
        inputLastName = findViewById(R.id.input_lastname);
        inputDni = findViewById(R.id.input_dni);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputPasswordRepeat = findViewById(R.id.input_password_repeat);
        signUpButton = findViewById(R.id.btn_login);

        signUpButton.setOnClickListener(v -> signUp());

        client = AuthClientFactory.getClient();
        session = new SessionManager(getApplicationContext());

        eventTrackerService = new EventTrackerService(session);
    }

    @Override
    public void onResume() {
        super.onResume();
        signUpButton.setEnabled(true);
    }

    public void signUp() {
        Log.d(TAG, "Signing Up");
        signUpButton.setEnabled(false);

        if (!validate()) {
            onSignUpFailed("Error de validación");
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrando usuario");
        progressDialog.show();

        String name = Objects.requireNonNull(this.inputName.getEditText()).getText().toString();
        String lastName = Objects.requireNonNull(this.inputLastName.getEditText()).getText().toString();
        Integer dni = Integer.valueOf(Objects.requireNonNull(this.inputDni.getEditText()).getText().toString());
        String email = Objects.requireNonNull(this.inputEmail.getEditText()).getText().toString();
        String password = Objects.requireNonNull(this.inputPassword.getEditText()).getText().toString();

        // launch a thread with the http call to the external service
        SignUpDTO userDTO = new SignUpDTO("DEV", name, lastName, dni, email, password);
        Call<AuthResponse> call = client.signUp(userDTO);

        Log.i(TAG, String.format("SignUp request: %s", userDTO.toString()));

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.i(TAG, String.format("User signedUp successfully! %s", response.body().toString()));

                    String email = userDTO.getEmail();
                    String model = email
                            .replace("@", "_at_")
                            .replace(".", "_");

                    session.createLoginSession(response.body().getToken(), email, model);

                    onSignUpSuccess(email);
                } else {
                    Log.i(TAG, String.format("User signedUp failure! %s", response.raw().toString()));
                    onSignUpFailed(response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Unable to signedUp User."+ t.getMessage());
                t.printStackTrace();
                onSignUpFailed(t.getMessage());
            }
        });
    }

    public void onSignUpSuccess(String email) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        eventTrackerService.pushEvent(EventType.REGISTER, String.format("Usuario %s registrado exitosamente", email));
        finish();
    }

    public void onSignUpFailed(String errMessage) {
        Log.e(TAG, errMessage);
        Toast.makeText(getBaseContext(), "Error en en registro!", Toast.LENGTH_LONG).show();
        signUpButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        inputName.setError(null);
        inputLastName.setError(null);
        inputDni.setError(null);
        inputEmail.setError(null);
        inputPassword.setError(null);
        inputPasswordRepeat.setError(null);

        String name = inputName.getEditText().getText().toString();
        String lastName = inputLastName.getEditText().getText().toString();
        String dni = inputDni.getEditText().getText().toString();
        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();
        String passwordRepeat = inputPasswordRepeat.getEditText().getText().toString();


        if (email.isEmpty()) {
            inputEmail.setError("El email es requerido");
            valid = false;
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Ingrese un correo válido");
                valid = false;
            }
        }

        if (name.isEmpty()) {
            inputName.setError("El nombre es requerido");
            valid = false;
        }

        if (lastName.isEmpty()) {
            inputLastName.setError("El apellido es requerido");
            valid = false;
        }

        if (dni.isEmpty()) {
            inputDni.setError("El dni es requerido");
            valid = false;
        } else {
            try {
                Integer dniInt = Integer.valueOf(dni);
            } catch (Exception e) {
                inputDni.setError("El DNI dene estar compuesto solamente por números");
                valid = false;
            }
        }

        if (password.isEmpty()) {
            inputPassword.setError("El password es requerido");
            valid = false;
        }

        if (passwordRepeat.isEmpty()) {
            inputPasswordRepeat.setError("Ingrese nuevamente el password");
            valid = false;
        }

        if (inputPassword.getEditText().toString().length() < 8) {
            inputPassword.setError("El password debe tener al menos 8 caracteres");
            valid = false;
        }

        if (!password.isEmpty() && !passwordRepeat.isEmpty() && !password.equals(passwordRepeat)) {
                this.inputPasswordRepeat.setError("Los passwords ingresados no son iguales");
                valid = false;
        }

        return valid;
    }
}

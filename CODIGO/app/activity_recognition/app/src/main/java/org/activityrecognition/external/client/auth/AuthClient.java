package org.activityrecognition.external.client.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface AuthClient {
        @POST("/api/api/register")
        Call<AuthResponse> signUp(@Body SignUpDTO userSignUpDTO);

        @POST("/api/api/login")
        Call<AuthResponse> login(@Body LoginDTO userLoginDTO);
}

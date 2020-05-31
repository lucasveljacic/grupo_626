package org.activityrecognition.client.user;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface UserClient {
        @POST("/api/api/register")
        Call<UserResponse> signUp(@Body UserDTO userSignUpDTO);

        @POST("/api/api/login")
        Call<UserResponse> login(@Body LoginDTO userLoginDTO);
}

package com.example.climap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GoogleAuthService {
    @POST("api/auth/google")
    Call<UserResponse> googleLogin(@Body IdTokenRequest request);
}

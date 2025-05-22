package com.example.climap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/register")
    Call<UserResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<UserResponse> login(@Body LoginRequest request);
}

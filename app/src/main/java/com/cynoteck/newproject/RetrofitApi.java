package com.cynoteck.newproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitApi {

    @GET
    Call<String> getDataFromGoogleApi(@Url String url);

}

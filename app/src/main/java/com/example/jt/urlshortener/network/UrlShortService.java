package com.example.jt.urlshortener.network;

import com.example.jt.urlshortener.model.UrlResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by JT on 8/28/17.
 */


public interface UrlShortService {
    @GET
    Call<UrlResponse> getShort(@Url String url);
}


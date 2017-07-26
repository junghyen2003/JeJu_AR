package com.example.jungh.jeju_ar.service;

import com.example.jungh.jeju_ar.model.LocationModel;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by jungh on 2017-06-30.
 */

public interface ResponseService {
    @Headers("Content-Type: application/json")
    @GET("/api/v1/locations/{id}")
    Observable<Response<LocationModel>> getLocationDetail(
            @Path("id") int id);
}

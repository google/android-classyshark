package com.google.classyshark.updater.networking;


import com.google.classyshark.updater.models.Release;
import retrofit2.Call;
import retrofit2.http.GET;

interface GitHubApi {

    String ENDPOINT = "https://api.github.com/";

    @GET("repos/google/android-classyshark/releases/latest")
    Call<Release> getLatestRelease();
}

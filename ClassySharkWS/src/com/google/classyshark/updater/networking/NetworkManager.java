package com.google.classyshark.updater.networking;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    public static GitHubApi getGitHubApi() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GitHubApi.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(GitHubApi.class);
    }
}

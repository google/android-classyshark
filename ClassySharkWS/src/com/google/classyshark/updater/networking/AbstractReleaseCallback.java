package com.google.classyshark.updater.networking;

import com.google.classyshark.updater.models.Release;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class AbstractReleaseCallback implements Callback<Release>{

    @Override
    public void onResponse(Call<Release> call, Response<Release> response) {
        onReleaseReceived(response.body());
    }

    public abstract void onReleaseReceived(Release release);

    @Override
    public void onFailure(Call<Release> call, Throwable throwable) {
        System.err.println("ERROR: " + throwable.getMessage());
    }
}

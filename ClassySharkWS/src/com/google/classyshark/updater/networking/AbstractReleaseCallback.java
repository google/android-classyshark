/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

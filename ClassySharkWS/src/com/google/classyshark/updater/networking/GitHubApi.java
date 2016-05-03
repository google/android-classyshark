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
import retrofit2.http.GET;

/**
 * This class is the one taking care of representing the API needed
 * in order to retrieve the latest release data from GitHub.
 */
public interface GitHubApi {

    String ENDPOINT = "https://api.github.com/";

    @GET("repos/google/android-classyshark/releases/latest")
    Call<Release> getLatestRelease();
}

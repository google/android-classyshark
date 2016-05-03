/*
 * Copyright 2015 Google, Inc.
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

package com.google.classysharkandroid.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.classysharkandroid.R;
import com.google.common.html.HtmlEscapers;

public class SourceViewerActivity extends AppCompatActivity {

    private String sourceCodeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_source_viewer);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(0xff606060));

        String result = "";

        try {

            String name = getIntent().getStringExtra(ClassesListActivity.SELECTED_CLASS_NAME);
            String barName = name.substring(name.lastIndexOf(".") + 1);

            bar.setTitle((Html.fromHtml("<font color=\"#FFFF80\">" +
                    barName + "</font>")));

            result = getIntent().getStringExtra(ClassesListActivity.SELECTED_CLASS_DUMP);

        } catch (Exception e) {
            e.printStackTrace();
        }

        sourceCodeText = result;
        sourceCodeText = HtmlEscapers.htmlEscaper().escape(sourceCodeText);

        WebView webView = (WebView) findViewById(R.id.source_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
            }
        });

        webView.loadDataWithBaseURL("file:///android_asset/", "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><script src=\"run_prettify.js?skin=sons-of-obsidian\"></script></head><body bgcolor=\"#000000\"><pre class=\"prettyprint \">" + sourceCodeText + "</pre></body></html>", "text/html", "UTF-8", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
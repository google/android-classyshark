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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.classysharkandroid.R;
import com.google.classysharkandroid.adapters.StableArrayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String APP_NAME = "APP_NAME";
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listView);
    }

    @Override
    public void onStart() {
        super.onStart();

        final ArrayList<AppListNode> apps = new ArrayList<>();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);

        for (Object object : pkgAppsList) {
            ResolveInfo info = (ResolveInfo) object;
            File file = new File(info.activityInfo.applicationInfo.publicSourceDir);

            AppListNode aln = new AppListNode();
            aln.name = info.activityInfo.applicationInfo.processName.toString();
            aln.file = file;

            apps.add(aln);
        }

        Collections.sort(apps);

        final StableArrayAdapter adapter = new StableArrayAdapter(MainActivity.this,
                android.R.layout.simple_list_item_1, convert(apps));
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(MainActivity.this, ClassesListActivity.class);
                String mimeType = myMime.getMimeTypeFromExtension("apk");
                newIntent.setDataAndType(Uri.fromFile(apps.get(position).file),mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.putExtra(APP_NAME, apps.get(position).name);

                try {
                    startActivity(newIntent);
                } catch (ActivityNotFoundException e) {

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static List<String> convert(ArrayList<AppListNode> apps) {
        ArrayList<String> result = new ArrayList<>();

        for(AppListNode node : apps) {
            result.add(node.name);
        }

        return result;
    }

    private static class AppListNode implements Comparable<AppListNode> {

        public String name;
        public File file;

        @Override
        public int compareTo(AppListNode another) {
            return this.name.compareTo(another.name);
        }
    }
}
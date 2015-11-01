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

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.classysharkandroid.R;
import com.google.classysharkandroid.adapters.StableArrayAdapter;
import com.google.classysharkandroid.dex.DexLoaderBuilder;
import com.google.classysharkandroid.reflector.ClassesNamesList;
import com.google.classysharkandroid.reflector.Reflector;
import com.google.classysharkandroid.utils.IOUtils;
import com.google.classysharkandroid.utils.UriUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class ClassesListActivity extends AppCompatActivity {

    public static final String SELECTED_CLASS_NAME = "SELECTED_CLASS_NAME";
    public static final String SELECTED_CLASS_DUMP = "SELECTED_CLASS_DUMP";

    private Uri uriFromIntent;
    private ClassesNamesList classesList;
    private ListView lv;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes_list);

        lv = (ListView) findViewById(R.id.listView);
        uriFromIntent = getIntent().getData();
        classesList = new ClassesNamesList();

        setActionBar();

        InputStream uriStream;
        try {

            mProgressDialog = new ProgressDialog(ClassesListActivity.this);
            mProgressDialog.setIcon(R.mipmap.ic_launcher);
            mProgressDialog.setMessage("¸.·´¯`·.´¯`·.¸¸.·´¯`·.¸><(((º>");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            uriStream = UriUtils.getStreamFromUri(ClassesListActivity.this,
                    uriFromIntent);

            final byte[] bytes = IOUtils.toByteArray(uriStream);

            new FillClassesNamesThread(bytes).start();

            new StartDexLoaderThread(bytes).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActionBar() {
        ActionBar bar = getSupportActionBar();
        String title = "Content";

        if(getIntent().getStringExtra(MainActivity.APP_NAME) != null) {
            title = getIntent().getStringExtra(MainActivity.APP_NAME);
        }

        bar.setTitle((Html.fromHtml("<font color=\"#FFFF80\">" +
                title + "</font>")));
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

    private class FillClassesNamesThread extends Thread {
        private final byte[] bytes;

        public FillClassesNamesThread(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {

            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                File incomeFile = File.createTempFile("classes" + Thread.currentThread().getId(), ".dex", getCacheDir());

                IOUtils.bytesToFile(bytes, incomeFile);

                File optimizedFile = File.createTempFile("opt" + Thread.currentThread().getId(), ".dex", getCacheDir());

                DexFile dx = DexFile.loadDex(incomeFile.getPath(),
                        optimizedFile.getPath(), 0);

                for (Enumeration<String> classNames = dx.entries(); classNames.hasMoreElements(); ) {
                    String className = classNames.nextElement();
                    classesList.add(className);
                }

            } catch (Exception e) {
                // ODEX, need to see how to handle
                e.printStackTrace();
            }


            ClassesListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < classesList.getClassNames().size(); ++i) {
                        list.add(classesList.getClassNames().get(i));
                    }
                    final StableArrayAdapter adapter = new StableArrayAdapter(ClassesListActivity.this,
                            android.R.layout.simple_list_item_1, list);
                    lv.setAdapter(adapter);

                    mProgressDialog.dismiss();

                    if(classesList.getClassNames().isEmpty()) {
                        Toast.makeText(ClassesListActivity.this, "Sorry don't support ODEX", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class StartDexLoaderThread extends Thread {
        private final byte[] bytes;

        public StartDexLoaderThread(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                final DexClassLoader loader = DexLoaderBuilder.fromBytes(ClassesListActivity.this, bytes);

                ClassesListActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                Class<?> loadClass;
                                try {
                                    loadClass = loader.loadClass(classesList.getClassName(position));

                                    Reflector reflector = new Reflector(loadClass);

                                    reflector.generateClassData();
                                    String result = reflector.toString();

                                    Intent i = new Intent(ClassesListActivity.this,
                                            SourceViewerActivity.class);

                                    i.putExtra(ClassesListActivity.SELECTED_CLASS_NAME,
                                            classesList.getClassName(position));
                                    i.putExtra(ClassesListActivity.SELECTED_CLASS_DUMP,
                                            result);

                                    startActivity(i);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


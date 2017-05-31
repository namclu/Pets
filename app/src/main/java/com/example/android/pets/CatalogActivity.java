/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.adapter.PetCursorAdapter;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CatalogActivity.class.getSimpleName();
    private static final int PET_LOADER = 0;

    // Global fields
    private PetCursorAdapter mCursorAdapter;

    // Projection specifies which columns from db the query will actually use
    private String[] projection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        // Find empty view and show only when the list has zero items
        View emptyListView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyListView);

        // Call ContentResolver query() method, which will call PetProvider query() method
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,   // Content URI
                projection,             // Project
                null,                   // Selection
                null,                   // SelectionArgs
                null);                  // Sort Order

        // Instantiate CursorAdapter
        mCursorAdapter = new PetCursorAdapter(this, cursor);
        petListView.setAdapter(mCursorAdapter);

        // Setup item click listener to open EditorActivity, passing in content URI (if present)
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editorIntent = new Intent(
                        Intent.ACTION_VIEW,
                        ContentUris.withAppendedId(PetEntry.CONTENT_URI, id),
                        CatalogActivity.this,
                        EditorActivity.class);

                startActivity(editorIntent);
            }
        });

        // Prepare the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Required methods for LoaderManager.LoaderCallbacks<Cursor>
    * */
    // called when the system needs a new loader to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,   // Parent activity content
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in Cursor
                null,                   // No selection clause
                null,                   // No selection args
                null);                  // Default sort order
    }

    // called when a loader has finished loading data
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    // called when a previously created loader is being reset (when you call destroyLoader(int)
    // or when the activity or fragment is destroyed, and thus making its data unavailable.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    // Add a pet to database
    public void insertPet() {

        // Create ContentValues object for a single pet
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row into database, returning ID of that new row
        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }
}
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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.adapter.PetCursorAdapter;
import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EDITOR_PET_LOADER = 1;

    // Global fields
    private PetCursorAdapter mCursorAdapter;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private String mSelection;
    private String[] mSelectionArgs;

    // Projection specifies which columns from db the query will actually use
    private String[] mProjection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get URI from calling activity. URI == null if called from FAB
        Intent intent = getIntent();
        Uri petUri = intent.getData();

        // Set title depending on if user wants to "Add a Pet" using FAB
        // or wants to "Edit Pet" by tapping on an existing Pet
        if (petUri == null) {
            setTitle(R.string.editor_activity_title_new_pet);
        } else {
            setTitle(R.string.editor_activity_title_edit_pet);

            // Create a cursor where pet _id == the id from petUri
            mSelection = PetEntry._ID + "=?";
            mSelectionArgs = new String[] {String.valueOf(ContentUris.parseId(petUri))};

            Cursor cursor = getContentResolver().query(
                    PetEntry.CONTENT_URI,   // Content URI
                    mProjection,            // Project
                    mSelection,              // Selection
                    mSelectionArgs,          // SelectionArgs
                    null);                  // Sort Order

            // Instantiate CursorAdapter
            mCursorAdapter = new PetCursorAdapter(this, cursor);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        // Prepare the loader
        getLoaderManager().initLoader(EDITOR_PET_LOADER, null, this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save new Pet to db and exit activity
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
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

        // Return a CursorLoader for a single pet
        return new CursorLoader(this,   // Parent activity content
                PetEntry.CONTENT_URI,   // Provider content URI to query
                mProjection,            // Columns to include in Cursor
                mSelection,             // No selection clause
                mSelectionArgs,         // No selection args
                null);                  // Default sort order
    }

    // called when a loader has finished loading data
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String petName = data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            if (!petName.isEmpty()) {
                mNameEditText.setText(petName);
            }
        }
    }

    // called when a previously created loader is being reset (when you call destroyLoader(int)
    // or when the activity or fragment is destroyed, and thus making its data unavailable.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    // Get user input of Pet from editor and saves new Pet into database
    private void savePet() {
        // Create db helper and get writable db
        PetDbHelper dbHelper = new PetDbHelper(this);

        // Create ContentValues object and put user entered values into corresponding column names
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(mNameEditText.getText())) {
            values.put(PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(mBreedEditText.getText())) {
            values.put(PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(mWeightEditText.getText())) {
            int weight = Integer.parseInt(mWeightEditText.getText().toString().trim());
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
        }
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);

        // Insert new row using PetProvider insert() method and get a URI
        // then use URI to get the row ID.
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        if (uri == null) {
            // If URI == null, then there was an error with db insertion
            Toast.makeText(this, R.string.editor_insert_pet_success, Toast.LENGTH_SHORT).show();
        } else {
            // Else db insertion successful and we display Toast
            Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
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
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDITOR_PET_LOADER = 1;

    /* Global fields */
    // Content URI for the existing pet (null if it's a new pet)
    private Uri mPetUri;

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

    // Boolean to track whether Pet has been edited (true) or not (false)
    private boolean mPetHasChanged = false;

    // Projection specifies which columns from db the query will actually use
    private String[] mProjection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT};
    /*
    * OnTouchListener that listens for any user touches on a View, implying that they are modifying
    * the view, and we change the mPetHasChanged boolean to true.
    * */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get URI from calling activity. URI == null if called from FAB
        Intent intent = getIntent();
        mPetUri = intent.getData();

        // If mPetUri == null, set activity title to "Add a Pet", otherwise
        // this is an existing Pet so set activity title to "Edit Pet"
        if (mPetUri == null) {
            setTitle(R.string.editor_activity_title_new_pet);
        } else {
            setTitle(R.string.editor_activity_title_edit_pet);
        }

        // Prepare the loader
        getLoaderManager().initLoader(EDITOR_PET_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields to determine if user has touched
        // or modified them
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
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
                if (!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog();

    }

    /*
    * Required methods for LoaderManager.LoaderCallbacks<Cursor>
    * */
    // called when the system needs a new loader to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Check if mPetUri is null or not
        if (mPetUri == null) {
            return null;
        }

        // Return a CursorLoader for a single pet
        return new CursorLoader(this,   // Parent activity content
                mPetUri,                // Provider content URI to query
                mProjection,            // Columns to include in Cursor
                null,                   // No selection clause
                null,                   // No selection args
                null);                  // Default sort order
    }

    // called when a loader has finished loading data
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Exit early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Set the name, breed, gender, and weight to the selected pet
        if (data.moveToFirst()) {

            String petName = data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            mNameEditText.setText(petName);

            String petBreed = data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_BREED));
            mBreedEditText.setText(petBreed);

            int petGender = data.getInt(data.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
            switch (petGender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_MALE);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_FEMALE);
                    break;
                default:
                    mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
            }

            int petWeight = data.getInt(data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT));
            mWeightEditText.setText(Integer.toString(petWeight));
        }
    }

    // called when a previously created loader is being reset (when you call destroyLoader(int)
    // or when the activity or fragment is destroyed, and thus making its data unavailable.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(0);
        mWeightEditText.setText("");
    }

    // Get user input of Pet from editor and saves Pet into database
    private void savePet() {

        // Read from input fields, use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // Create ContentValues object and put user entered values into corresponding column names
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(nameString)) {
            values.put(PetEntry.COLUMN_PET_NAME, nameString);
        }
        if (!TextUtils.isEmpty(breedString)) {
            values.put(PetEntry.COLUMN_PET_BREED, breedString);
        }
        if (!TextUtils.isEmpty(weightString)) {
            values.put(PetEntry.COLUMN_PET_WEIGHT, weightString);
        }
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);

        // If mPetUri == null, save a new Pet, otherwise
        // this will update an existing Pet
        if (mPetUri == null) {
            // If user has left all fields blank and mGenderSpinner == 0 (GENDER_UNKNOWN),
            // then exit activity w/o adding a Pet
            if (TextUtils.isEmpty(nameString) &&
                    TextUtils.isEmpty(breedString) &&
                    TextUtils.isEmpty(weightString) &&
                    mGender == PetEntry.GENDER_UNKNOWN) {
                Toast.makeText(this, R.string.editor_pet_not_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Add a new Pet
                // Insert new row using PetProvider insert() method and get a URI
                // then use URI to get the row ID.
                Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

                if (uri == null) {
                    // If URI == null, then there was an error with db insertion
                    Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
                } else {
                    // Else db insertion successful and we display Toast
                    Toast.makeText(this, R.string.editor_insert_pet_success, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Update an existing pet
            int rowsUpdated = getContentResolver().update(mPetUri, values, null, null);
            if (rowsUpdated == 0) {
                // If no Pet was updated, then there was an error
                Toast.makeText(this, R.string.editor_update_pet_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Else Pet update successful
                Toast.makeText(this, R.string.editor_update_pet_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
     * Create "Discard Changes" dialog
     * */
    /*private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {*/
    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked the "Discard" button, so exit editing and return
                        // back to parent activity (CatalogActivity)
                        finish();
                    }
                })
                .setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                        @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked the "Keep editing" button, so dismiss the dialog
                        // and continue editing the pet.
                        dialog.dismiss();
                    }
                });
        // Create and show the AlertDialog
        builder.create().show();
    }
}
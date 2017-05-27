package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by namlu on 11-May-17.
 *
 * Define a db schema for Pets
 */

public final class PetContract {

    // Constant for content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    // Constant for the base URI, which is the scheme + content authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible path to the pets table
    public static final String PATH_PETS = "pets";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PetContract(){

    }

    /* Inner class that defines the table contents */
    public static abstract class PetEntry implements BaseColumns {

        // Content URI to access the pet data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        // Name of the database table for pets
        public static final String TABLE_NAME = "pets";
        // Unique ID for pet, Type: INTEGER
        public static final String _ID = BaseColumns._ID;
        // Name of pet, Type: STRING
        public static final String COLUMN_PET_NAME = "name";
        // Breed of pet, Type: STRING
        public static final String COLUMN_PET_BREED = "breed";
        // Gender of pet, Type: INTEGER
        // Possible values are {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
        // or {@link #GENDER_FEMALE}
        public static final String COLUMN_PET_GENDER = "gender";
        // Weight of pet (in kg), Type: INTEGER
        public static final String COLUMN_PET_WEIGHT = "weight";

        // Possible values for pet gender
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }
    }
}

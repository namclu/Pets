package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by namlu on 11-May-17.
 *
 * Define a db schema for Pets
 */

public final class PetContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PetContract(){

    }

    /* Inner class that defines the table contents */
    public static abstract class PetEntry implements BaseColumns {
        // Name of the database table for pets
        public static final String TABLE_NAME = "pets";
        // Unique ID for pet, Type: INTEGER
        public static final String _ID = BaseColumns._ID;
        // Name of pet, Type: STRING
        public static final String COLUMN_NAME_PET_NAME = "name";
        // Breed of pet, Type: STRING
        public static final String COLUMN_NAME_PET_BREED = "breed";
        // Gender of pet, Type: INTEGER
        // Possible values are {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
        // or {@link #GENDER_FEMALE}
        public static final String COLUMN_NAME_PET_GENDER = "gender";
        // Weight of pet (in kg), Type: INTEGER
        public static final String COLUMN_NAME_PET_WEIGHT = "weight";

        // Possible values for pet gender
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}

package com.deepred.subworld;

/**
 * Created by aplicaty on 25/02/16.
 */
public class ICommon {

    // Preferences
    public final static String PREFS_NAME = "subworld_prefs";

    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";

    public static final String FIREBASE_REF = "https://subworld.firebaseio.com/";
    public static final String GEO_FIRE_REF = "https://subworld.firebaseio.com/_geofire";

    // Characters
    public final static int CHRS_NOT_SET = -1;
    public final static int CHRS_ARCHEOLOGIST = 0;
    public final static int CHRS_FORT_TELLER = 1;
    public final static int CHRS_SPY = 2;
    public final static int CHRS_THIEF = 3;



    //Skills
    public final static int DEFAULT_FINDING_ARCH = 5;
    public final static int DEFAULT_HIDING_ARCH = 2;
    public final static int DEFAULT_OPENING_ARCH = 3;
    public final static int DEFAULT_WATCHING_ARCH = 1;
    public final static int DEFAULT_STEALTH_ARCH = 1;
    public final static int DEFAULT_STEAL_ARCH = 1;
    public final static int DEFAULT_DEFENSE_ARCH = 1;

    public final static int DEFAULT_FINDING_FORT = 3;
    public final static int DEFAULT_HIDING_FORT = 1;
    public final static int DEFAULT_OPENING_FORT = 2;
    public final static int DEFAULT_WATCHING_FORT = 3;
    public final static int DEFAULT_STEALTH_FORT = 1;
    public final static int DEFAULT_STEAL_FORT = 1;
    public final static int DEFAULT_DEFENSE_FORT = 2;

    public final static int DEFAULT_FINDING_SPY = 1;
    public final static int DEFAULT_HIDING_SPY = 2;
    public final static int DEFAULT_OPENING_SPY = 1;
    public final static int DEFAULT_WATCHING_SPY = 5;
    public final static int DEFAULT_STEALTH_SPY = 3;
    public final static int DEFAULT_STEAL_SPY = 1;
    public final static int DEFAULT_DEFENSE_SPY = 1;

    public final static int DEFAULT_FINDING_THIEF = 1;
    public final static int DEFAULT_HIDING_THIEF = 2;
    public final static int DEFAULT_OPENING_THIEF = 1;
    public final static int DEFAULT_WATCHING_THIEF = 1;
    public final static int DEFAULT_STEALTH_THIEF = 2;
    public final static int DEFAULT_STEAL_THIEF = 5;
    public final static int DEFAULT_DEFENSE_THIEF = 2;
    public final static int EVOLUTION_FACTOR_ONE = 0;
    public final static int EVOLUTION_FACTOR_TWO = 1;
    public final static int EVOLUTION_FACTOR_THREE = 2;
    public final static int EVOLUTION_FACTOR_FOUR = 3;
    // Treasures
    public final static int TREASURE_TYPE_MONEY = 0;
    public final static int TREASURE_TYPE_JEWELS = 1;
    public final static int TREASURE_TYPE_RELICS = 2;
    public final static int TREASURE_TYPE_SAFEPASS = 3;
    public final static int TREASURE_TYPE_ELECTRONICS = 4;
    public final static int TREASURE_TYPE_GOLD = 5;
    public final static int TREASURE_TYPE_SILVER = 6;
    public final static int TREASURE_TYPE_DIAMONDS = 7;
    public final static int TREASURE_DEFAULT_VALUE_MONEY = 100;
    public final static int TREASURE_DEFAULT_VALUE_JEWELS = 90;
    public final static int TREASURE_DEFAULT_VALUE_RELICS = 120;
    public final static int TREASURE_DEFAULT_VALUE_SAFEPASS = 60;
    public final static int TREASURE_DEFAULT_VALUE_ELECTRONICS = 80;
    public final static int TREASURE_DEFAULT_VALUE_GOLD = 150;
    public final static int TREASURE_DEFAULT_VALUE_SILVER = 130;
    public final static int TREASURE_DEFAULT_VALUE_DIAMONDS = 300;
    public final static int[] defaultTreasureValues = {TREASURE_DEFAULT_VALUE_MONEY,
            TREASURE_DEFAULT_VALUE_JEWELS,
            TREASURE_DEFAULT_VALUE_RELICS,
            TREASURE_DEFAULT_VALUE_SAFEPASS,
            TREASURE_DEFAULT_VALUE_ELECTRONICS,
            TREASURE_DEFAULT_VALUE_GOLD,
            TREASURE_DEFAULT_VALUE_SILVER,
            TREASURE_DEFAULT_VALUE_DIAMONDS};
    // Locations
    public final static int LOCATION_TYPE_USER = 0;
    public final static int LOCATION_TYPE_TREASURE = 1;
    public static float[] EvolutionFactorValues = {1.0f, 0.75f, 0.5f, 0.33f};
    // Matriz con 4 filas, una por cada tipo de personaje
    // Las columnas son cada una de las habilidades
    // Cada habilidad esta compuesta por el valor de la habilidad y el factor de mejora de dicha habilidad
    public static int[][][] skillsTable = {
            {
                    {DEFAULT_FINDING_ARCH, EVOLUTION_FACTOR_ONE},
                    {DEFAULT_HIDING_ARCH, EVOLUTION_FACTOR_TWO},
                    {DEFAULT_OPENING_ARCH, EVOLUTION_FACTOR_THREE},
                    {DEFAULT_WATCHING_ARCH, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_STEALTH_ARCH, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_STEAL_ARCH, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_DEFENSE_ARCH, EVOLUTION_FACTOR_THREE}
            },
            {
                    {DEFAULT_FINDING_FORT, EVOLUTION_FACTOR_ONE} ,
                    {DEFAULT_HIDING_FORT, EVOLUTION_FACTOR_FOUR},
                    {DEFAULT_OPENING_FORT, EVOLUTION_FACTOR_THREE} ,
                    {DEFAULT_WATCHING_FORT, EVOLUTION_FACTOR_ONE},
                    {DEFAULT_STEALTH_FORT, EVOLUTION_FACTOR_THREE} ,
                    {DEFAULT_STEAL_FORT, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_DEFENSE_FORT, EVOLUTION_FACTOR_THREE}
            },
            {
                    {DEFAULT_FINDING_SPY, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_HIDING_SPY, EVOLUTION_FACTOR_THREE},
                    {DEFAULT_OPENING_SPY, EVOLUTION_FACTOR_FOUR} ,
                    {DEFAULT_WATCHING_SPY, EVOLUTION_FACTOR_ONE},
                    {DEFAULT_STEALTH_SPY, EVOLUTION_FACTOR_TWO},
                    {DEFAULT_STEAL_SPY, EVOLUTION_FACTOR_FOUR},
                    {DEFAULT_DEFENSE_SPY, EVOLUTION_FACTOR_THREE}
            },
            {
                    {DEFAULT_FINDING_THIEF, EVOLUTION_FACTOR_FOUR},
                    {DEFAULT_HIDING_THIEF, EVOLUTION_FACTOR_TWO},
                    {DEFAULT_OPENING_THIEF, EVOLUTION_FACTOR_THREE},
                    {DEFAULT_WATCHING_THIEF, EVOLUTION_FACTOR_FOUR},
                    {DEFAULT_STEALTH_THIEF, EVOLUTION_FACTOR_THREE},
                    {DEFAULT_STEAL_THIEF, EVOLUTION_FACTOR_ONE},
                    {DEFAULT_DEFENSE_THIEF, EVOLUTION_FACTOR_THREE}
            }
    };



    // Location related constants
    // The minimum distance to change Updates in meters
    public static final long LOCATION_MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 meters

    // The minimum time between updates in milliseconds
    public static final long LOCATION_MIN_TIME_BW_UPDATES = 1000 * 10; // 10 seconds

    public static final long LOCATION_GOOGLE_TIME_INTERVAL = 15000;

    public static final long TIME_ESTIMATED_FOR_QUERY_COMPLETION =  10000; // In milliseconds

    public static final int MIN_USERS_IN_RANGE = 10;
    public static final int MAX_USERS_IN_RANGE = 30;

    public static final int MAX_RANGE = 1500; // In meters
    public static final int RANGE_VARIATION = 100; // In meters
    public static final int MIN_RANGE = 40; // In meters
    public static final int SMALL_RANGE_VARIATION = 10; // In meters

    public static final int DISABLE_GPS_IF_NO_LOCATIONS_AFTER = 10000; // Milliseconds

    // Refresh rate for the screen with other user distance
    // la frecuencia a la que se debe consultar los puntos en funcion de la distancia de los otros usuarios

    // El provider de localizacion cambia cuando hay otros usuarios a una distancia menor de XXX m




    // Distance tables
    public static final int DISTANCE_RANGE_0 = 10; // In meters
    public static final int DISTANCE_RANGE_1 = 20; // In meters
    public static final int DISTANCE_RANGE_2 = 30; // In meters
    public static final int DISTANCE_RANGE_3 = 60; // In meters
    public static final int DISTANCE_RANGE_4 = 100; // In meters
    public static final int DISTANCE_RANGE_5 = 200; // In meters

    public static final int[] distanceRanges = {
            DISTANCE_RANGE_0, DISTANCE_RANGE_1, DISTANCE_RANGE_2, DISTANCE_RANGE_3, DISTANCE_RANGE_4, DISTANCE_RANGE_5
    };

    // first field is the result of the diference of skills, second field is DISTANCE_RANGE_x
    public static final boolean[][] distanceTable = {
            {true,false,false,false,false,false},
            {true,true,false,false,false,false},
            {true,true,false,false,false,false},
            {true,true,true,false,false,false},
            {true,true,true,false,false,false},
            {true,true,true,false,false,false},
            {true,true,true,true,false,false},
            {true,true,true,true,true,false},
            {true,true,true,true,true,true}
    };


}
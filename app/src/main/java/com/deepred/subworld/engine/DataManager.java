package com.deepred.subworld.engine;

import android.location.Location;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ICommon;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.ICallbacks;
import com.deepred.subworld.utils.MyUserManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.Map;

/**
 * Created by aplicaty on 25/02/16.
 */
class DataManager implements GeoQueryEventListener {
    private static volatile DataManager INSTANCE;
    private static Object obj = new Object();
    private String TAG = "DataManager";
    private Firebase dbRef = null;
    private GeoFire dbGeoRef = null;
    private GeoQuery geoQuery;
    private String uid = null;
    private User user = null;

    private DataManager(){
        Firebase.setAndroidContext(ApplicationHolder.getApp().getApplicationContext());
        dbRef = new Firebase(ICommon.FIREBASE_REF);
        dbGeoRef = new GeoFire(new Firebase(ICommon.GEO_FIRE_REF));
    }

    static DataManager getInstance(){
        if(INSTANCE == null){
            synchronized(obj){
                if(INSTANCE == null){
                    INSTANCE = new DataManager();
                }
            }
        }
        return INSTANCE;
    }

    Firebase getDbRef() {
        return dbRef;
    }

    void loginOrRegister(final String eMail, final String password, final ICallbacks.ILoginCallbacks cb) {
        dbRef.authWithPassword(eMail, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                uid = authData.getUid();
                cb.onLoginOk(true);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
                if (firebaseError.getCode() == FirebaseError.INVALID_EMAIL || firebaseError.getCode() == FirebaseError.USER_DOES_NOT_EXIST) {
                    dbRef.createUser(eMail, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            Log.d(TAG, "Successfully created user account with uid: " + result.get("uid"));
                            uid = (String) result.get("uid");
                            cb.onLoginOk(false);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // there was an error
                            Log.d(TAG, "Error al intentar el login en Firebase.");
                            cb.onLoginError();
                        }
                    });
                } else if (firebaseError.getCode() == FirebaseError.INVALID_PASSWORD) {
                    cb.onLoginError();
                }
            }
        });
    }

    void getUser() {
        Firebase userRef = dbRef.child("users").child(uid);

        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    user = snapshot.getValue(User.class);
                    user.setUid(uid);
                    MyUserManager.getInstance().updateUser(user);
                } else {
                    Log.d(TAG, "Tenemos credenciales en el dispositivo, pero no hay registro de usuario en BBDD");
                    MyUserManager.getInstance().updateUser(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError databaseError) {
                Log.d(TAG, "The read failed: " + databaseError.getMessage());
            }
        });

    }

    void getUser(final String _uid, final ICallbacks.IUserCallbacks cb) {
        Firebase userRef = dbRef.child("users").child(_uid);

        // Attach an listener to read the data at our posts reference
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.hasChildren()) {
                    user = snapshot.getValue(User.class);
                    user.setUid(_uid);
                    cb.onUserChange(user);
                } else {
                    Log.d(TAG, "Tenemos credenciales en el dispositivo, pero no hay registro de usuario en BBDD");
                    cb.onUserChange(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "The read failed: " + firebaseError.getMessage());
            }
        });
    }


    void checkName(String name, final ICallbacks.INameCheckCallbacks cb) {
        Firebase ref = dbRef.child("usernames").child(name.toLowerCase());
        // Hacer la consulta ignorecase

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (0 < snapshot.getChildrenCount()) {
                    // Ya existe el nombre
                    cb.onNameAlreadyExists();
                } else {
                    cb.onValidUsername();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    void storeUsername(String name, final ICallbacks.INameStoringCallbacks cb) {
        Firebase ref = dbRef.child("usernames");
        ref.push().setValue(name, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                    cb.onStored(false);
                } else {
                    Log.d(TAG, "Data saved successfully.");
                    cb.onStored(true);
                }
            }
        });
    }

    void saveUser(User user, final ICallbacks.IUserInitialStoreCallbacks cb) {
        Firebase ref = dbRef.child("users").child(user.getUid());
        ref.setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                    cb.onUserCreationError();
                } else {
                    Log.d(TAG, "Data saved successfully.");
                    cb.onUserCreationSuccess();
                }
            }
        });
    }

    /*
        rad is actual range radius in kilometers
     */
    void queryLocations(final Location l, final double rad) {
        Log.d(TAG, "QueryLocations");
        if(uid == null)
            return;

        dbGeoRef.setLocation(uid, new GeoLocation(l.getLatitude(), l.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, FirebaseError error) {
                if (error != null) {
                    Log.e(TAG, "There was an error saving the location to GeoFire: " + error);
                } else {
                    Log.d(TAG, "Location saved on server successfully!");

                    if (geoQuery == null) {
                        geoQuery = dbGeoRef.queryAtLocation(new GeoLocation(l.getLatitude(), l.getLongitude()), rad);
                        geoQuery.addGeoQueryEventListener(DataManager.getInstance());
                    } else {
                        geoQuery.setCenter(new GeoLocation(l.getLatitude(), l.getLongitude()));
                        geoQuery.setRadius(rad);
                    }
                }
            }
        });
    }

    public void stopQueryingLocations() {
        geoQuery.removeAllListeners();
    }

    String getUid() {
        return uid;
    }


    @Override
    public void onKeyEntered(String id, GeoLocation geoLocation) {
        Log.d(TAG, "onKeyEntered: " + id);
        UsersViewRangeManager.getInstance().add(id, geoLocation, id.equals(uid));
    }

    @Override
    public void onKeyExited(String id) {
        Log.d(TAG, "onKeyExited: " + id);
        UsersViewRangeManager.getInstance().remove(id);
    }

    @Override
    public void onKeyMoved(String id, GeoLocation geoLocation) {
        Log.d(TAG, "onKeyMoved: " + id);
        UsersViewRangeManager.getInstance().add(id, geoLocation, id.equals(uid));
    }

    @Override
    public void onGeoQueryReady() {
        Log.d(TAG, "onGeoQueryReady");
        UsersViewRangeManager.getInstance().queryCompleted();
    }

    @Override
    public void onGeoQueryError(FirebaseError firebaseError) {
        Log.d(TAG, "onGeoQueryError");
    }
}

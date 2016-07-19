package com.deepred.subworld.utils;

import android.os.IInterface;

import com.deepred.subworld.model.User;

public class ICallbacks {

    /**
     *
     */
    public interface ILoginCallbacks {
        void onLoginOk(boolean wait4User);
        void onLoginError();
    }


    public interface INameCheckCallbacks {
        void onValidUsername();
        void onNameAlreadyExists();
    }


    public interface  IChangeCallbacks<T> {
        void onChange(T changed);
    }

    public interface IStoreCallbacks {
        void onError();
        void onSuccess();
    }
}
package com.deepred.subworld.utils;

import com.deepred.subworld.model.User;

public class ICallbacks {

    /**
     *
     */
    public interface ILoginCallbacks {
        void onLoginOk(boolean wait4User);

        void onLoginError();
    }

    /**
     *
     */
    public interface IUserCallbacks {
        void onUserChange(User user);
    }


    public interface INameCheckCallbacks {
        void onValidUsername();

        void onNameAlreadyExists();
    }

    public interface INameStoringCallbacks {
        void onStored(boolean ok);
    }

    public interface IUserInitialStoreCallbacks {
        void onUserCreationError();

        void onUserCreationSuccess();
    }

}
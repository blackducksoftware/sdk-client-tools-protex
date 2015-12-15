package com.blackducksoftware.sdk.protex.client.util;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

// TODO doc
public class SimpleCallbackHandler implements CallbackHandler {

    private final String username;

    private final char[] password;

    private SimpleCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(callback, "Only " + NameCallback.class.getName() + " and " + PasswordCallback.class.getName()
                            + " are supported");
                }
            }
        }
    }

    public static CallbackHandler create(String username, String password) {
        checkNotNull(username, "Username may not be null");
        checkNotNull(password, "Password may not be null");

        return new SimpleCallbackHandler(username, password.toCharArray());
    }

    public static CallbackHandler create(String username, char[] password) {
        checkNotNull(username, "Username may not be null");
        checkNotNull(password, "Password may not be null");

        return new SimpleCallbackHandler(username, password);
    }

    private static void checkNotNull(Object var, String message) {
        if (var == null) {
            throw new IllegalArgumentException(message);
        }
    }

}

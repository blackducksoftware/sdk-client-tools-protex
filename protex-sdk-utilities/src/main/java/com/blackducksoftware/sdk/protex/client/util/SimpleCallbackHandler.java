/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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

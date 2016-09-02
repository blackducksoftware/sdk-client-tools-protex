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

import org.apache.ws.security.WSPasswordCallback;

public class ProgrammedPasswordCallback implements CallbackHandler {

    private final CallbackHandler callbackHandler;

    public ProgrammedPasswordCallback(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    handle((WSPasswordCallback) callback);
                } else {
                    throw new UnsupportedCallbackException(callback, "Unsupported callback type " + (callback != null ? callback.getClass().getName() : null)
                            + " provided. Only " + WSPasswordCallback.class.getName() + " is supported ");
                }
            }
        }
    }

    protected void handle(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifier();
        NameCallback usernameCallback = new NameCallback("Username: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);

        callbackHandler.handle(new Callback[] { usernameCallback, passwordCallback });

        String username = usernameCallback.getName();

        if (username == null) {
            throw new UnsupportedCallbackException(callback, "Username not set in client. Call constructor method " + ProgrammedPasswordCallback.class.getName()
                    + "(\"<your username>\", \"<your password>\")");
        }

        // set the password for our outgoing message.
        if (username.equals(identifier)) {
            callback.setPassword(new String(passwordCallback.getPassword()));
        } else {
            throw new UnsupportedCallbackException(callback, "Password not set in client. Call constructor method " + ProgrammedPasswordCallback.class.getName()
                    + "(\"<your username>\", \"<your password>\")");
        }
    }

}

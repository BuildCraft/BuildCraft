/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

public class LoadingException extends Exception {
    private static final long serialVersionUID = -3439641111545783074L;

    public LoadingException() {}

    public LoadingException(String message) {
        super(message);
    }

    public LoadingException(Throwable cause) {
        super(cause);
    }

    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

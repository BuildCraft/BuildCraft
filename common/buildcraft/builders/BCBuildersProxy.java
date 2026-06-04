/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): Forge SidedProxy / IGuiHandler deferred
package buildcraft.builders;

public abstract class BCBuildersProxy {
    private static BCBuildersProxy proxy = null;

    public static BCBuildersProxy getProxy() {
        return proxy;
    }

    public static void setProxy(BCBuildersProxy p) {
        proxy = p;
    }

    public void fmlPreInit() {}
    public void fmlInit() {}
    public void fmlPostInit() {}
}

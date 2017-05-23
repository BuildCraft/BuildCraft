/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

/** Holds the current {@link IAdvDebugTarget}. Use {@link DebugRenderHelper} for helpers when rendering debuggables. */
public enum BCAdvDebugging {
    INSTANCE;

    private IAdvDebugTarget target = null;
    IAdvDebugTarget targetClient = null;

    public static boolean isBeingDebugged(IAdvDebugTarget target) {
        return INSTANCE.target == target;
    }

    public static void setCurrentDebugTarget(IAdvDebugTarget target) {
        if (INSTANCE.target != null) {
            INSTANCE.target.disableDebugging();
        }
        INSTANCE.target = target;
    }

    public static void setClientDebugTarget(IAdvDebugTarget target) {
        INSTANCE.targetClient = target;
    }

    public void onServerPostTick() {
        if (target != null) {
            if (!target.doesExistInWorld()) {
                target.disableDebugging();
                target = null;
            } else {
                target.sendDebugState();
            }
        }
    }
}

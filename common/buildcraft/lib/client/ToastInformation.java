/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

import buildcraft.lib.gui.ISimpleDrawable;

// STUB(R.Chen): Forge IToast.draw(GuiToast, long) → Yarn Toast.draw(DrawContext, ToastManager, long).
// Full toast rendering (texture + localized text) is deferred — Phase 10.
public class ToastInformation implements Toast {

    public final String localeKey;
    public final ISimpleDrawable icon;
    private final Object type;

    public ToastInformation(String localeKey, ISimpleDrawable icon, Object type) {
        this.localeKey = localeKey;
        this.icon = icon;
        this.type = type;
    }

    public ToastInformation(String localeKey, ISimpleDrawable icon) {
        this(localeKey, icon, new Object());
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        // STUB(R.Chen): toast rendering deferred — Phase 10
        return startTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Object getType() {
        return type;
    }
}

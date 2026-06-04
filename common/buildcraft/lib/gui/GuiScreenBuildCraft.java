/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

// STUB(R.Chen): Forge GuiScreen → Fabric Screen. GUI render — Phase 5.
@Environment(EnvType.CLIENT)
public class GuiScreenBuildCraft extends Screen {
    public GuiScreenBuildCraft(Text title) { super(title); }
}

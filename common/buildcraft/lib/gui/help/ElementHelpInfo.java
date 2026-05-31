/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.help;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.pos.IGuiArea;

@Environment(EnvType.CLIENT)
public class ElementHelpInfo {
    public final String title;
    public final String text;

    public ElementHelpInfo(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public static class HelpPosition {
        public final ElementHelpInfo info;
        public final IGuiArea area;

        public HelpPosition(ElementHelpInfo info, IGuiArea area) {
            this.info = info;
            this.area = area;
        }
    }
}

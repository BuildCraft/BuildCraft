/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GL11/Tessellator/DynamicTexture client rendering deferred
package buildcraft.lib.client.sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DynamicTextureBC {
    public final int width, height;

    public DynamicTextureBC(int iWidth, int iHeight) {
        this.width = iWidth;
        this.height = iHeight;
    }

    public void setColord(int x, int y, double r, double g, double b, double a) {}
    public void setColori(int x, int y, int r, int g, int b, int a) {}
    public void setColor(int x, int y, int color) {}
    public void upload() {}
    public void delete() {}
}

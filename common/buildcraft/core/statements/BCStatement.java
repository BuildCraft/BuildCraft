/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BCStatement implements IStatement {

    protected final String uniqueTag;

    /** UniqueTag accepts multiple possible tags, use this feature to migrate to more standardised tags if needed,
     * otherwise just pass a single string. The first passed string will be the one used when saved to disk.
     *
     * @param uniqueTag */
    public BCStatement(String... uniqueTag) {
        this.uniqueTag = uniqueTag[0];
        for (String tag : uniqueTag) {
            StatementManager.statements.put(tag, this);
        }
    }

    @Override
    public String getUniqueTag() {
        return uniqueTag;
    }

    @Override
    public int maxParameters() {
        return 0;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public IStatement rotateLeft() {
        return this;
    }

    @Override
    public IStatement[] getPossible() {
        return new IStatement[] { this };
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final TextureAtlasSprite getGuiSprite() {
        SpriteHolder holder = getSpriteHolder();
        return holder == null ? null : holder.getSprite();
    }

    @SideOnly(Side.CLIENT)
    public abstract SpriteHolder getSpriteHolder();
}

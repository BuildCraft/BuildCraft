/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

public abstract class BCStatement implements IStatement {

    protected final String uniqueTag;
    // This is not final so sub-classes can change it if they need later on
    protected ResourceLocation location;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    /** UniqueTag accepts multiple possible tags, use this feature to migrate to more standardized tags if needed,
     * otherwise just pass a single string. The first passed string will be the one used when saved to disk.
     *
     * @param uniqueTag */
    public BCStatement(String... uniqueTag) {
        this(new ResourceLocation("buildcraftcore:items/triggers/" + uniqueTag[0]), uniqueTag);
    }

    /** UniqueTag accepts multiple possible tags, use this feature to migrate to more standardized tags if needed,
     * otherwise just pass a single string. The first passed string will be the one used when saved to disk.
     * 
     * @deprecated use the above one, and set the ResourceLocation seperately
     *
     * @param uniqueTag */
    protected BCStatement(ResourceLocation loc, String... uniqueTag) {
        this.uniqueTag = uniqueTag[0];
        for (String tag : uniqueTag) {
            StatementManager.statements.put(tag, this);
        }
        location = loc;
        MinecraftForge.EVENT_BUS.register(this);
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
    public IStatementParameter createParameter(int index) {
        return null;
    }

    protected void setLocation(String newLocation) {
        location = new ResourceLocation(newLocation);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void stitchTextures(TextureStitchEvent.Pre event) {
        sprite = event.map.registerSprite(location);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        return sprite;
    }
}

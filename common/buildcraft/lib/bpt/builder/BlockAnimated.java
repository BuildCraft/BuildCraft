/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt.builder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.data.LoadingException;

public class BlockAnimated extends AbstractAnimatedElement {
    private final IBlockState state;
    private final Vec3d startPos, endPos;
    private final long endMotion;

    public static BlockAnimated create(IBlockState state, Vec3d startPos, Vec3d endPos, long startMotion) {
        throw new AbstractMethodError("IMPLEMENT THIS YOU %&£#");
    }

    public BlockAnimated(IBlockState state, Vec3d startPos, Vec3d endPos, long startMotion, long endMotion, long endExistance) {
        super(startMotion, endExistance);
        this.state = state;
        this.startPos = startPos;
        this.endPos = endPos;
        this.endMotion = endMotion;
    }

    public BlockAnimated(NBTTagCompound nbt) throws LoadingException {
        super(nbt);
        this.state = NBTUtils.readEntireBlockState(nbt.getCompoundTag("state"));
        this.startPos = NBTUtils.readVec3d(nbt.getTag("startPos"));
        this.endPos = NBTUtils.readVec3d(nbt.getTag("endPos"));
        this.endMotion = nbt.getLong("endMotion");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("state", NBTUtils.writeEntireBlockState(state));
        nbt.setTag("startPos", NBTUtils.writeVec3d(startPos));
        nbt.setTag("endPos", NBTUtils.writeVec3d(endPos));
        return nbt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(VertexBuffer vb, long now, float partialTicks) {
        setTranslation(vb, now, partialTicks);

        // IBakedModel baked = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

        vb.setTranslation(0, 0, 0);
    }

    @SideOnly(Side.CLIENT)
    private void setTranslation(VertexBuffer vb, long now, float partialTicks) {
        throw new AbstractMethodError("Implement this!");
    }
}

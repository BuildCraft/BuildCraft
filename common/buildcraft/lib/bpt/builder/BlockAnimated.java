/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
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

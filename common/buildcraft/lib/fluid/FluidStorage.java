package buildcraft.lib.fluid;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class FluidStorage extends FluidTank implements INBTSerializable<NBTTagCompound> {
    protected String name;
    protected TileEntity tile;

    protected static Map<Fluid, Integer> fluidColors = new HashMap<>();

    public FluidStorage(String name, int capacity, TileEntity tile) {
        super(capacity);
        this.name = name;
        this.tile = tile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        if(fluid != null) {
            return fluid.amount;
        }
        return 0;
    }

    public boolean canAccept(FluidStack fluidStack) {
        return fluid != null && fluid.isFluidEqual(fluidStack);
    }

    public void add(FluidStack fluidStack) {
        if(fluidStack == null) {
            return;
        }
        if(this.canAccept(fluidStack) && fluid != null) {
            fluid.amount += fluidStack.amount;
        } else if(fluid == null) {
            fluid = fluidStack.copy();
        }
    }

    public Fluid getFluidFluid() {
        if(fluid != null) {
            return fluid.getFluid();
        }
        return null;
    }

    public String getFluidName() {
        if(getFluidFluid() != null) {
            return getFluidFluid().getName();
        }
        return "n/a";
    }

    public int getFluidColor() {
        if(getFluidFluid() != null) {
            if(!fluidColors.containsKey(getFluidFluid())) {
                try {
                    TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
                    String flow = getFluidFluid().getFlowing().toString();
                    TextureAtlasSprite sprite;
                    if(map.getTextureExtry(flow) != null) {
                        sprite = map.getTextureExtry(flow);
                    } else {
                        sprite = map.registerSprite(getFluidFluid().getFlowing());
                    }
                    int[] pixels = sprite.getFrameTextureData(0)[0];
                    int pixel = pixels[pixels.length / 2];
                    // order: argb -> abgr
                    byte[] bytes = ByteBuffer.allocate(4).putInt(pixel).array();
                    int a = ((int) bytes[0]) & 0xFF;
                    int r = ((int) bytes[1]) & 0xFF;
                    int g = ((int) bytes[2]) & 0xFF;
                    int b = ((int) bytes[3]) & 0xFF;
                    fluidColors.put(getFluidFluid(), ((a & 0xff) << 24) + ((b & 0xff) << 16) + ((g & 0xff) << 8) + (r & 0xff));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return fluidColors.get(getFluidFluid());
        }
        return 0xFF_00_00_00;
    }

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound tankData = new NBTTagCompound();
        super.writeToNBT(tankData);
        nbt.setTag(name, tankData);
        return nbt;
    }

    @Override
    public final FluidTank readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(name)) {
            setFluid(null);
            NBTTagCompound tankData = nbt.getCompoundTag(name);
            super.readFromNBT(tankData);
        }
        return this;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.readFromNBT(nbt);
    }

    public String getDebugString() {
        return getAmount() + " / " + capacity + " MB of " + getFluidName();
    }

    public void writeToBuffer(ByteBuf buffer) {
        NBTTagCompound tankData = new NBTTagCompound();
        super.writeToNBT(tankData);
        ByteBufUtils.writeTag(buffer, tankData);
    }

    public void readFromBuffer(ByteBuf buffer) {
        NBTTagCompound tankData = ByteBufUtils.readTag(buffer);
        super.readFromNBT(tankData);
    }
}

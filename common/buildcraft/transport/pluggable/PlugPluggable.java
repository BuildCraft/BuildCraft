package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.client.model.ModelKeyPlug;

public class PlugPluggable extends PipePluggable {
    public enum Material {
        COBBLESTONE(0);

        public final int id;
        public static final TIntObjectMap<Material> ID_MAP = new TIntObjectHashMap<>();

        Material(int id) {
            this.id = id;
        }
    }

    static {
        for (Material m : Material.values()) {
            Material.ID_MAP.put(m.id, m);
        }
    }

    private Material material;

    public PlugPluggable() {
        material = Material.COBBLESTONE;
    }

    public PlugPluggable(int id) {
        super();
        material = Material.ID_MAP.get(id);
        if (material == null) {
            material = Material.COBBLESTONE;
        }
    }

    public PlugPluggable(Material material) {
        this.material = material;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("id", (byte) material.id);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        int id = nbt.hasKey("id") ? nbt.getByte("id") : 0;
        material = Material.ID_MAP.get(id);
        if (material == null) {
            material = Material.COBBLESTONE;
        }
    }

    @Override
    public ItemStack[] getDropItems(IPipeTile pipe) {
        return new ItemStack[] { new ItemStack(BuildCraftTransport.plugItem, 1, material.id) };
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
        float[][] bounds = new float[3][2];
        // X START - END
        bounds[0][0] = 0.25F;
        bounds[0][1] = 0.75F;
        // Y START - END
        bounds[1][0] = 0.125F;
        bounds[1][1] = 0.251F;
        // Z START - END
        bounds[2][0] = 0.25F;
        bounds[2][1] = 0.75F;

        MatrixTranformations.transform(bounds, side);
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelKeyPlug getModelRenderKey(EnumWorldBlockLayer layer, EnumFacing side) {
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            return new ModelKeyPlug(side, material);
        }
        return null;
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeByte(material.id);
    }

    @Override
    public void readData(ByteBuf data) {
        material = Material.ID_MAP.get(data.readByte());
        if (material == null) {
            material = Material.COBBLESTONE;
        }
    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        return ((PlugPluggable) o).material != material;
    }
}

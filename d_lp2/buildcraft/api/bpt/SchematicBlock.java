package buildcraft.api.bpt;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public abstract class SchematicBlock extends Schematic {
    /** Stores the offset of this block. */
    protected BlockPos offset;

    public final Block block;
    protected IBlockState state;

    public SchematicBlock(IBlockState state) {
        this.block = state.getBlock();
        this.state = block.getDefaultState();
    }

    public SchematicBlock(NBTTagCompound nbt, BlockPos offset) throws SchematicException {
        String regName = nbt.getString("block");
        this.block = Block.REGISTRY.getObject(new ResourceLocation(regName));
        if (block == null) {
            throw new SchematicException("Unknown block name " + regName);
        }
        if (nbt.hasKey("state")) {
            NBTTagCompound props = nbt.getCompoundTag("state");
            for (IProperty<?> prop : state.getPropertyNames()) {
                readStateProperty(props, prop);
            }
        }
        this.offset = offset;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", block.getRegistryName().toString());
        if (state.getProperties().size() > 0) {
            NBTTagCompound props = new NBTTagCompound();
            for (IProperty<?> prop : state.getPropertyNames()) {
                writeStateProperty(props, prop);
            }
            nbt.setTag("state", props);
        }
        return nbt;
    }

    private <V extends Comparable<V>> void readStateProperty(NBTTagCompound props, IProperty<V> property) throws SchematicException {
        String name = props.getString(property.getName());
        for (V value : property.getAllowedValues()) {
            if (name.equals(property.getName(value))) {
                state = state.withProperty(property, value);
                return;
            }
        }
        throw new SchematicException("Unknown state property value " + name + " for property " + property);
    }

    private <V extends Comparable<V>> void writeStateProperty(NBTTagCompound props, IProperty<V> prop) {
        V value = state.getValue(prop);
        props.setString(prop.getName(), prop.getName(value));
    }

    @Override
    public void rotate(Rotation rotation) {
        state = state.withRotation(rotation);
    }

    @Override
    public void mirror(Mirror mirror) {
        state = state.withMirror(mirror);
    }

    @Override
    public void translate(Vec3i by) {
        offset = offset.add(by);
    }
}

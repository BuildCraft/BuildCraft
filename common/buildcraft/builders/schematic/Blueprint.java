package buildcraft.builders.schematic;

import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class Blueprint extends Snapshot {
    public final List<SchematicBlock> schematicBlocks = new ArrayList<>();

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("schematicBlocks", NBTUtilBC.writeCompoundList(schematicBlocks.stream().map(SchematicBlock::serializeNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        NBTUtilBC.readCompoundList(nbt.getTagList("schematicBlocks", Constants.NBT.TAG_COMPOUND)).map(schematicBlockTag -> {
            SchematicBlock schematicBlock = new SchematicBlock();
            schematicBlock.deserializeNBT(schematicBlockTag);
            return schematicBlock;
        }).forEach(schematicBlocks::add);
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }
}

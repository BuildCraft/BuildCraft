package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class SchematicEntity implements INBTSerializable<NBTTagCompound> {
    public NBTTagCompound entityNbt;
    public Vec3d pos;
    public BlockPos hangingPos;
    public EnumFacing hangingFacing;
    public Rotation entityRotation = Rotation.NONE;
    public List<ItemStack> requiredItems = new ArrayList<>();
    public List<Fluid> requiredFluids = new ArrayList<>();

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public SchematicEntity getRotated(Rotation rotation) {
        SchematicEntity schematicEntity = new SchematicEntity();
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.add(rotation);
        schematicEntity.requiredItems = requiredItems;
        schematicEntity.requiredFluids = requiredFluids;
        return schematicEntity;
    }


    @SuppressWarnings("Duplicates")
    public Entity build(World world, BlockPos basePos) {
        Vec3d placePos = new Vec3d(basePos).add(pos);
        BlockPos placeHangingPos = basePos.add(hangingPos);
        NBTTagCompound newEntityNbt = new NBTTagCompound();
        entityNbt.getKeySet().stream()
                .map(key -> Pair.of(key, entityNbt.getTag(key)))
                .forEach(kv -> newEntityNbt.setTag(kv.getKey(), kv.getValue()));
        newEntityNbt.setTag("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.setUniqueId("UUID", UUID.randomUUID());
        if (Stream.of("TileX", "TileY", "TileZ", "Facing").allMatch(newEntityNbt::hasKey)) {
            newEntityNbt.setInteger("TileX", placeHangingPos.getX());
            newEntityNbt.setInteger("TileY", placeHangingPos.getY());
            newEntityNbt.setInteger("TileZ", placeHangingPos.getZ());
            newEntityNbt.setByte("Facing", (byte) hangingFacing.getHorizontalIndex());
        }
        Entity entity = EntityList.createEntityFromNBT(newEntityNbt, world);
        if (entity != null) {
//            entity.setLocationAndAngles(
//                    placePos.xCoord,
//                    placePos.yCoord,
//                    placePos.zCoord,
////                    entityRotation == Rotation.NONE
////                            ? entity.rotationYaw
////                            : entity.rotationYaw + (entity.rotationYaw - entity.getRotatedYaw(entityRotation)),
//                    entity.rotationYaw,
//                    entity.rotationPitch
//            );
            world.spawnEntity(entity);
        }
        return entity;
    }

    @SuppressWarnings("Duplicates")
    public Entity buildWithoutChecks(World world, BlockPos basePos) {
        return build(world, basePos);
    }
}

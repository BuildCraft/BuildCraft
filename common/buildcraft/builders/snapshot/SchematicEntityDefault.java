/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class SchematicEntityDefault implements ISchematicEntity<SchematicEntityDefault> {
    private NBTTagCompound entityNbt;
    private Vec3d pos;
    private BlockPos hangingPos;
    private EnumFacing hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    public static boolean predicate(SchematicEntityContext context) {
        ResourceLocation registryName = EntityList.getKey(context.entity);
        return registryName != null &&
            RulesLoader.READ_DOMAINS.contains(registryName.getResourceDomain()) &&
            RulesLoader.getRules(context.entity).stream().anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        entityNbt = context.entity.serializeNBT();
        pos = context.entity.getPositionVector().subtract(new Vec3d(context.basePos));
        if (context.entity instanceof EntityHanging) {
            EntityHanging entityHanging = (EntityHanging) context.entity;
            hangingPos = entityHanging.getHangingPosition().subtract(context.basePos);
            hangingFacing = entityHanging.getHorizontalFacing();
        } else {
            hangingPos = new BlockPos(pos);
            hangingFacing = EnumFacing.NORTH;
        }
    }

    @Override
    public Vec3d getPos() {
        return pos;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(SchematicEntityContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(context.entity);
        List<ItemStack> requiredItems = new ArrayList<>();
        if (rules.stream().noneMatch(rule -> rule.doNotCopyRequiredItemsFromBreakBlockDrops)) {
            if (context.world instanceof FakeWorld) {
                requiredItems.addAll(((FakeWorld) context.world).killEntityAndGetDrops(context.entity));
            }
        }
        if (rules.stream().map(rule -> rule.requiredItems).anyMatch(Objects::nonNull)) {
            requiredItems.clear();
            rules.stream()
                .map(rule -> rule.requiredItems)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(requiredItems::add);
        }
        return requiredItems;
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids(SchematicEntityContext context) {
        return Collections.emptyList();
    }

    @Override
    public SchematicEntityDefault getRotated(Rotation rotation) {
        SchematicEntityDefault schematicEntity = new SchematicEntityDefault();
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.add(rotation);
        return schematicEntity;
    }

    @Override
    public Entity build(World world, BlockPos basePos) {
        Vec3d placePos = new Vec3d(basePos).add(pos);
        BlockPos placeHangingPos = basePos.add(hangingPos);
        NBTTagCompound newEntityNbt = new NBTTagCompound();
        entityNbt.getKeySet().stream()
            .map(key -> Pair.of(key, entityNbt.getTag(key)))
            .forEach(kv -> newEntityNbt.setTag(kv.getKey(), kv.getValue()));
        newEntityNbt.setTag("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.setUniqueId("UUID", UUID.randomUUID());
        boolean rotate = false;
        if (Stream.of("TileX", "TileY", "TileZ", "Facing").allMatch(newEntityNbt::hasKey)) {
            newEntityNbt.setInteger("TileX", placeHangingPos.getX());
            newEntityNbt.setInteger("TileY", placeHangingPos.getY());
            newEntityNbt.setInteger("TileZ", placeHangingPos.getZ());
            newEntityNbt.setByte("Facing", (byte) hangingFacing.getHorizontalIndex());
        } else {
            rotate = true;
        }
        Entity entity = EntityList.createEntityFromNBT(newEntityNbt, world);
        if (entity != null) {
            if (rotate) {
                entity.setLocationAndAngles(
                    placePos.xCoord,
                    placePos.yCoord,
                    placePos.zCoord,
                    entity.rotationYaw + (entity.rotationYaw - entity.getRotatedYaw(entityRotation)),
                    entity.rotationPitch
                );
            }
            world.spawnEntity(entity);
        }
        return entity;
    }

    @Override
    public Entity buildWithoutChecks(World world, BlockPos basePos) {
        return build(world, basePos);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("entityNbt", entityNbt);
        nbt.setTag("pos", NBTUtilBC.writeVec3d(pos));
        nbt.setTag("hangingPos", NBTUtil.createPosTag(hangingPos));
        nbt.setTag("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.setTag("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        entityNbt = nbt.getCompoundTag("entityNbt");
        pos = NBTUtilBC.readVec3d(nbt.getTag("pos"));
        hangingPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("hangingPos"));
        hangingFacing = NBTUtilBC.readEnum(nbt.getTag("hangingFacing"), EnumFacing.class);
        entityRotation = NBTUtilBC.readEnum(nbt.getTag("entityRotation"), Rotation.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicEntityDefault that = (SchematicEntityDefault) o;

        return entityNbt.equals(that.entityNbt) &&
            pos.equals(that.pos) &&
            hangingPos.equals(that.hangingPos) &&
            hangingFacing == that.hangingFacing &&
            entityRotation == that.entityRotation;
    }

    @Override
    public int hashCode() {
        int result = entityNbt.hashCode();
        result = 31 * result + pos.hashCode();
        result = 31 * result + hangingPos.hashCode();
        result = 31 * result + hangingFacing.hashCode();
        result = 31 * result + entityRotation.hashCode();
        return result;
    }
}

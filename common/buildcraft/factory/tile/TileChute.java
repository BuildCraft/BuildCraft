package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.block.BlockChute;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TileChute extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final int PICKUP_RADIUS = 3;
    private static final int PICKUP_MAX = 3;
    public final ItemHandlerSimple inv = itemManager.addInvHandler("inv", 4, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    private final MjBattery battery = new MjBattery(1000_000);
    private int progress = 0;

    public static boolean hasInventoryAtPosition(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        return ItemTransactorHelper.getTransactor(tile, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void pickupItems(EnumFacing currentSide) {
        world.getEntitiesWithinAABB(
                EntityItem.class,
                new AxisAlignedBB(pos.offset(currentSide, PICKUP_RADIUS)).expandXyz(PICKUP_RADIUS)
        ).stream()
                .limit(PICKUP_MAX)
                .forEach(entityItem -> {
                    ItemStack stack = entityItem.getEntityItem();
                    stack = inv.insert(stack, false, false);
                    if (stack.isEmpty()) {
                        entityItem.setDead();
                    } else {
                        entityItem.setEntityItemStack(stack);
                    }
                });
    }

    private void putInNearInventories(EnumFacing currentSide) {
        List<EnumFacing> sides = new ArrayList<>(Arrays.asList(EnumFacing.values()));
        Collections.shuffle(sides, new Random());
        sides.removeIf(Predicate.isEqual(currentSide));
        Stream.<Pair<EnumFacing, ICapabilityProvider>>concat(
                sides.stream()
                        .map(side -> Pair.of(side, world.getTileEntity(pos.offset(side)))),
                sides.stream()
                        .flatMap(side ->
                                world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.offset(side))).stream()
                                        .map(entity -> Pair.of(side, entity))
                        )
        )
                .map(sideProvider -> ItemTransactorHelper.getTransactor(sideProvider.getRight(), sideProvider.getLeft().getOpposite()))
                .filter(Predicate.isEqual(NoSpaceTransactor.INSTANCE).negate())
                .forEach(transactor ->
                        transactor.insert(
                                inv.extract(
                                        stack -> {
                                            ItemStack leftOver = transactor.insert(stack.copy(), false, true);
                                            return leftOver.isEmpty() || leftOver.getCount() < stack.getCount();
                                        },
                                        1,
                                        1,
                                        false
                                ),
                                false,
                                false
                        )
                );
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        if (!(world.getBlockState(pos).getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(getWorld(), getPos());

        EnumFacing currentSide = world.getBlockState(pos).getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == EnumFacing.UP.getOpposite()) {
            progress += 1000; // can be free because of gravity
        }
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            pickupItems(currentSide);
        }

        putInNearInventories(currentSide);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("progress");
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", progress);
        nbt.setTag("mj_battery", battery.serializeNBT());
        return nbt;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("progress = " + progress);
    }
}

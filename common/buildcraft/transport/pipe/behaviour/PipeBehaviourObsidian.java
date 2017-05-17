package buildcraft.transport.pipe.behaviour;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    private static final int FREE_RADIUS = 2;
    private static final int MAX_RADIUS = 5;
    private static final long TARGET = MjAPI.MJ;
    private static final double INSERT_SPEED = 0.04;

    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);
    private final Map<UUID, Long> entitiesProgress = new HashMap<>();

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        NBTUtilBC.readCompoundList(nbt.getTagList("entitiesProgress", Constants.NBT.TAG_COMPOUND))
            .forEach(entryTag ->
                entitiesProgress.put(
                    entryTag.getUniqueId("key"),
                    entryTag.getLong("value")
                )
            );
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("battery", battery.serializeNBT());
        nbt.setTag(
            "entitiesProgress",
            NBTUtilBC.writeCompoundList(
                entitiesProgress.entrySet().stream()
                .map(entry -> {
                    NBTTagCompound entryTag = new NBTTagCompound();
                    entryTag.setUniqueId("key", entry.getKey());
                    entryTag.setLong("value", entry.getValue());
                    return entryTag;
                })
            )
        );
        return nbt;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        List<EnumFacing> connected = Arrays.stream(EnumFacing.VALUES)
            .filter(pipe::isConnected)
            .collect(Collectors.toList());
        EnumFacing openFace = connected.size() == 1 ? connected.get(0).getOpposite() : null;
        if (openFace != null) {
            List<Entity> entities = new ArrayList<>(pipe.getHolder().getPipeWorld().getEntitiesWithinAABB(
                Entity.class,
                new AxisAlignedBB(pipe.getHolder().getPipePos().offset(openFace, MAX_RADIUS)).expandXyz(MAX_RADIUS)
            ));
            entities.removeIf(entity -> {
                IItemTransactor transactor = ItemTransactorHelper.getTransactorForEntity(entity, openFace.getOpposite());
                return transactor == NoSpaceTransactor.INSTANCE ||
                    transactor.extract(StackFilter.ALL, 1, 1, true).isEmpty();
            });
            Map<UUID, Entity> entitiesMap = entities.stream()
                .collect(Collectors.toMap(Entity::getUniqueID, Function.identity()));
            for (Entity entity : entities) {
                entitiesProgress.putIfAbsent(entity.getUniqueID(), 0L);
            }
            entitiesProgress.keySet().removeIf(id -> !entitiesMap.keySet().contains(id));
            for (Iterator<UUID> iterator = entitiesProgress.keySet().iterator(); iterator.hasNext(); ) {
                UUID id = iterator.next();
                Entity entity = entitiesMap.get(id);
                if (entity.getPositionVector().distanceTo(VecUtil.convertCenter(pipe.getHolder().getPipePos())) < FREE_RADIUS) {
                    entitiesProgress.put(
                        id,
                        entitiesProgress.get(id) + Math.min(MjAPI.MJ / 10, TARGET - entitiesProgress.get(id))
                    );
                }
                entitiesProgress.put(
                    id,
                    entitiesProgress.get(id) + battery.extractPower(
                        0,
                        Math.min(
                            TARGET - entitiesProgress.get(id),
                            battery.getCapacity() / entitiesProgress.size()
                        )
                    )
                );
                if (entitiesProgress.get(id) >= TARGET) {
                    IItemTransactor transactor = ItemTransactorHelper.getTransactorForEntity(
                        entity,
                        openFace.getOpposite()
                    );

                    if (pipe.getFlow() instanceof IFlowItems) {
                        ((IFlowItems) pipe.getFlow()).insertItemsForce(
                            transactor.extract(StackFilter.ALL, 1, 1, true),
                            openFace,
                            null,
                            INSERT_SPEED
                        );
                        transactor.extract(StackFilter.ALL, 1, 1, false);
                        iterator.remove();
                    }
                    // TODO: Fluid extraction
                    // if (pipe.getFlow() instanceof IFlowFluid) {
                    // }
                }
            }
        }
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return entitiesProgress.isEmpty() ? 0 : (battery.getCapacity() - battery.getStored());
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return battery.addPowerChecking(microJoules, simulate);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) return (T) this;
        else if (capability == MjAPI.CAP_RECEIVER) return (T) this;
        else if (capability == MjAPI.CAP_CONNECTOR) return (T) this;
        return null;
    }
}

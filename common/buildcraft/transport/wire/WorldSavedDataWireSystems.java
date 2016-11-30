package buildcraft.transport.wire;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCMessageHandler;
import com.google.common.base.Predicates;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldSavedDataWireSystems extends WorldSavedData {
    public static final String DATA_NAME = "BC_WireSystems";
    public World world;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
    public boolean structureChanged = true;
    public final List<WireSystem> changedSystems = new ArrayList<>();
    public final List<EntityPlayerMP> changedPlayers = new ArrayList<>();

    public WorldSavedDataWireSystems() {
        super(DATA_NAME);
    }

    public WorldSavedDataWireSystems(String name) {
        super(name);
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.Element element) {
        return wireSystems.keySet().stream().filter(wireSystem -> wireSystem.hasElement(element)).collect(Collectors.toList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
        structureChanged = true;
    }

    public void buildAndAddWireSystem(WireSystem.Element element) {
        WireSystem wireSystem = new WireSystem().build(this, element);
        if(!wireSystem.isEmpty()) {
            wireSystems.put(wireSystem, false);
            wireSystems.put(wireSystem, wireSystem.update(this));
        }
        structureChanged = true;
    }

    public void tick() {
        wireSystems.keySet().stream()
                .filter(wireSystem -> {
                    boolean newPowered = wireSystem.update(this);
                    return wireSystems.put(wireSystem, newPowered) != newPowered;
                })
                .forEach(changedSystems::add);
        // noinspection Guava
        world.getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue()).forEach((player) -> {
            Map<WireSystem.Element, Boolean> elementsPowered = wireSystems.entrySet().stream()
                    .filter(systemPower ->
                            systemPower.getKey().isPlayerWatching(player) &&
                                    (structureChanged || changedSystems.contains(systemPower.getKey()) || changedPlayers.contains(player))
                    )
                    .flatMap(systemPower -> systemPower.getKey().elements.stream()
                            .filter(element -> element.type == WireSystem.Element.Type.WIRE_PART)
                            .map(element -> Pair.of(element, systemPower.getValue())))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            if(!elementsPowered.isEmpty()) {
                BCMessageHandler.netWrapper.sendTo(new MessageElementsPowered(elementsPowered), player);
            }
        });
        structureChanged = false;
        changedSystems.clear();
        changedPlayers.clear();
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList entriesList = new NBTTagList();
        wireSystems.forEach((wireSystem, powered) -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setTag("wireSystem", wireSystem.writeToNBT());
            entry.setBoolean("powered", powered);
            entriesList.appendTag(entry);
        });
        nbt.setTag("entries", entriesList);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        wireSystems.clear();
        NBTTagList entriesList = nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < entriesList.tagCount(); i++) {
            NBTTagCompound entry = entriesList.getCompoundTagAt(i);
            wireSystems.put(new WireSystem().readFromNBT(entry.getCompoundTag("wireSystem")), entry.getBoolean("powered"));
        }
    }

    public static WorldSavedDataWireSystems get(World world) {
        if(world.isRemote) {
            BCLog.logger.warn("Creating WireSystems on client, this is a bug");
        }
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataWireSystems instance = (WorldSavedDataWireSystems) storage.getOrLoadData(WorldSavedDataWireSystems.class, DATA_NAME);
        if(instance == null) {
            instance = new WorldSavedDataWireSystems();
            storage.setData(DATA_NAME, instance);
        }
        instance.world = world;
        return instance;
    }
}

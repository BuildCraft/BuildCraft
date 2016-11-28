package buildcraft.transport.wire;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldSavedDataWireSystems extends WorldSavedData {
    public static final String DATA_NAME = "BC_WireSystems";
    public final World world;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();

    public WorldSavedDataWireSystems(World world) {
        super(DATA_NAME);
        this.world = world;
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.Element element) {
        return wireSystems.keySet().stream().filter(wireSystem -> wireSystem.hasElement(element)).collect(Collectors.toList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
    }

    public void buildAndAddWireSystem(WireSystem.Element element) {
        WireSystem wireSystem = new WireSystem().build(this, element);
        if(!wireSystem.isEmpty()) {
            wireSystems.put(wireSystem, false);
            updateWireSystem(wireSystem);
        }
    }

    public void updateWireSystem(WireSystem wireSystem) {
        wireSystems.put(wireSystem, false);
    }

    public void updateAllWireSystems() {
        wireSystems.keySet().forEach(this::updateWireSystem);
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
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataWireSystems instance = (WorldSavedDataWireSystems) storage.getOrLoadData(WorldSavedDataWireSystems.class, DATA_NAME);
        if(instance == null) {
            instance = new WorldSavedDataWireSystems(world);
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
}

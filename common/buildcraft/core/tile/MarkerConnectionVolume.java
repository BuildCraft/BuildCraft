package buildcraft.core.tile;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.Box;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.tile.MarkerCache;
import buildcraft.lib.tile.MarkerConnection;

@Deprecated
public class MarkerConnectionVolume extends MarkerConnection<TileMarkerVolume, MarkerConnectionVolume> {
    public static final MarkerConnectionVolume LOADER = new MarkerConnectionVolume(null);

    public final Set<BlockPos> positions = new HashSet<>();
    public final Box box = new Box();

    public MarkerConnectionVolume(MarkerCache<TileMarkerVolume, MarkerConnectionVolume>.PerWorld worldCache) {
        super(worldCache);
    }

    @Override
    public Collection<BlockPos> getAllMarkers() {
        return positions;
    }

    @Override
    public MarkerConnectionVolume loadFromNBT(MarkerCache<TileMarkerVolume, MarkerConnectionVolume>.PerWorld cache, NBTTagCompound nbt) {
        MarkerConnectionVolume target = new MarkerConnectionVolume(cache);
        NBTTagList tagList = nbt.getTagList("positions", Constants.NBT.TAG_INT_ARRAY);
        for (int i = 0; i < tagList.tagCount(); i++) {
            target.positions.add(NBTUtils.readBlockPos(tagList.get(i)));
        }
        box.initialize(nbt.getCompoundTag("box"));
        return target;
    }

    @Override
    public NBTTagCompound saveToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        for (BlockPos pos : positions) {
            tagList.appendTag(NBTUtils.writeBlockPos(pos));
        }
        nbt.setTag("box", box.writeToNBT());
        return nbt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderConnections() {
        // TODO
    }
}

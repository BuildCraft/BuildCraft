package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.IWireManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

public class WireManager implements IWireManager {
    private final IPipeHolder holder;
    public final Map<EnumWirePart, EnumDyeColor> parts = new EnumMap<>(EnumWirePart.class);
    public final Set<EnumWirePart> poweredClient = EnumSet.noneOf(EnumWirePart.class);
    public final Map<EnumWireBetween, EnumDyeColor> betweens = new EnumMap<>(EnumWireBetween.class);
    public boolean inited = false;
    // TODO: Wire connections to adjacent blocks

    public WireManager(IPipeHolder holder) {
        this.holder = holder;
    }

    public WorldSavedDataWireSystems getWireSystems() {
        return WorldSavedDataWireSystems.get(holder.getPipeWorld());
    }

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public boolean addPart(EnumWirePart part, EnumDyeColor colour) {
        if(getColorOfPart(part) == null) {
            parts.put(part, colour);
            if(!holder.getPipeWorld().isRemote) {
                getWireSystems().buildAndAddWireSystem(new WireSystem.Element(holder.getPipePos(), part));
            }
            updateBetweens(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public EnumDyeColor removePart(EnumWirePart part) {
        EnumDyeColor color = getColorOfPart(part);
        if(color == null) {
            return null;
        } else {
            parts.remove(part);
            if(!holder.getPipeWorld().isRemote) {
                WireSystem.Element element = new WireSystem.Element(holder.getPipePos(), part);
                WireSystem.getConnectedElementsOfElement(holder, element).forEach(getWireSystems()::buildAndAddWireSystem);
                getWireSystems().getWireSystemsWithElement(element).forEach(getWireSystems()::removeWireSystem);
            }
            updateBetweens(false);
            return color;
        }
    }

    @Override
    public void updateBetweens(boolean recursive) {
        betweens.clear();
        parts.forEach((part, color) -> {
            for(EnumWireBetween between : EnumWireBetween.VALUES) {
                EnumWirePart[] betweenParts = between.parts;
                if(between.to == null) {
                    if((betweenParts[0] == part && getColorOfPart(betweenParts[1]) == color) ||
                            (betweenParts[1] == part && getColorOfPart(betweenParts[0]) == color)) {
                        betweens.put(between, color);
                    }
                } else if(holder.getPipe().isConnected(between.to)) {
                    IPipe pipe = holder.getNeighbouringPipe(between.to);
                    if(pipe != null) {
                        IPipeHolder holder = pipe.getHolder();
                        IWireManager wireManager = holder.getWireManager();
                        if(betweenParts[0] == part && wireManager.getColorOfPart(betweenParts[1]) == color) {
                            betweens.put(between, color);
                        }
                    }
                }
            }
        });

        if(!recursive) {
            for(EnumFacing side : EnumFacing.values()) {
                TileEntity tile = holder.getPipeWorld().getTileEntity(holder.getPipePos().offset(side));
                if(tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    holder.getWireManager().updateBetweens(true);
                }
            }
        }
    }

    @Override
    public EnumDyeColor getColorOfPart(EnumWirePart part) {
        return parts.get(part);
    }

    @Override
    public boolean hasPartOfColor(EnumDyeColor color) {
        return parts.values().contains(color);
    }

    @Override
    public boolean isPowered(EnumWirePart part) {
        if(holder.getPipeWorld().isRemote) {
            return poweredClient.contains(part);
        } else {
            return getWireSystems().getWireSystemsWithElement(new WireSystem.Element(holder.getPipePos(), part))
                    .stream()
                    .map(wireSystem -> getWireSystems().wireSystems.get(wireSystem)).reduce(Boolean::logicalOr).orElse(false);
        }
    }

    @Override
    public boolean isAnyPowered(EnumDyeColor color) {
        return parts.entrySet().stream().filter(partColor -> partColor.getValue() == color).anyMatch(partColor -> isPowered(partColor.getKey()));
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        int[] wiresArray = new int[parts.size() * 2];
        int[] i = {0};
        parts.forEach((part, color) -> {
            wiresArray[i[0]] = part.ordinal();
            wiresArray[i[0] + 1] = color.getMetadata();
            i[0] += 2;
        });
        nbt.setIntArray("parts", wiresArray);
        return nbt;
    }

    public void readFromNbt(NBTTagCompound nbt) {
        parts.clear();
        int[] wiresArray = nbt.getIntArray("parts");
        for(int i = 0; i < wiresArray.length; i += 2) {
            parts.put(EnumWirePart.VALUES[wiresArray[i]], EnumDyeColor.byMetadata(wiresArray[i + 1]));
        }
    }

    public void writePayload(PacketBuffer buffer, Side side) {
        if (side == Side.SERVER) {
            buffer.writeInt(parts.size());
            parts.forEach((part, color) -> {
                buffer.writeInt(part.ordinal());
                buffer.writeInt(color.getMetadata());
            });
        }
    }

    @SideOnly(Side.CLIENT)
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            parts.clear();
            int count = buffer.readInt();
            for(int i = 0; i < count; i++) {
                parts.put(EnumWirePart.VALUES[buffer.readInt()], EnumDyeColor.byMetadata(buffer.readInt()));
            }
            updateBetweens(false);
        }
    }
}

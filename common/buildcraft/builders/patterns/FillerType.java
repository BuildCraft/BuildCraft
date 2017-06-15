package buildcraft.builders.patterns;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;

import buildcraft.core.BCCoreStatements;

public class FillerType extends StatementType<IFillerPattern> {
    public static final FillerType INSTANCE = new FillerType();

    private FillerType() {
        super(IFillerPattern.class, BCCoreStatements.PATTERN_NONE);
    }

    @Override
    public IFillerPattern[] getPossible(IFillerPattern slot, GuiJson<?> gui) {
        return slot.getPossible();
    }

    @Override
    public IFillerPattern readFromNbt(NBTTagCompound nbt) {
        String kind = nbt.getString("kind");
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            return defaultStatement;
        }
        return pattern;
    }

    @Override
    public NBTTagCompound writeToNbt(IFillerPattern slot) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("kind", slot.getUniqueTag());
        return nbt;
    }

    @Override
    public IFillerPattern readFromBuffer(PacketBufferBC buffer) {
        String kind = buffer.readString();
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            return defaultStatement;
        }
        return pattern;
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, IFillerPattern slot) {
        buffer.writeString(slot.getUniqueTag());
    }
}

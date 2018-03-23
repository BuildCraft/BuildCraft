package buildcraft.builders.filler;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;

import buildcraft.builders.BCBuildersStatements;

public class FillerType extends StatementType<IFillerPattern> {
    public static final FillerType INSTANCE = new FillerType();

    private FillerType() {
        super(IFillerPattern.class, BCBuildersStatements.PATTERN_NONE);
    }

    @Override
    public IFillerPattern convertToType(Object value) {
        return value instanceof IFillerPattern ? (IFillerPattern) value : null;
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

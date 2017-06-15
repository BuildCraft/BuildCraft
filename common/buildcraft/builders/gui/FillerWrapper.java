package buildcraft.builders.gui;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;
import buildcraft.lib.statement.StatementWrapper;

import buildcraft.core.BCCoreStatements;

@Deprecated
public class FillerWrapper extends StatementWrapper implements IFillerPattern {
    public static final StatementType<FillerWrapper> TYPE;

    static {
        IFillerPattern def = BCCoreStatements.PATTERN_NONE;
        TYPE = new StatementType<>(FillerWrapper.class, new FillerWrapper(def), FillerWrapper::readFromNbt,
            FillerWrapper::readFromBuf);
    }

    public FillerWrapper(IFillerPattern delegate) {
        super(delegate, EnumPipePart.CENTER);
    }

    public static FillerWrapper readFromNbt(NBTTagCompound nbt) {
        String kind = nbt.getString("kind");
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            pattern = BCCoreStatements.PATTERN_NONE;
        }
        return new FillerWrapper(pattern);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("kind", getDelegate().getUniqueTag());
        return nbt;
    }

    public static FillerWrapper readFromBuf(PacketBufferBC buffer) {
        String kind = buffer.readString();
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            pattern = BCCoreStatements.PATTERN_NONE;
        }
        return new FillerWrapper(pattern);
    }

    @Override
    public void writeToBuf(PacketBufferBC buf) {
        buf.writeString(getUniqueTag());
    }

    public IFillerPattern getDelegate() {
        return (IFillerPattern) delegate;
    }

    @Override
    public FillerWrapper[] getPossible() {
        IFillerPattern[] possible = getDelegate().getPossible();
        FillerWrapper[] real = new FillerWrapper[possible.length];
        for (int i = 0; i < possible.length; i++) {
            real[i] = new FillerWrapper(possible[i]);
        }
        return real;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        return getDelegate().createTemplate(filler, params);
    }
}

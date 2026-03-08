package buildcraft.lib.statement;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementManager.IParamReaderBuf;
import buildcraft.api.statements.StatementManager.IParameterReader;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;

public class StatementTypeParam extends StatementType<IStatementParameter> {
    public static final StatementTypeParam INSTANCE = new StatementTypeParam();

    public StatementTypeParam() {
        super(IStatementParameter.class, null);
    }

    @Override
    public IStatementParameter convertToType(Object value) {
        return value instanceof IStatementParameter ? (IStatementParameter) value : null;
    }

    @Override
    public IStatementParameter readFromNbt(CompoundTag nbt) {
        String kind = nbt.getString("kind");
        IParameterReader reader = StatementManager.getParameterReader(kind);
        if (reader == null) {
            return null;
        } else {
            return reader.readFromNbt(nbt);
        }
    }

    @Override
    public CompoundTag writeToNbt(IStatementParameter slot) {
        CompoundTag nbt = new CompoundTag();
        if (slot != null) {
            slot.writeToNbt(nbt);
            nbt.putString("kind", slot.getUniqueTag());
        }
        return nbt;
    }

    @Override
    public IStatementParameter readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String tag = buffer.readString();
            IParamReaderBuf reader = StatementManager.paramsBuf.get(tag);
            if (reader == null) {
                throw new InvalidInputDataException("Unknown paramater type " + tag);
            }
            return reader.readFromBuf(buffer);
        } else {
            return null;
        }
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, IStatementParameter slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            slot.writeToBuf(buffer);
        }
    }
}

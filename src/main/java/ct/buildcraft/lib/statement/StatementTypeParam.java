package ct.buildcraft.lib.statement;

import java.io.IOException;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementManager;
import ct.buildcraft.api.statements.StatementManager.IParamReaderBuf;
import ct.buildcraft.api.statements.StatementManager.IParameterReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    public IStatementParameter readFromBuffer(FriendlyByteBuf buffer) throws IOException {
        if (buffer.readBoolean()) {
            String tag = buffer.readUtf();
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
    public void writeToBuffer(FriendlyByteBuf buffer, IStatementParameter slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            slot.writeToBuf(buffer);
        }
    }
}

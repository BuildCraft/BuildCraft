package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;
import buildcraft.lib.statement.TriggerWrapper;
import buildcraft.lib.statement.TriggerWrapper.TriggerWrapperInternal;
import net.minecraft.nbt.CompoundNBT;

import java.io.IOException;

public class TriggerType extends StatementType<TriggerWrapper> {
    public static final TriggerType INSTANCE = new TriggerType();

    private TriggerType() {
        super(TriggerWrapper.class, null);
    }

    @Override
    public TriggerWrapper convertToType(Object value) {
        if (value instanceof ITriggerInternal) {
            return new TriggerWrapperInternal((ITriggerInternal) value);
        }
        // We cannot convert sided actions (as they require a side)
        return null;
    }

    @Override
    public TriggerWrapper readFromNbt(CompoundNBT nbt) {
        if (nbt == null) {
            return null;
        }
        String kind = nbt.getString("kind");
        if (kind == null || kind.isEmpty()) {
            return null;
        }
        EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side"));
        IStatement statement = StatementManager.statements.get(kind);
        if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, side.face);
        }
        BCLog.logger.warn("[gate.trigger] Couldn't find a trigger called '{}'! (found {})", kind, statement);
        return null;
    }

    @Override
    public CompoundNBT writeToNbt(TriggerWrapper slot) {
        CompoundNBT nbt = new CompoundNBT();
        if (slot == null) {
            return nbt;
        }
        nbt.putString("kind", slot.getUniqueTag());
        nbt.putByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public TriggerWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readString();
            EnumPipePart part = buffer.readEnum(EnumPipePart.class);
            IStatement statement = StatementManager.statements.get(name);
            if (statement instanceof ITrigger) {
                return TriggerWrapper.wrap(statement, part.face);
            } else {
                throw new InvalidInputDataException("Unknown trigger '" + name + "'");
            }
        } else {
            return null;
        }
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, TriggerWrapper slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            buffer.writeEnum(slot.sourcePart);
        }
    }
}

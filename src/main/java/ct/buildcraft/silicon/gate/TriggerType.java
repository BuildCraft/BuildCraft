package ct.buildcraft.silicon.gate;

import java.io.IOException;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.ITrigger;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.StatementManager;
import ct.buildcraft.lib.statement.StatementType;
import ct.buildcraft.lib.statement.TriggerWrapper;
import ct.buildcraft.lib.statement.TriggerWrapper.TriggerWrapperInternal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    public TriggerWrapper readFromNbt(CompoundTag nbt) {
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
    public CompoundTag writeToNbt(TriggerWrapper slot) {
        CompoundTag nbt = new CompoundTag();
        if (slot == null) {
            return nbt;
        }
        nbt.putString("kind", slot.getUniqueTag());
        nbt.putByte("side", (byte) slot.getSourcePart().getIndex());
        return nbt;
    }

    @Override
    public TriggerWrapper readFromBuffer(FriendlyByteBuf buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readUtf();
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
    public void writeToBuffer(FriendlyByteBuf buffer, TriggerWrapper slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            buffer.writeEnum(slot.getSourcePart());
        }
    }
}

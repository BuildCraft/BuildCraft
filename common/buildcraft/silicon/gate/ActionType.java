package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.ActionWrapper.ActionWrapperInternal;
import buildcraft.lib.statement.StatementType;
import net.minecraft.nbt.CompoundNBT;

import java.io.IOException;

public class ActionType extends StatementType<ActionWrapper> {
    public static final ActionType INSTANCE = new ActionType();

    private ActionType() {
        super(ActionWrapper.class, null);
    }

    @Override
    public ActionWrapper convertToType(Object value) {
        if (value instanceof IActionInternal) {
            return new ActionWrapperInternal((IActionInternal) value);
        }
        // We cannot convert sided actions (as they require a side)
        return null;
    }

    @Override
    public ActionWrapper readFromNbt(CompoundNBT nbt) {
        if (nbt == null) {
            return null;
        }
        String kind = nbt.getString("kind");
        if (kind == null || kind.isEmpty()) {
            return null;
        }
        EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side"));
        IStatement statement = StatementManager.statements.get(kind);
        if (statement instanceof IAction) {
            return ActionWrapper.wrap(statement, side.face);
        }
        BCLog.logger.warn("[gate.trigger] Couldn't find an action called '{}'! (found {})", kind, statement);
        return null;
    }

    @Override
    public CompoundNBT writeToNbt(ActionWrapper slot) {
        CompoundNBT nbt = new CompoundNBT();
        if (slot == null) {
            return nbt;
        }
        nbt.putString("kind", slot.getUniqueTag());
        nbt.putByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public ActionWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readString();
            EnumPipePart part = buffer.readEnum(EnumPipePart.class);
            IStatement statement = StatementManager.statements.get(name);
            if (statement instanceof IAction) {
                return ActionWrapper.wrap(statement, part.face);
            } else {
                throw new InvalidInputDataException("Unknown action '" + name + "'");
            }
        } else {
            return null;
        }
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, ActionWrapper slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            buffer.writeEnum(slot.sourcePart);
        }
    }
}

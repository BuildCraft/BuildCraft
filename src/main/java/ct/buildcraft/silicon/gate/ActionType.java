package ct.buildcraft.silicon.gate;

import java.io.IOException;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.statements.IAction;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.StatementManager;
import ct.buildcraft.lib.statement.ActionWrapper;
import ct.buildcraft.lib.statement.ActionWrapper.ActionWrapperInternal;
import ct.buildcraft.lib.statement.StatementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    public ActionWrapper readFromNbt(CompoundTag nbt) {
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
    public CompoundTag writeToNbt(ActionWrapper slot) {
        CompoundTag nbt = new CompoundTag();
        if (slot == null) {
            return nbt;
        }
        nbt.putString("kind", slot.getUniqueTag());
        nbt.putByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public ActionWrapper readFromBuffer(FriendlyByteBuf buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readUtf();
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
    public void writeToBuffer(FriendlyByteBuf buffer, ActionWrapper slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            buffer.writeEnum(slot.sourcePart);
        }
    }
}

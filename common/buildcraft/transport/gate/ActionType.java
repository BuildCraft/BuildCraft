package buildcraft.transport.gate;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;

public class ActionType extends StatementType<ActionWrapper> {
    public static final ActionType INSTANCE = new ActionType();

    private ActionType() {
        super(ActionWrapper.class, null);
    }

    @Override
    public ActionWrapper readFromNbt(NBTTagCompound nbt) {
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
    public NBTTagCompound writeToNbt(ActionWrapper slot) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (slot == null) {
            return nbt;
        }
        nbt.setString("kind", slot.getUniqueTag());
        nbt.setByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public ActionWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readString();
            EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
            IStatement stmnt = StatementManager.statements.get(name);
            if (stmnt instanceof IAction) {
                return ActionWrapper.wrap(stmnt, part.face);
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
            buffer.writeString(slot.getUniqueTag());
            buffer.writeEnumValue(slot.sourcePart);
        }
    }
}

package buildcraft.transport.gate;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;

public class TriggerType extends StatementType<TriggerWrapper> {
    public static final TriggerType INSTANCE = new TriggerType();

    private TriggerType() {
        super(TriggerWrapper.class, null);
    }

    @Override
    public TriggerWrapper readFromNbt(NBTTagCompound nbt) {
        if (nbt == null) {
            return null;
        }
        String kind = nbt.getString("kind");
        EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side"));
        IStatement statement = StatementManager.statements.get(kind);
        if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, side.face);
        }
        BCLog.logger.warn("[gate.trigger] Couldn't find a trigger called '%s'! (found %s)", kind, statement);
        return null;
    }

    @Override
    public NBTTagCompound writeToNbt(TriggerWrapper slot) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (slot == null) {
            return nbt;
        }
        nbt.setString("kind", slot.getUniqueTag());
        nbt.setByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public TriggerWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readString();
            EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
            IStatement stmnt = StatementManager.statements.get(name);
            if (stmnt instanceof ITrigger) {
                return TriggerWrapper.wrap(stmnt, part.face);
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
            buffer.writeString(slot.getUniqueTag());
            buffer.writeEnumValue(slot.sourcePart);
        }
    }
}

package buildcraft.lib.statement;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementManager.IParamReaderBuf;
import buildcraft.api.statements.StatementManager.IParameterReader;

import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;

/** Util class for holding, saving, loading and networking {@link StatementWrapper} and its
 * {@link IStatementParameter}'s. */
public class FullStatement<S extends IStatement> implements IReference<S> {
    public final StatementType<S> type;
    public final int maxParams;
    private final ParamSlot[] params;
    private S statement;

    public FullStatement(StatementType<S> type, int maxParams) {
        this.type = type;
        this.statement = type.defaultStatement;
        this.maxParams = maxParams;
        this.params = new ParamSlot[maxParams];
        for (int i = 0; i < maxParams; i++) {
            params[i] = new ParamSlot();
        }
    }

    // NBT

    public void readFromNbt(NBTTagCompound nbt) {
        statement = type.readFromNbt(nbt.getCompoundTag("s"));
        if (statement == null) {
            for (ParamSlot p : params) {
                p.setWithoutUpdating(null);
            }
        } else {
            for (int p = 0; p < params.length; p++) {
                ParamSlot slot = params[p];
                NBTTagCompound pNbt = nbt.getCompoundTag(Integer.toString(p));
                String kind = pNbt.getString("kind");
                IParameterReader reader = StatementManager.getParameterReader(kind);
                if (reader == null) {
                    slot.setWithoutUpdating(null);
                } else {
                    slot.setWithoutUpdating(reader.readFromNbt(pNbt));
                }
            }
        }
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (statement != null) {
            nbt.setTag("s", type.writeToNbt(statement));
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p].get();
                if (param != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    param.writeToNbt(tag);
                    tag.setString("kind", param.getUniqueTag());
                    nbt.setTag(Integer.toString(p), tag);
                }
            }
        }
        return nbt;
    }

    // Networking

    public void readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            statement = type.readFromBuffer(buffer);
            for (int p = 0; p < params.length; p++) {
                if (buffer.readBoolean()) {
                    String tag = buffer.readString();
                    IParamReaderBuf reader = StatementManager.paramsBuf.get(tag);
                    if (reader == null) {
                        throw new InvalidInputDataException("Unknown paramater type " + tag);
                    }
                    params[p].setWithoutUpdating(reader.readFromBuf(buffer));
                } else {
                    params[p].setWithoutUpdating(null);
                }
            }
        } else {
            for (ParamSlot p : params) {
                p.setWithoutUpdating(null);
            }
        }
    }

    public void writeToBuffer(PacketBufferBC buffer) {
        if (statement == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            type.writeToBuffer(buffer, statement);
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p].get();
                if (param == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeString(param.getUniqueTag());
                    param.writeToBuf(buffer);
                }
            }
        }
    }

    // IReference

    @Override
    public S get() {
        return statement;
    }

    @Override
    public void set(S to) {
        statement = to;
        for (int i = 0; i < params.length; i++) {
            params[i].onSetMain(to, i);
        }
    }

    @Override
    public boolean canSet(Object value) {
        return type.clazz.isInstance(value);
    }

    // Params

    public IReference<IStatementParameter> getParamRef(int i) {
        return params[i];
    }

    public IStatementParameter get(int index) {
        return getParamRef(index).get();
    }

    public void set(int index, IStatementParameter param) {
        getParamRef(index).set(param);
    }
}

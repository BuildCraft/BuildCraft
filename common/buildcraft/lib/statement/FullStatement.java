package buildcraft.lib.statement;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;

/** Util class for holding, saving, loading and networking {@link StatementWrapper} and its
 * {@link IStatementParameter}'s. */
public class FullStatement<S extends IStatement> implements IReference<S> {
    public final StatementType<S> type;
    public final int maxParams;
    public boolean canInteract = true;
    private final IStatementChangeListener listener;
    private final IStatementParameter[] params;
    private final ParamRef[] paramRefs;
    private S statement;

    public FullStatement(StatementType<S> type, int maxParams, IStatementChangeListener listener) {
        this.type = type;
        this.statement = type.defaultStatement;
        this.listener = listener;
        this.maxParams = maxParams;
        this.params = new IStatementParameter[maxParams];
        this.paramRefs = new FullStatement.ParamRef[maxParams];
        for (int i = 0; i < maxParams; i++) {
            paramRefs[i] = new ParamRef(this, i);
        }
    }

    // NBT

    public void readFromNbt(NBTTagCompound nbt) {
        statement = type.readFromNbt(nbt.getCompoundTag("s"));
        if (statement == null) {
            Arrays.fill(params, null);
        } else {
            for (int p = 0; p < params.length; p++) {
                NBTTagCompound pNbt = nbt.getCompoundTag(Integer.toString(p));
                params[p] = StatementTypeParam.INSTANCE.readFromNbt(pNbt);
            }
        }
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (statement != null) {
            nbt.setTag("s", type.writeToNbt(statement));
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p];
                if (param != null) {
                    nbt.setTag(Integer.toString(p), StatementTypeParam.INSTANCE.writeToNbt(param));
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
                params[p] = StatementTypeParam.INSTANCE.readFromBuffer(buffer);
            }
        } else {
            statement = type.defaultStatement;
            Arrays.fill(params, null);
        }
    }

    public void writeToBuffer(PacketBufferBC buffer) {
        if (statement == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            type.writeToBuffer(buffer, statement);
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p];
                StatementTypeParam.INSTANCE.writeToBuffer(buffer, param);
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
        if (statement == null) {
            Arrays.fill(params, null);
            return;
        }
        for (int i = 0; i < params.length; i++) {
            if (i > statement.maxParameters()) {
                params[i] = null;
            } else {
                params[i] = statement.createParameter(params[i], i);
            }
        }
    }

    @Override
    public boolean canSet(Object value) {
        if (value == null) {
            return true;
        }
        if (!type.clazz.isInstance(value)) {
            return false;
        }
        return ((IStatement) value).minParameters() <= params.length;
    }

    static class ParamRef implements IReference<IStatementParameter> {
        public final IReference<? extends IStatement> statementRef;
        public final IStatementParameter[] array;
        public final int index;

        public ParamRef(FullStatement<?> full, int index) {
            statementRef = full;
            this.array = full.params;
            this.index = index;
        }

        @Override
        public IStatementParameter get() {
            return array[index];
        }

        @Override
        public void set(IStatementParameter to) {
            array[index] = to;
        }

        @Override
        public boolean canSet(Object value) {
            IStatement statement = statementRef.get();
            if (statement == null) {
                return false;
            }
            if (value == null || value instanceof IStatementParameter) {
                IStatementParameter param = (IStatementParameter) value;
                return statement.createParameter(param, index) == param;
            }
            return false;
        }
    }

    // Params

    public IReference<IStatementParameter> getParamRef(int i) {
        return paramRefs[i];
    }

    public IStatementParameter get(int index) {
        return getParamRef(index).get();
    }

    public void set(int index, IStatementParameter param) {
        getParamRef(index).set(param);
    }

    public boolean canSet(int index, Object param) {
        return getParamRef(index).canSet(param);
    }

    public int getParamCount() {
        return params.length;
    }

    public IStatementParameter[] getParameters() {
        return params;
    }

    // Gui change listeners

    /** Gui elements should call this after calling {@link #set(IStatement)} or {@link #set(int, IStatementParameter)},
     * with either -1 or the param index respectively. */
    public void postSetFromGui(int paramIndex) {
        if (listener != null) {
            listener.onChange(this, paramIndex);
        }
    }

    public interface IStatementChangeListener {
        /** @param stmnt The statement that changed
         * @param paramIndex The index of the parameter that changed, or -1 if the main {@link IStatement} changed. */
        void onChange(FullStatement<?> stmnt, int paramIndex);
    }
}

package buildcraft.lib.statement;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;

/** Util class for holding, saving, loading and networking {@link StatementWrapper} and its
 * {@link IStatementParameter}'s. */
public class FullStatement<S extends StatementWrapper> implements IReference<S> {
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

    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    // Networking

    public void readFromBuffer(PacketBufferBC buffer) {

    }

    public void writeToBuffer(PacketBufferBC buffer) {

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
        // TODO: Update property!
    }

    @Override
    public boolean canSet(Object value) {
        return type.clazz.isInstance(value);
    }
}

package buildcraft.lib.statement;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.net.PacketBufferBC;

public class StatementTypeParam extends StatementType<IStatementParameter> {
    public static final StatementTypeParam INSTANCE = new StatementTypeParam();

    public StatementTypeParam() {
        super(IStatementParameter.class, null);
    }

    @Override
    public IStatementParameter readFromNbt(NBTTagCompound nbt) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public NBTTagCompound writeToNbt(IStatementParameter slot) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public IStatementParameter readFromBuffer(PacketBufferBC buffer) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, IStatementParameter slot) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }
}

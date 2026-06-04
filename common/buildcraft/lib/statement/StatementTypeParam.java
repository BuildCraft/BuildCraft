/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.statement;

import java.io.IOException;

import net.minecraft.nbt.NbtCompound;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementManager.IParamReaderBuf;
import buildcraft.api.statements.StatementManager.IParameterReader;

import buildcraft.lib.net.PacketBufferBC;

public class StatementTypeParam extends StatementType<IStatementParameter> {
    public static final StatementTypeParam INSTANCE = new StatementTypeParam();

    public StatementTypeParam() {
        super(IStatementParameter.class, null);
    }

    @Override
    public IStatementParameter convertToType(Object value) {
        return value instanceof IStatementParameter ? (IStatementParameter) value : null;
    }

    @Override
    public IStatementParameter readFromNbt(NbtCompound nbt) {
        String kind = nbt.getString("kind");
        IParameterReader reader = StatementManager.getParameterReader(kind);
        if (reader == null) {
            return null;
        } else {
            return reader.readFromNbt(nbt);
        }
    }

    @Override
    public NbtCompound writeToNbt(IStatementParameter slot) {
        NbtCompound nbt = new NbtCompound();
        if (slot != null) {
            slot.writeToNbt(nbt);
            nbt.putString("kind", slot.getUniqueTag());
        }
        return nbt;
    }

    @Override
    public IStatementParameter readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String tag = buffer.readString();
            IParamReaderBuf reader = StatementManager.paramsBuf.get(tag);
            if (reader == null) {
                throw new InvalidInputDataException("Unknown paramater type " + tag);
            }
            return reader.readFromBuf(buffer);
        } else {
            return null;
        }
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, IStatementParameter slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeString(slot.getUniqueTag());
            slot.writeToBuf(buffer);
        }
    }
}

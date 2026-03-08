package buildcraft.lib.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class StatementType<S extends IGuiSlot> {

    public final Class<S> clazz;
    public final S defaultStatement;

    public StatementType(Class<S> clazz, S defaultStatement) {
        this.clazz = clazz;
        this.defaultStatement = defaultStatement;
    }

    /** Reads a {@link StatementWrapper} from the given {@link CompoundTag}. The tag compound will be equal to the
     * one returned by {@link #writeToNbt(IGuiSlot)} */
    public abstract S readFromNbt(CompoundTag nbt);

    public abstract CompoundTag writeToNbt(S slot);

    /** Reads a {@link StatementWrapper} from the given {@link PacketBufferBC}. The buffer will return the data written
     * to a different buffer by {@link #writeToBuffer(PacketBufferBC, IGuiSlot)}. */
    public abstract S readFromBuffer(PacketBufferBC buffer) throws IOException;

    public abstract void writeToBuffer(PacketBufferBC buffer, S slot);

    @Nullable
    public abstract S convertToType(Object value);
}

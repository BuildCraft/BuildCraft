package ct.buildcraft.lib.statement;

import java.io.IOException;

import javax.annotation.Nullable;

import ct.buildcraft.api.statements.IGuiSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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

    /** Reads a {@link StatementWrapper} from the given {@link FriendlyByteBuf}. The buffer will return the data written
     * to a different buffer by {@link #writeToBuffer(FriendlyByteBuf, IGuiSlot)}. */
    public abstract S readFromBuffer(FriendlyByteBuf buffer) throws IOException;

    public abstract void writeToBuffer(FriendlyByteBuf buffer, S slot);

    @Nullable
    public abstract S convertToType(Object value);
}

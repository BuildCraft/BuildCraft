package buildcraft.lib.statement;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.lib.net.PacketBufferBC;

public class StatementType<S extends StatementWrapper> {

    public final Class<S> clazz;
    public final S defaultStatement;
    public final NbtReader<S> nbtRreader;
    public final BufReader<S> bufReader;

    public StatementType(Class<S> clazz, S defaultStatement, NbtReader<S> nbtRreader, BufReader<S> bufReader) {
        this.clazz = clazz;
        this.defaultStatement = defaultStatement;
        this.nbtRreader = nbtRreader;
        this.bufReader = bufReader;
    }

    @FunctionalInterface
    public interface NbtReader<S extends StatementWrapper> {
        S readFromNbt(NBTTagCompound nbt);
    }

    @FunctionalInterface
    public interface BufReader<S extends StatementWrapper> {
        S readFromBuffer(PacketBufferBC buffer);
    }
}

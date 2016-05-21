package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;

public interface IEmeraldPipe extends IFilteredPipe {

    enum FilterMode {
        WHITE_LIST,
        BLACK_LIST,
        ROUND_ROBIN
    }

    class EmeraldPipeSettings {
        private FilterMode filterMode;

        public EmeraldPipeSettings(FilterMode defaultMode) {
            filterMode = defaultMode;
        }

        public FilterMode getFilterMode() {
            return filterMode;
        }

        public void setFilterMode(FilterMode mode) {
            filterMode = mode;
        }

        public void readFromNBT(NBTTagCompound nbt) {
            filterMode = FilterMode.values()[nbt.getByte("filterMode")];
        }

        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setByte("filterMode", (byte) filterMode.ordinal());
        }
    }

    EmeraldPipeSettings getSettings();

    boolean isValidFilterMode(FilterMode mode);
}

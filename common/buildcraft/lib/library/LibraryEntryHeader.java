package buildcraft.lib.library;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.permission.PlayerOwner;

public final class LibraryEntryHeader {
    public final String name, kind;
    public final LocalDateTime creation;
    public final PlayerOwner author;
    private final int hash;

    public LibraryEntryHeader(String name, String kind, LocalDateTime creation, PlayerOwner author) {
        this.name = name;
        this.kind = kind;
        this.creation = creation;
        this.author = author;
        this.hash = computeHash();
    }

    public LibraryEntryHeader(PacketBuffer buffer) {
        this.name = buffer.readStringFromBuffer(256);
        this.kind = buffer.readStringFromBuffer(30);
        int year = buffer.readInt();
        int month = buffer.readByte();
        int dayOfMonth = buffer.readByte();
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        int hour = buffer.readByte();
        int minute = buffer.readByte();
        int second = buffer.readByte();
        LocalTime time = LocalTime.of(hour, minute, second);
        this.creation = LocalDateTime.of(date, time);
        this.author = PlayerOwner.read(buffer);
        this.hash = computeHash();
    }

    public void writeToByteBuf(PacketBuffer buffer) {
        buffer.writeString(name);
        buffer.writeInt(creation.getYear());
        buffer.writeByte(creation.getMonthValue());
        buffer.writeByte(creation.getDayOfMonth());
        buffer.writeByte(creation.getHour());
        buffer.writeByte(creation.getMinute());
        buffer.writeByte(creation.getSecond());
        author.writeToByteBuf(buffer);
    }

    public LibraryEntryHeader(NBTTagCompound nbt, String kind) {
        this.name = nbt.getString("name");
        this.kind = kind;
        this.creation = NBTUtils.readLocalDateTime(nbt.getCompoundTag("creation"));
        this.author = PlayerOwner.read(nbt.getCompoundTag("author"));
        this.hash = computeHash();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", name);
        nbt.setTag("creation", NBTUtils.writeLocalDateTime(creation));
        nbt.setTag("author", author.writeToNBT());
        return nbt;
    }

    private int computeHash() {
        return Objects.hash(name, kind, creation, author.getOwner().getId());
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        LibraryEntryHeader other = (LibraryEntryHeader) obj;
        // PlayerOwner's are properly internalised so checking for equality is fine.
        return author == other.author//
            && kind.equals(other.kind)//
            && name.equals(other.name)//
            && creation.equals(other.creation);
    }
}

package buildcraft.lib.permission;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.permission.EnumProtectionStatus;

class OwnerSettings {
    @Nonnull
    EnumProtectionStatus defaultStatus = EnumProtectionStatus.ANYONE;

    final Set<UUID> friends = new HashSet<>();

    public OwnerSettings() {}

    public OwnerSettings(NBTTagCompound nbt) {
        byte def = nbt.getByte("defaultStatus");
        defaultStatus = EnumProtectionStatus.get(def);

        NBTTagList list = nbt.getTagList("friends", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound comp = list.getCompoundTagAt(i);
            if (comp.hasKey("M") && comp.hasKey("L")) {
                addFriend(NBTUtil.getUUIDFromTag(comp));
            }
        }
    }

    NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("defaultStatus", defaultStatus.permissionLevel);

        NBTTagList list = new NBTTagList();
        for (UUID uuid : friends) {
            list.appendTag(NBTUtil.createUUIDTag(uuid));
        }
        nbt.setTag("friends", list);

        return nbt;
    }

    boolean isFriend(UUID toTest) {
        return friends.contains(toTest);
    }

    void addFriend(UUID newFriend) {
        // Yay new friend :)
        friends.add(newFriend);
    }

    void removeFriend(UUID oldFriend) {
        // goodbye for now?
        friends.remove(oldFriend);
    }

    void clearFriends() {
        // IS SAD DAY
        friends.clear();
    }
}

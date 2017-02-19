package buildcraft.builders.schematic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Snapshot implements INBTSerializable<NBTTagCompound> {
    public UUID id;
    public UUID owner;
    public Date created;
    public String name;

    public Snapshot(UUID id, UUID owner, Date created, String name) {
        this.id = id;
        this.owner = owner;
        this.created = created;
        this.name = name;
    }

    public Snapshot() {
    }

    public String getFileName() {
        return Stream.of(
                id,
                owner,
                created,
                name
        )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(";"));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("id", id);
        nbt.setUniqueId("owner", owner);
        nbt.setLong("created", created.getTime());
        nbt.setString("name", name);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        id = nbt.getUniqueId("id");
        owner = nbt.getUniqueId("owner");
        created = new Date(nbt.getLong("created"));
        name = nbt.getString("name");
    }

    public EntityPlayer getOwnerPlayer(World world) {
        return world.getPlayerEntityByUUID(owner);
    }

    abstract public EnumSnapshotType getType();

    public enum EnumSnapshotType {
        TEMPLATE(Template::new, 3),
        BLUEPRINT(Blueprint::new, 9);

        public final Supplier<Snapshot> create;
        public final int maxPerTick;

        EnumSnapshotType(Supplier<Snapshot> create, int maxPerTick) {
            this.create = create;
            this.maxPerTick = maxPerTick;
        }
    }
}

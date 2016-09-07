package buildcraft.lib.bpt.builder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.command.IPayloadWriter;

public class BuilderAnimationManager implements INBTSerializable<NBTTagCompound> {
    public enum EnumBuilderAnimMessage {
        STATE,
        ITEM,
        BLOCK,
        FLUID,
        POWER
    }

    public interface IAnimationMessageSender {
        /** @param type The type of message. Will never be {@link EnumBuilderAnimMessage#STATE}. */
        void sendAnimationMessage(EnumBuilderAnimMessage type, IPayloadWriter writer);
    }

    private final IAnimationMessageSender sender;
    private final List<AbstractAnimatedElement> elements = new ArrayList<>();
    private long now = 0;

    public BuilderAnimationManager(IAnimationMessageSender sender) {
        this.sender = sender;
    }

    public void writeStatePayload(PacketBuffer buffer) {

    }

    @SideOnly(Side.CLIENT)
    public void receiveMessage(EnumBuilderAnimMessage type, PacketBuffer buffer) {

    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("now", now);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        now = nbt.getLong("now");
    }

    public void reset() {
        now = 0;
        elements.clear();
    }

    public void tick() {

        now++;
    }

    @SideOnly(Side.CLIENT)
    public void render(VertexBuffer vb, float partialTicks) {

    }
}

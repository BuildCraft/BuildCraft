package buildcraft.core.tablet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tablet.TabletBitmap;
import buildcraft.core.BuildCraftCore;

public class TabletClient extends TabletBase {
    protected final TabletRenderer renderer;

    public TabletClient() {
        super();
        this.renderer = new TabletRenderer(new TabletBitmap(this.getScreenWidth(), this.getScreenHeight()));
    }

    @Override
    public void tick(float time) {
        super.tick(time);
    }

    public void updateGui(float time, GuiTablet gui, boolean force) {
        renderer.tick(time);

        if (renderer.shouldChange() || force) {
            gui.copyDisplay(renderer.get());
        }
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void refreshScreen(TabletBitmap newDisplay) {
        renderer.update(newDisplay);
    }

    @Override
    public void receiveMessage(NBTTagCompound compound) {
        if (!receiveMessageInternal(compound)) {

        }
    }

    @Override
    public void launchProgram(String name) {
        // noop
    }

    @Override
    public void sendMessage(NBTTagCompound compound) {
        compound.setBoolean("__program", true);
        BuildCraftCore.instance.sendToServer(new PacketTabletMessage(compound));
    }
}

package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.block.BlockBuildCraftBase_BC8;
import buildcraft.lib.item.ItemBuildCraft_BC8;

public abstract class LibProxy {
    @SidedProxy
    private static LibProxy proxy;

    public static LibProxy getProxy() {
        return proxy;
    }

    void postRegisterItem(ItemBuildCraft_BC8 item) {}

    void postRegisterBlock(BlockBuildCraftBase_BC8 block) {}

    void fmlPreInit() {}

    void fmlInit() {}

    void fmlPostInit() {}

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public EntityPlayer getPlayerForContext(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends LibProxy {
        @Override
        public void postRegisterItem(ItemBuildCraft_BC8 item) {
            item.postRegisterClient();
        }

        @Override
        void fmlInit() {
            super.fmlInit();
            ItemBuildCraft_BC8.fmlInitClient();
        }

        @Override
        public World getClientWorld() {
            return Minecraft.getMinecraft().theWorld;
        }

        @Override
        public EntityPlayer getClientPlayer() {
            return Minecraft.getMinecraft().thePlayer;
        }

        @Override
        public EntityPlayer getPlayerForContext(MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                return super.getPlayerForContext(ctx);
            }
            return getClientPlayer();
        }
    }

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends LibProxy {}
}

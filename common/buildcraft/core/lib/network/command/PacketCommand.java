/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network.command;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.network.PacketIds;

import io.netty.buffer.ByteBuf;

public class PacketCommand extends Packet {
    public static final ArrayList<CommandTarget> targets;
    public ByteBuf stream;
    public String command;
    public Object target;
    public CommandTarget handler;
    private CommandWriter writer;
    /** Only used for reading */
    private EntityPlayer player;

    static {
        targets = new ArrayList<CommandTarget>();
        targets.add(new CommandTargetTile());
        targets.add(new CommandTargetEntity());
        targets.add(new CommandTargetContainer());
    }

    public PacketCommand() {}

    public PacketCommand(Object target, String command, CommandWriter writer) {
        super();

        this.target = target;
        this.command = command;
        this.writer = writer;

        this.isChunkDataPacket = true;

        // Find the valid handler
        for (CommandTarget c : targets) {
            if (c.getHandledClass().isAssignableFrom(target.getClass())) {
                this.handler = c;
                break;
            }
        }
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);
        NetworkUtils.writeUTF(data, command);
        data.writeByte(targets.indexOf(handler));
        handler.write(data, target);
        if (writer != null) {
            writer.write(data);
        }
    }

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);
        command = NetworkUtils.readUTF(data);
        handler = targets.get(data.readUnsignedByte());
        stream = data; // for further reading
        this.player = player;
    }

    @Override
    public int getID() {
        return PacketIds.COMMAND;
    }

    @Override
    public void applyData(World world) {
        if (handler != null) {
            ICommandReceiver receiver = handler.handle(player, stream, world);
            if (receiver != null) {
                receiver.receiveCommand(command, FMLCommonHandler.instance().getEffectiveSide(), player, stream);
            }
        }
    }
}

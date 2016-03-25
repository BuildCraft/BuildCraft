package buildcraft.transport;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

import buildcraft.api.core.BCLog;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.IMCHandler;

public class IMCHandlerTransport extends IMCHandler {
    @Override
    public void processIMCEvent(IMCEvent event, IMCMessage m) {
        if (m.key.equals("add-facade")) {
            processAddFacadeIMC(event, m);
        } else if (m.key.equals("blacklist-facade")) {
            processBlacklistFacadeIMC(event, m);
        } else if (m.key.equals("add-gate-expansion-recipe")) {
            processGateExpansionRecipeAddIMC(event, m);
        }
    }

    public static void processGateExpansionRecipeAddIMC(IMCEvent event, IMCMessage msg) {
        boolean failed = false;
        if (!msg.isNBTMessage()) {
            failed = true;
        } else {
            NBTTagCompound recipe = msg.getNBTValue();
            if (!recipe.hasKey("id") || !recipe.hasKey("expansion") || !recipe.hasKey("input")) {
                failed = true;
            } else {
                IGateExpansion exp = GateExpansions.getExpansion(recipe.getString("expansion"));
                ItemStack is = ItemStack.loadItemStackFromNBT(recipe.getCompoundTag("input"));
                if (exp == null || is == null) {
                    failed = true;
                } else {
                    GateExpansions.registerExpansion(exp, is);
                }
            }
        }
        if (failed) {
            BCLog.logger.warn("Received invalid gate expansion recipe IMC message from mod %s!", msg.getSender());
        }
    }

    public static void processAddFacadeIMC(IMCEvent event, IMCMessage m) {
        try {
            if (m.isStringMessage()) {
                Splitter splitter = Splitter.on("@").trimResults();

                String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
                if (array.length <= 0 || array.length >= 3) {
                    BCLog.logger.info(String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                } else {
                    String blockName = array[0];
                    Integer metaId = array.length == 1 ? -1 : Ints.tryParse(array[1]);

                    if (Strings.isNullOrEmpty(blockName) || metaId == null) {
                        BCLog.logger.info(String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                    } else {
                        Block block = Block.blockRegistry.getObject(new ResourceLocation(blockName));
                        if (metaId < 0) {
                            ItemFacade.whitelistFacade(block);
                        } else {
                            ItemFacade.whitelistFacade(block.getStateFromMeta(metaId));
                        }
                    }
                }
            } else if (m.isItemStackMessage()) {
                ItemStack modItemStack = m.getItemStackValue();
                Block block = Block.getBlockFromItem(modItemStack.getItem());

                if (block != null) {
                    if (modItemStack.getItemDamage() < 0) {
                        ItemFacade.whitelistFacade(block);
                    } else {
                        ItemFacade.whitelistFacade(block.getStateFromMeta(modItemStack.getItemDamage()));
                    }
                }
            }
        } catch (Exception ex) {}
    }

    // TODO: Merge with AddFacadeIMC
    public static void processBlacklistFacadeIMC(IMCEvent event, IMCMessage m) {
        try {
            if (m.isStringMessage()) {
                Splitter splitter = Splitter.on("@").trimResults();

                String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
                if (array.length <= 0 || array.length >= 3) {
                    BCLog.logger.info(String.format("Received an invalid blacklist-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                } else {
                    String blockName = array[0];
                    Integer metaId = array.length == 1 ? -1 : Ints.tryParse(array[1]);

                    if (Strings.isNullOrEmpty(blockName) || metaId == null) {
                        BCLog.logger.info(String.format("Received an invalid blacklist-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                    } else {
                        Block block = Block.blockRegistry.getObject(new ResourceLocation(blockName));
                        if (metaId < 0) {
                            ItemFacade.blacklistFacade(block);
                        } else {
                            ItemFacade.blacklistFacade(block.getStateFromMeta(metaId));
                        }
                    }
                }
            } else if (m.isItemStackMessage()) {
                ItemStack modItemStack = m.getItemStackValue();

                Block block = Block.getBlockFromItem(modItemStack.getItem());
                if (block != null) {
                    if (modItemStack.getItemDamage() < 0) {
                        ItemFacade.blacklistFacade(block);
                    } else {
                        ItemFacade.blacklistFacade(block.getStateFromMeta(modItemStack.getItemDamage()));
                    }
                }
            }
        } catch (Throwable e) {}
    }
}

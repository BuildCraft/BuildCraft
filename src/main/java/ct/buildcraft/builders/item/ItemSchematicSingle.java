/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.item;

import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.builders.snapshot.SchematicBlockManager;
import ct.buildcraft.lib.inventory.InventoryWrapper;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ItemSchematicSingle extends Item {
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(Item.Properties prop) {
        super(prop);
//        setHasSubtypes(true);
    }
    
    
    @Override
	public int getMaxStackSize(ItemStack stack) {
    	return stack.getDamageValue() == DAMAGE_CLEAN ? 16 : super.getMaxStackSize(stack);
	}

/*    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, DAMAGE_CLEAN, "clean");
        addVariant(variants, DAMAGE_USED, "used");
    }*/
    
    

    @Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player,
			InteractionHand hand) {
    	ItemStack stack = StackUtil.asNonNull(player.getItemInHand(hand));
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (player.isCrouching()) {
            CompoundTag itemData = NBTUtilBC.getItemData(stack);
            itemData.remove(NBT_KEY);
            if (itemData.isEmpty()) {
                stack.setTag(null);
            }
            stack.setDamageValue(DAMAGE_CLEAN);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}


    @Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    	Level world = context.getLevel();
    	Player player = context.getPlayer();
    	InteractionHand hand = context.getHand();
    	Direction side = context.getHorizontalDirection();
    	BlockPos pos = context.getClickedPos();
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        if (player.isCrouching()) {
            CompoundTag itemData = NBTUtilBC.getItemData(StackUtil.asNonNull(stack));
            itemData.remove(NBT_KEY);
            if (itemData.isEmpty()) {
                stack.setTag(null);;
            }
            stack.setDamageValue(DAMAGE_CLEAN);
            return InteractionResult.SUCCESS;
        }
        int damage = stack.getDamageValue();
        BlockState state = world.getBlockState(pos);
        if (damage != DAMAGE_USED) {
            
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                world,
                pos,
                pos,
                state,
                state.getBlock()
            ));
            if (schematicBlock.isAir()) {
                return InteractionResult.FAIL;
            }
            NBTUtilBC.getItemData(stack).put(NBT_KEY, SchematicBlockManager.writeToNBT(schematicBlock));
            stack.setDamageValue(DAMAGE_USED);
            return InteractionResult.SUCCESS;
        } else {
            BlockPos placePos = pos;
            boolean replaceable = world.getBlockState(pos).canBeReplaced(new BlockPlaceContext(context));
            if (!replaceable) {
                placePos = placePos.offset(side.getNormal());
            }
            if (replaceable && !world.isEmptyBlock(placePos)) {
               if(!world.setBlockAndUpdate(placePos, Blocks.AIR.defaultBlockState()))
            	   return InteractionResult.FAIL;
            }
            try {
                ISchematicBlock schematicBlock = getSchematic(stack);
                if (schematicBlock != null) {
                    if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                        List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids();
                        List<ItemStack> requiredItems = schematicBlock.computeRequiredItems();
                        if (requiredFluids.isEmpty()) {
                            InventoryWrapper itemTransactor = new InventoryWrapper(player.getInventory());
                            if (StackUtil.mergeSameItems(requiredItems).stream().noneMatch(s ->
                                itemTransactor.extract(
                                    extracted -> StackUtil.canMerge(s, extracted),
                                    s.getCount(),
                                    s.getCount(),
                                    true
                                ).isEmpty()
                            )) {
                                if (schematicBlock.build(world, placePos)) {
                                    StackUtil.mergeSameItems(requiredItems).forEach(s ->
                                        itemTransactor.extract(
                                            extracted -> StackUtil.canMerge(s, extracted),
                                            s.getCount(),
                                            s.getCount(),
                                            false
                                        )
                                    );
                                    SoundUtil.playBlockPlace(world, placePos);
                                    player.swing(hand);
                                    return InteractionResult.SUCCESS;
                                }
                            } else {
                            	MutableComponent text = MutableComponent.create(new LiteralContents("Not enough items. Total needed: "));
                            	StackUtil.mergeSameItems(requiredItems).forEach(item -> text.append(item.getDisplayName()));
                                player.displayClientMessage(text, true);//TODO Check method
                            }
                        } else {
                            player.displayClientMessage(
                                Component.literal("Schematic requires fluids"),
                                true
                            );
                        }
                    }
                }
            } catch (InvalidInputDataException e) {
                player.displayClientMessage(
                		Component.literal("Invalid schematic: " + e.getMessage()),
                    true
                );
                e.printStackTrace();
            }
            return InteractionResult.FAIL;
        }
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            return SchematicBlockManager.readFromNBT(NBTUtilBC.getItemData(stack).getCompound(NBT_KEY));
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            BCLog.logger.warn("Invalid schematic " + e.getMessage());
            return null;
        }
    }
}

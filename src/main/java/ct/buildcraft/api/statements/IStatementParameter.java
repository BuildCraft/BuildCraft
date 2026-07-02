/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.statements;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface IStatementParameter extends IGuiSlot {

    /** @return An {@link ItemStack} to render for this parameter, or {@link ItemStack#EMPTY} if this should not render
     *         an {@link ItemStack}. */
    @Nonnull
    ItemStack getItemStack();

    default DrawType getDrawType() {
        return DrawType.SPRITE_STACK;
    }

    /** Return a non-null value to be set as the statement parameter if you handled the mouse click and do not want all
     * possible values to be shown, or null if you did nothing and wish to show all possible values.
     * 
     * @see #getPossible(IStatementContainer) */
    IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse);

    void writeToNbt(CompoundTag nbt);

    /** Writes this parameter to the given {@link FriendlyByteBuf}. The default implementation writes out the value of
     * {@link #writeToNbt(CompoundTag)}, and that will be passed back into
     * {@link IParameterReader#readFromNbt(CompoundTag)}.
     * <p>
     * It is likely that implementors can write a more compact form of themselves, so they are encouraged to override
     * this and also register an {@link IParamReaderBuf} in
     * {@link StatementManager#registerParameter(String, IParamReaderBuf)} or
     * {@link StatementManager#registerParameter(IParameterReader, IParamReaderBuf)} */
    default void writeToBuf(FriendlyByteBuf buffer) {
        CompoundTag nbt = new CompoundTag();
        writeToNbt(nbt);
        buffer.writeNbt(nbt);
    }

    /** This returns the parameter after a left rotation. Used in particular in blueprints orientation. */
    IStatementParameter rotateLeft();

    /** @return An array of all the possible alternative params for this state. Note that the return array may contain
     *         null if you want to space out the values. */
    IStatementParameter[] getPossible(IStatementContainer source);

    /** @return True if the possible array is set up specially for a direction, or false if they are not. This affects
     *         the selection hover layout. If this returns false then {@link #getPossible(IStatementContainer)} will be
     *         offset up by one, null added to 0, and all other nulls removed. */
    default boolean isPossibleOrdered() {
        return false;
    }

    public enum DrawType {
        /** Draws the sprite, as returned by {@link IStatementParameter#getSprite()}. */
        SPRITE_ONLY,

        /** Draws the {@link ItemStack}, as returned by {@link IStatementParameter#getItemStack()}. */
        STACK_ONLY,

        /** Only draws the {@link ItemStack}, as returned by {@link IStatementParameter#getItemStack()}, except if
         * {@link ItemStack#isEmpty()} returns true, in which case this a question mark will be drawn. */
        STACK_ONLY_OR_QUESTION_MARK,

        /** Draws {@link #SPRITE_ONLY}, but then also draws {@link #STACK_ONLY} */
        SPRITE_STACK,

        /** Draws {@link #SPRITE_ONLY}, but then also draws {@link #STACK_ONLY_OR_QUESTION_MARK} */
        SPRITE_STACK_OR_QUESTION_MARK,

        /** Draws {@link #STACK_ONLY}, but then also draws {@link #SPRITE_ONLY} */
        STACK_SPRITE,

        /** Draws {@link #STACK_ONLY_OR_QUESTION_MARK}, but then also draws {@link #SPRITE_ONLY} */
        STACK_OR_QUESTION_MARK_THEN_SPRITE
    }
}

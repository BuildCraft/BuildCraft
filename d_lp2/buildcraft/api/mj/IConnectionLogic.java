package buildcraft.api.mj;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/** Provides logic to check what this can connect to. */
public interface IConnectionLogic {
    /** Gets all of the possible machines that this can connect to. You don't have to check that they are valid (or even
     * exist), the power network will do that for you.
     * 
     * @param identifier The current machine to check from. Passed in to allow for enum types.
     * @return A collection of all connectable machines. null represents no connections. This collection will only be
     *         iterated, never mutated. */
    Iterable<MjMachineIdentifier> getConnectableMachines(MjMachineIdentifier identifier);

    /** @date Created on 2 Apr 2016 by AlexIIL */
    public enum AllSidesLogic implements IConnectionLogic {
        INSTANCE;

        @Override
        public Collection<MjMachineIdentifier> getConnectableMachines(MjMachineIdentifier identifier) {
            BlockPos pos = identifier.pos;
            ImmutableList.Builder<MjMachineIdentifier> identifiers = ImmutableList.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                identifiers.add(new MjMachineIdentifier(identifier.dimension, pos.offset(face), face.getOpposite()));
            }
            return identifiers.build();
        }
    }

    /** @date Created on 2 Apr 2016 by AlexIIL */
    public enum OppositeSideLogic implements IConnectionLogic {
        INSTANCE;

        @Override
        public Collection<MjMachineIdentifier> getConnectableMachines(MjMachineIdentifier identifier) {
            EnumFacing face = identifier.face;
            if (face == null) return null;
            return ImmutableList.of(new MjMachineIdentifier(identifier.dimension, identifier.pos.offset(face), face.getOpposite()));
        }
    }
}

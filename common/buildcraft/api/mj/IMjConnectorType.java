package buildcraft.api.mj;

import javax.annotation.Nonnull;

/** Designates a type that an MJ connector can have. The default (simple) values are in {@link MjSimpleType}, however
 * feel free to add more types in a different enum/class if you wish. This can also be used to provide additional
 * information that is not present in {@link MjSimpleType} */
public interface IMjConnectorType {
    /** Checks to see if this type is the same as, or a child of, the other type. */
    boolean is(@Nonnull IMjConnectorType other);

    /** Gets the most appropriate {@link MjSimpleType} that this type represents. If your type machine doesn't fit
     * easily into this system then open an issue at
     *
     * @see https://github.com/BuildCraft/BuildCraftAPI/issues */
    @Nonnull
    MjSimpleType getSimpleType();
}

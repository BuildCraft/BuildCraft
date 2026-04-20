package ct.buildcraft.api.transport.pipe;

import java.util.Arrays;

public class PipeFaceTex {

    private static final int SINGLE_WHITE_SPRITES_COUNT = 64;
    private static final PipeFaceTex[] SINGLE_WHITE_SPRITES;
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    public static final PipeFaceTex NO_SPRITE = new PipeFaceTex();

    static {
        SINGLE_WHITE_SPRITES = new PipeFaceTex[SINGLE_WHITE_SPRITES_COUNT];
        for (int i = 0; i < SINGLE_WHITE_SPRITES_COUNT; i++) {
            SINGLE_WHITE_SPRITES[i] = new PipeFaceTex(i);
        }
    }

    private final int[] textures;
    /** The colours of the faces. Will be empty if every colour is -1 (i.e none of the faces are coloured) */
    private final int[] colours;

    private final int hash;

    public static PipeFaceTex get(int[] textures, int... colours) {
        switch (textures.length) {
            case 0: {
                return NO_SPRITE;
            }
            case 1: {
                if (colours.length == 0 || colours[0] == -1) {
                    return get(textures[0]);
                }
                return new PipeFaceTex(textures, colours);
            }
            default: {
                return new PipeFaceTex(textures, colours);
            }
        }
    }

    /** @return A {@link PipeFaceTex} with all the textures given, and every colour being 0xFF_FF_FF. */
    public static PipeFaceTex get(int... textures) {
        return get(textures, EMPTY_INT_ARRAY);
    }

    /** @return A {@link PipeFaceTex} with the single texture given, and the single colour being 0xFF_FF_FF. */
    public static PipeFaceTex get(int singleTexture) {
        if (singleTexture < SINGLE_WHITE_SPRITES_COUNT) {
            return SINGLE_WHITE_SPRITES[singleTexture];
        }
        return new PipeFaceTex(singleTexture);
    }

    private PipeFaceTex(int[] textures, int... colours) {
        this.textures = textures;
        this.colours = colours;
        for (int i = 0; i < colours.length; i++) {
            // Remove any alpha
            colours[i] &= 0xFF_FF_FF;
        }
        if (textures.length == 0) {
            hash = -1;
        } else if (textures.length == 1) {
            // If the colour is -1 then this will equal the hash below
            hash = textures[0] + getColour(0) + 1;
        } else {
            int[] cArr = colours;
            {
                int prevLength = cArr.length;
                int count = getCount();
                if (prevLength < count) {
                    cArr = Arrays.copyOf(cArr, count);
                    for (int i = prevLength; i < count; i++) {
                        cArr[i] = -1;
                    }
                }
            }
            hash = Arrays.hashCode(cArr) + 31 * Arrays.hashCode(textures);
        }
    }

    private PipeFaceTex(int... textures) {
        this(textures, EMPTY_INT_ARRAY);
    }

    private PipeFaceTex(int singleTexture) {
        textures = new int[] { singleTexture };
        colours = EMPTY_INT_ARRAY;
        hash = singleTexture;
    }

    public int getCount() {
        return textures.length;
    }

    public int getTexture(int index) {
        return textures[index];
    }

    public int getColour(int index) {
        if (index >= colours.length) {
            return -1;
        }
        return colours[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        PipeFaceTex other = (PipeFaceTex) obj;
        if (hash != other.hash) {
            return false;
        }
        if (!Arrays.equals(textures, other.textures)) {
            return false;
        }
        for (int i = textures.length; i > 0;) {
            i--;
            if (getColour(i) != other.getColour(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /** INTERNAL (FOR TESTING - DO NOT USE) */
    public static PipeFaceTex ___testing_create_single(int single) {
        return new PipeFaceTex(single);
    }
}

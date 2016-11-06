package buildcraft.lib.client.sprite;

import java.util.function.IntSupplier;

public class SpriteOptionDependent extends SpriteDelegate {
    private final IntSupplier indexSupplier;
    private final ISprite[] possible;

    public SpriteOptionDependent(IntSupplier indexSupplier, ISprite... possible) {
        this.indexSupplier = indexSupplier;
        this.possible = possible;
    }

    @Override
    public ISprite getDelegate() {
        int idx = indexSupplier.getAsInt();
        return possible[idx % possible.length];
    }
}

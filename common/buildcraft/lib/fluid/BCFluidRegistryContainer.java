package buildcraft.lib.fluid;

import net.minecraft.item.BucketItem;
import net.minecraftforge.fml.RegistryObject;

public class BCFluidRegistryContainer {
    private RegistryObject<BCFluid.Source> still;
    private RegistryObject<BCFluid.Flowing> flowing;
    private RegistryObject<BCFluidBlock> block;
    private RegistryObject<BucketItem> bucket;


    public BCFluid.Source getStill() {
        return still.get();
    }

    public BCFluid.Flowing getFlowing() {
        return flowing.get();
    }

    public BCFluidBlock getBlock() {
        return block.get();
    }

    public BucketItem getBucket() {
        return bucket.get();
    }


    public void setStill(RegistryObject<BCFluid.Source> stillFluid) {
        this.still = stillFluid;
    }

    public void setFlowing(RegistryObject<BCFluid.Flowing> flowingFluid) {
        this.flowing = flowingFluid;
    }

    public void setBlock(RegistryObject<BCFluidBlock> block) {
        this.block = block;
    }

    public void setBucket(RegistryObject<BucketItem> bucket) {
        this.bucket = bucket;
    }
}

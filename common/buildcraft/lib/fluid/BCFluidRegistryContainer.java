package buildcraft.lib.fluid;

import buildcraft.lib.item.ItemBucketBC;
import net.minecraftforge.registries.RegistryObject;

public class BCFluidRegistryContainer {
    private RegistryObject<BCFluid.Source> still;
    private RegistryObject<BCFluid.Flowing> flowing;
    private RegistryObject<BCFluidBlock> block;
    private RegistryObject<ItemBucketBC> bucket;
    private BCFluidAttributes fluidType;


    public BCFluid.Source getStill() {
        return still.get();
    }

    public BCFluid.Flowing getFlowing() {
        return flowing.get();
    }

    public BCFluidBlock getBlock() {
        return block.get();
    }

    public ItemBucketBC getBucket() {
        return bucket.get();
    }

    public BCFluidAttributes getFluidType() {
        return fluidType;
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

    public void setBucket(RegistryObject<ItemBucketBC> bucket) {
        this.bucket = bucket;
    }

    public void setFluidType(BCFluidAttributes fluidType) {
        this.fluidType = fluidType;
    }
}

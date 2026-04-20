package ct.buildcraft.energy;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class BCEnergyProvider{
	static class BlockState extends BlockStateProvider{

		public BlockState(DataGenerator generator, ExistingFileHelper helper) {
			super(generator, BCEnergy.MODID, helper);
		}

		@Override
		protected void registerStatesAndModels() {
			
	    	try {
				for(int id=0;id<BCEnergyFluids.NAME.length;id++) 
				    for (int h = 0; h < 3; h++) {
				        String name = BCEnergyFluids.NAME[id];
				        RegistryObject<LiquidBlock> registryObject = BCEnergyFluids.OIL_BLOCK.get(3 * id + h);
						LiquidBlock block = registryObject.get();
						simpleBlock(block, new ConfiguredModel(models().getBuilder("buildcraftenergy:fluids/"+name+"/"+BCEnergyFluids.HEAT_NAMES[h])));
				        	//.texture("particle", "buildcraftenergy:blocks/fluids/"+name+"/"+BCEnergyFluids.HEAT_NAMES[h]+"_still");
				    }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	static class BlockModel extends BlockModelProvider{

		public BlockModel(DataGenerator gen, ExistingFileHelper exFileHelper) {
			super(gen, BCEnergy.MODID, exFileHelper);
		}

		@Override
		protected void registerModels() {
	    	try {
				for(int id=0;id<BCEnergyFluids.NAME.length;id++) 
				    for (int h = 0; h < 3; h++) {
				        String name = BCEnergyFluids.NAME[id];
				        getBuilder("buildcraftenergy:fluids/"+name+"/"+BCEnergyFluids.HEAT_NAMES[h])
				        	.texture("particle", "buildcraftenergy:blocks/fluids/"+name+"/"+BCEnergyFluids.HEAT_NAMES[h]+"_still");
				    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	static class ItemModel extends ItemModelProvider{

		public ItemModel(DataGenerator gen, ExistingFileHelper exFileHelper) {
			super(gen, BCEnergy.MODID, exFileHelper);
		}

		@Override
		protected void registerModels() {
	    	try {
				for(int id=0;id<BCEnergyFluids.NAME.length;id++) 
				    for (int h = 0; h < 3; h++) {
				        String name = BCEnergyFluids.NAME[id];
				        getBuilder("buildcraftenergy:item/"+name+"/"+BCEnergyFluids.HEAT_NAMES[h]+"_bucket")
				        	.parent(this.getBuilder("forge:item/bucket_drip"))
				        	.customLoader(DynamicFluidContainerModelBuilder::begin)
				        			.applyTint(false).flipGas(true).fluid(BCEnergyFluids.OIL_SOURCE.get(3*id+h).get()).end();
				    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	
}

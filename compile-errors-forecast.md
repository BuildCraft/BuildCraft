# Anticipated Compile Errors After Build Migration (Phase 1)

This document lists every category of compile error that will appear when
`./gradlew build` is run after the Gradle migration, **before any Java source
files are touched**.  It is grouped by submodule and by error category.

The errors are structural — they arise entirely from the removal of the Forge
and FML runtimes from the compile classpath.  No logic bugs are implied.

---

## Error Category Key

| Code | Category |
|------|----------|
| `FORGE-IMPORT` | `import net.minecraftforge.*` — symbol not on Fabric classpath |
| `MC-RENAME` | MC class renamed between 1.12.2 and 1.20.1 (different package or name) |
| `API-REMOVED` | Entire API removed with no direct replacement (needs bespoke porting) |
| `DEPRECATED-REMOVED` | Was deprecated in intermediate versions, deleted in 1.20.1 |

---

## Affected File Counts (pre-porting)

| Module | Files with Forge imports | Approx. Forge import lines | Approx. MC rename hits |
|---|---:|---:|---:|
| lib | 152 | 372 | ~893 |
| core | 40 | 121 | ~274 |
| energy | 19 | 57 | ~91 |
| transport | 58 | 171 | ~722 |
| factory | 26 | 79 | ~281 |
| silicon | 33 | 92 | ~295 |
| builders | 52 | 121 | ~416 |
| robotics | 7 | 22 | ~34 |
| BuildCraftAPI/api | ~15 (est.) | ~40 (est.) | ~60 (est.) |
| **Total** | **~402** | **~1,075** | **~3,066** |

---

## Per-Module Error Tables

### `buildcraft.lib` — Core Infrastructure (Highest Volume)

| Forge Class / API | Error Code | Fabric / 1.20.1 Replacement | Notes |
|---|---|---|---|
| `net.minecraftforge.fml.relauncher.Side` | `FORGE-IMPORT` | `net.fabricmc.api.EnvType` | Rename |
| `net.minecraftforge.fml.relauncher.SideOnly` | `FORGE-IMPORT` | `net.fabricmc.api.Environment` | Rename |
| `net.minecraftforge.fml.common.Mod` | `FORGE-IMPORT` | `fabric.mod.json` entrypoint | No annotation equivalent |
| `net.minecraftforge.fml.common.Mod.Instance` | `API-REMOVED` | Not needed in Fabric | Remove field + annotation |
| `net.minecraftforge.fml.common.SidedProxy` | `API-REMOVED` | `ModInitializer` / `ClientModInitializer` split | Refactor to separate init classes |
| `net.minecraftforge.fml.common.event.FMLPreInitializationEvent` | `API-REMOVED` | `ModInitializer.onInitialize()` | Collapse 3-phase init to 1 |
| `net.minecraftforge.fml.common.event.FMLInitializationEvent` | `API-REMOVED` | `ModInitializer.onInitialize()` | |
| `net.minecraftforge.fml.common.event.FMLPostInitializationEvent` | `API-REMOVED` | `ModInitializer.onInitialize()` | |
| `net.minecraftforge.fml.common.network.simpleimpl.IMessage` | `FORGE-IMPORT` | `net.minecraft.network.packet.CustomPayload` | Rewrite networking layer |
| `net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler` | `FORGE-IMPORT` | `ServerPlayNetworking.PlayPayloadHandler` | |
| `net.minecraftforge.fml.common.network.simpleimpl.MessageContext` | `FORGE-IMPORT` | `ServerPlayNetworking.Context` | |
| `net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper` | `FORGE-IMPORT` | `PayloadTypeRegistry` | |
| `net.minecraftforge.fml.common.network.IGuiHandler` | `API-REMOVED` | `NamedScreenHandlerFactory` | |
| `net.minecraftforge.fml.common.network.NetworkRegistry` | `FORGE-IMPORT` | Fabric networking API | |
| `net.minecraftforge.fml.common.registry.ForgeRegistries` | `FORGE-IMPORT` | `net.minecraft.registry.Registries` | Rename |
| `net.minecraftforge.fml.common.Loader` / `LoaderState` | `API-REMOVED` | `FabricLoader.getInstance()` | |
| `net.minecraftforge.fml.common.FMLCommonHandler` | `API-REMOVED` | `MinecraftServer` / lifecycle events | |
| `net.minecraftforge.fml.client.FMLClientHandler` | `API-REMOVED` | `MinecraftClient.getInstance()` | Client only |
| `net.minecraftforge.common.capabilities.Capability` | `FORGE-IMPORT` | Fabric Transfer API `Storage<V>` | Full redesign (see migration-plan.md §3d) |
| `net.minecraftforge.common.capabilities.ICapabilityProvider` | `API-REMOVED` | Direct `instanceof` / Transfer API lookup | Remove interface from TileBC_Neptune |
| `net.minecraftforge.common.capabilities.CapabilityManager` | `API-REMOVED` | No equivalent; use Transfer API | |
| `net.minecraftforge.common.capabilities.CapabilityInject` | `API-REMOVED` | No equivalent | |
| `net.minecraftforge.common.ForgeChunkManager` | `FORGE-IMPORT` | `ServerWorld.setChunkForced()` | |
| `net.minecraftforge.common.MinecraftForge` (event bus) | `FORGE-IMPORT` | Fabric API static event instances | |
| `net.minecraftforge.common.DimensionManager` | `FORGE-IMPORT` | `MinecraftServer.getWorldRegistryKeys()` | |
| `net.minecraftforge.common.util.FakePlayer` | `FORGE-IMPORT` | `FakeServerPlayerEntity` (Fabric API) | |
| `net.minecraftforge.common.config.Configuration` | `FORGE-IMPORT` | Cloth Config or custom JSON config | |
| `net.minecraftforge.common.crafting.CraftingHelper` | `FORGE-IMPORT` | Vanilla `RecipeManager` | |
| `net.minecraftforge.common.crafting.IShapedRecipe` | `FORGE-IMPORT` | `CraftingRecipe` | |
| `net.minecraftforge.fluids.FluidStack` | `FORGE-IMPORT` | `(FluidVariant, long)` — Fabric Transfer API | Amount unit: mB → droplets (×81000) |
| `net.minecraftforge.fluids.Fluid` | `FORGE-IMPORT` | `net.minecraft.fluid.Fluid` (compatible) | Package moved |
| `net.minecraftforge.fluids.FluidTank` | `FORGE-IMPORT` | `SingleVariantStorage<FluidVariant>` | |
| `net.minecraftforge.fluids.FluidRegistry` | `FORGE-IMPORT` | `net.minecraft.registry.Registries.FLUID` | |
| `net.minecraftforge.fluids.FluidUtil` | `FORGE-IMPORT` | Fabric Transfer API fluid utilities | |
| `net.minecraftforge.fluids.capability.IFluidHandler` | `FORGE-IMPORT` | `Storage<FluidVariant>` | |
| `net.minecraftforge.fluids.capability.IFluidTankProperties` | `API-REMOVED` | No equivalent; query `Storage` directly | |
| `net.minecraftforge.fluids.capability.CapabilityFluidHandler` | `API-REMOVED` | `FluidStorage.SIDED` lookup | |
| `net.minecraftforge.items.IItemHandler` | `FORGE-IMPORT` | `Storage<ItemVariant>` | |
| `net.minecraftforge.items.IItemHandlerModifiable` | `FORGE-IMPORT` | `Storage<ItemVariant>` | |
| `net.minecraftforge.items.CapabilityItemHandler` | `API-REMOVED` | `ItemStorage.SIDED` lookup | |
| `net.minecraftforge.items.ItemHandlerHelper` | `FORGE-IMPORT` | `StorageUtil` (Fabric Transfer API) | |
| `net.minecraftforge.items.SlotItemHandler` | `FORGE-IMPORT` | Custom `Slot` subclass | |
| `net.minecraftforge.energy.IEnergyStorage` | `FORGE-IMPORT` | `EnergyStorage` (Team Reborn) | |
| `net.minecraftforge.energy.CapabilityEnergy` | `API-REMOVED` | `EnergyStorage.SIDED` (Team Reborn) | |
| `net.minecraftforge.oredict.OreDictionary` | `API-REMOVED` | Vanilla item tags (`TagKey<Item>`) | |
| `net.minecraftforge.oredict.ShapedOreRecipe` | `API-REMOVED` | Vanilla shaped recipe JSON | |
| `net.minecraftforge.oredict.ShapelessOreRecipe` | `API-REMOVED` | Vanilla shapeless recipe JSON | |
| `net.minecraftforge.event.RegistryEvent` | `API-REMOVED` | `Registry.register()` calls in `onInitialize` | |
| `net.minecraftforge.event.entity.EntityJoinWorldEvent` | `FORGE-IMPORT` | `EntityWorldEvents.AFTER_ENTITIES_LOAD` | |
| `net.minecraftforge.fml.common.eventhandler.SubscribeEvent` | `API-REMOVED` | Fabric API event registrations | |
| `net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent` | `FORGE-IMPORT` | `ServerTickEvents.END_SERVER_TICK` | |
| `net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent` | `FORGE-IMPORT` | `ClientTickEvents.END_CLIENT_TICK` | |
| `net.minecraftforge.client.event.ModelBakeEvent` | `FORGE-IMPORT` | `ModelLoadingPlugin` | |
| `net.minecraftforge.client.event.ModelRegistryEvent` | `FORGE-IMPORT` | `ModelLoadingPlugin` | |
| `net.minecraftforge.client.event.RenderWorldLastEvent` | `FORGE-IMPORT` | `WorldRenderLastCallback` | |
| `net.minecraftforge.client.event.TextureStitchEvent` | `FORGE-IMPORT` | `ClientSpriteRegistryCallback` / resource pack API | |
| `net.minecraftforge.client.model.ModelLoader` | `FORGE-IMPORT` | `ModelLoader` (1.20 vanilla, different API) | |
| `net.minecraftforge.client.model.animation.FastTESR` | `API-REMOVED` | `BlockEntityRenderer` | |
| `net.minecraftforge.client.model.IModel` | `API-REMOVED` | `UnbakedModel` | |
| `net.minecraftforge.fml.client.registry.ClientRegistry` | `API-REMOVED` | `BlockEntityRendererFactories.register()` | |
| `net.minecraftforge.registries.IForgeRegistryEntry` | `API-REMOVED` | No equivalent; remove from class hierarchy | |
| `net.minecraftforge.common.property.IUnlistedProperty` | `API-REMOVED` | Removed from 1.20 block state system | |
| `TileEntity` (class name) | `MC-RENAME` | `BlockEntity` | Package also changed |
| `IBlockState` | `MC-RENAME` | `BlockState` | |
| `NBTTagCompound` | `MC-RENAME` | `CompoundTag` | |
| `NBTTagList` | `MC-RENAME` | `ListTag` | |
| `NBTUtil` | `MC-RENAME` | `NbtHelper` | |
| `EnumFacing` | `MC-RENAME` | `Direction` | |
| `EnumHand` | `MC-RENAME` | `Hand` | |
| `World` | `MC-RENAME` | `Level` | |
| `WorldServer` | `MC-RENAME` | `ServerLevel` | |
| `EntityPlayer` | `MC-RENAME` | `Player` | |
| `EntityPlayerMP` | `MC-RENAME` | `ServerPlayer` | |
| `EntityLivingBase` | `MC-RENAME` | `LivingEntity` | |
| `SPacketUpdateTileEntity` | `MC-RENAME` | `ClientboundBlockEntityDataPacket` | |
| `BlockStateContainer` | `MC-RENAME` | `StateDefinition` | |
| `IProperty` | `MC-RENAME` | `Property` | |
| `DrawableHelper` | `MC-RENAME` | `GuiGraphics` (draw calls moved to it) | |

---

### `buildcraft.core`

| Forge API | Error Code | Replacement |
|---|---|---|
| All `FORGE-IMPORT` from lib (inherited) | `FORGE-IMPORT` | Same as lib table |
| `net.minecraftforge.client.event.RenderGameOverlayEvent` | `FORGE-IMPORT` | `HudRenderCallback` |
| `net.minecraftforge.common.ISpecialArmor` | `API-REMOVED` | No Fabric equivalent; custom attribute system |
| `net.minecraftforge.common.config.ConfigCategory` / `ConfigElement` | `API-REMOVED` | Cloth Config API |
| `net.minecraftforge.fml.client.IModGuiFactory` | `API-REMOVED` | Cloth Config screen factory |
| `net.minecraftforge.fml.client.config.GuiConfig` / `IConfigElement` | `API-REMOVED` | Cloth Config |
| `net.minecraftforge.fml.client.event.ConfigChangedEvent` | `API-REMOVED` | Cloth Config callbacks |
| `net.minecraftforge.event.terraingen.PopulateChunkEvent` | `API-REMOVED` | `ChunkGeneratorMixin` / `FabricChunkGenerator` |
| `net.minecraftforge.event.entity.player.ItemTooltipEvent` | `FORGE-IMPORT` | `ItemTooltipCallback` |
| `net.minecraftforge.fml.common.event.FMLServerStartingEvent` | `FORGE-IMPORT` | `ServerLifecycleEvents.SERVER_STARTING` |
| `net.minecraftforge.fluids.capability.FluidTankProperties` | `API-REMOVED` | `SingleVariantStorage` |
| `IBlockState` → `BlockState` | `MC-RENAME` | `BlockState` |
| `BlockStateContainer` → `StateDefinition` | `MC-RENAME` | `StateDefinition.Builder` |

---

### `buildcraft.energy`

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.energy.IEnergyStorage` | `FORGE-IMPORT` | `teamreborn.energy.api.EnergyStorage` |
| `net.minecraftforge.energy.CapabilityEnergy` | `API-REMOVED` | `EnergyStorage.SIDED` lookup |
| `net.minecraftforge.common.BiomeDictionary` | `API-REMOVED` | Vanilla biome tag system (`BiomeTags`) |
| `net.minecraftforge.event.terraingen.WorldTypeEvent` | `API-REMOVED` | World generation API removed; use `FabricBiomeSource` |
| `net.minecraftforge.client.model.ModelFluid` | `API-REMOVED` | `FluidRenderHandler` (Fabric Renderer API) |
| `net.minecraftforge.fluids.FluidRegistry` | `API-REMOVED` | `Registries.FLUID` |
| `net.minecraftforge.fml.common.registry.GameRegistry` | `API-REMOVED` | Direct `Registry.register()` |
| `net.minecraftforge.event.terraingen.PopulateChunkEvent` | `API-REMOVED` | Fabric world generation hooks |
| `BlockFluidBase` / `BlockFluidClassic` | `MC-RENAME` | `FlowableFluidBlock` (Fabric API) |

---

### `buildcraft.transport` (Second-Highest Volume)

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.common.property.ExtendedBlockState` | `API-REMOVED` | Removed; use standard `StateDefinition` |
| `net.minecraftforge.common.property.IExtendedBlockState` | `API-REMOVED` | Removed; store dynamic data in `BlockEntity` |
| `net.minecraftforge.common.property.IUnlistedProperty` | `API-REMOVED` | Removed |
| `net.minecraftforge.common.util.BlockSnapshot` | `API-REMOVED` | No direct equivalent |
| `net.minecraftforge.common.ForgeHooks` | `FORGE-IMPORT` | Various vanilla equivalents |
| `net.minecraftforge.common.IShearable` | `FORGE-IMPORT` | No Fabric equivalent (remove or use tag) |
| `net.minecraftforge.common.crafting.IConditionFactory` | `API-REMOVED` | Fabric Condition API |
| `net.minecraftforge.common.crafting.JsonContext` | `API-REMOVED` | Custom recipe JSON loading |
| `net.minecraftforge.event.world.ChunkWatchEvent` | `FORGE-IMPORT` | `ChunkWatchEvents` (Fabric API) |
| `net.minecraftforge.event.world.BlockEvent` | `FORGE-IMPORT` | `BlockBreakCallback` etc. |
| `net.minecraftforge.fml.common.event.FMLInterModComms` | `API-REMOVED` | No IMC in Fabric; use entrypoints or direct API |
| `WorldSavedData` | `MC-RENAME` | `PersistentState` |
| `ITickable` | `API-REMOVED` | `BlockEntityTicker<T>` returned from `Block.getTicker()` |
| All FluidStack / IFluidHandler | `FORGE-IMPORT` | Fabric Transfer API |
| All IItemHandler | `FORGE-IMPORT` | Fabric Transfer API |
| `net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent` | `FORGE-IMPORT` | `ServerTickEvents` |

---

### `buildcraft.factory`

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.common.crafting.IShapedRecipe` | `FORGE-IMPORT` | `CraftingRecipe` |
| `net.minecraftforge.fluids.FluidRegistry` | `API-REMOVED` | `Registries.FLUID` |
| `net.minecraftforge.fluids.capability.FluidTankProperties` | `API-REMOVED` | `SingleVariantStorage` |
| `net.minecraftforge.common.util.FakePlayer` | `FORGE-IMPORT` | `FakeServerPlayerEntity` |
| `net.minecraftforge.client.model.animation.FastTESR` | `API-REMOVED` | `BlockEntityRenderer` |
| All FluidStack / IFluidHandler / IFluidTankProperties | `FORGE-IMPORT` | Fabric Transfer API |
| All IItemHandlerModifiable | `FORGE-IMPORT` | Fabric Transfer API |
| `ITickable` | `API-REMOVED` | `BlockEntityTicker<T>` |

---

### `buildcraft.silicon`

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.client.ForgeHooksClient` | `FORGE-IMPORT` | No equivalent; use `MinecraftClient` methods |
| `net.minecraftforge.client.MinecraftForgeClient` | `FORGE-IMPORT` | No equivalent |
| `net.minecraftforge.common.ForgeHooks` | `FORGE-IMPORT` | Various vanilla equivalents |
| `net.minecraftforge.fluids.IFluidBlock` | `API-REMOVED` | Removed; check `Block instanceof FluidBlock` |
| `net.minecraftforge.fml.common.event.FMLInterModComms` | `API-REMOVED` | Entrypoints / direct API |
| `net.minecraftforge.common.crafting.JsonContext` | `API-REMOVED` | Vanilla recipe loading |
| `net.minecraftforge.fml.client.event.ConfigChangedEvent` | `API-REMOVED` | Cloth Config callbacks |
| `net.minecraftforge.fml.common.registry.ForgeRegistries` | `API-REMOVED` | `Registries.*` |
| `net.minecraftforge.oredict.ShapedOreRecipe` | `API-REMOVED` | Vanilla shaped recipe + item tags |
| Custom `IMessage` (gate sync) | `FORGE-IMPORT` | `CustomPayload` records |

---

### `buildcraft.builders`

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.client.event.RenderTooltipEvent` | `FORGE-IMPORT` | `TooltipComponentCallback` |
| `net.minecraftforge.event.world.GetCollisionBoxesEvent` | `API-REMOVED` | No direct equivalent; use `VoxelShape` override |
| `net.minecraftforge.common.util.INBTSerializable` | `FORGE-IMPORT` | `NbtSerializable` (or inline read/write) |
| `net.minecraftforge.fml.client.config.GuiUtils` | `API-REMOVED` | `GuiGraphics` helpers |
| `net.minecraftforge.fml.common.event.FMLServerStartingEvent` | `FORGE-IMPORT` | `ServerLifecycleEvents.SERVER_STARTING` |
| `net.minecraftforge.fml.common.gameevent.TickEvent` | `FORGE-IMPORT` | `ServerTickEvents` / `ClientTickEvents` |
| All FluidStack / FluidRegistry / FluidUtil | `FORGE-IMPORT` | Fabric Transfer API |
| `WorldSavedData` | `MC-RENAME` | `PersistentState` |
| `ITickable` on quarry | `API-REMOVED` | `BlockEntityTicker<T>` |
| `net.minecraftforge.common.util.FakePlayer` | `FORGE-IMPORT` | `FakeServerPlayerEntity` |
| `net.minecraftforge.fml.common.ModContainer` | `FORGE-IMPORT` | `ModContainer` (Fabric Loader — different API) |

---

### `buildcraft.robotics` (Lowest Volume)

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.fml.common.Mod` | `FORGE-IMPORT` | `fabric.mod.json` entrypoint |
| `net.minecraftforge.fml.common.SidedProxy` | `API-REMOVED` | Split init classes |
| `net.minecraftforge.fml.common.event.FML*Event` | `FORGE-IMPORT` | `ModInitializer.onInitialize()` |
| `net.minecraftforge.fml.common.eventhandler.SubscribeEvent` | `API-REMOVED` | Fabric API event hooks |
| `net.minecraftforge.fml.common.network.*` | `FORGE-IMPORT` | Fabric networking (`CustomPayload`) |
| `net.minecraftforge.event.RegistryEvent` | `API-REMOVED` | `Registry.register()` |
| `net.minecraftforge.fml.relauncher.Side` / `SideOnly` | `FORGE-IMPORT` | `EnvType` / `@Environment` |
| `net.minecraftforge.fml.client.registry.ClientRegistry` | `API-REMOVED` | `BlockEntityRendererFactories.register()` |

---

### `BuildCraftAPI/api` (BC Public API Subproject)

| Forge API | Error Code | Replacement |
|---|---|---|
| `net.minecraftforge.fml.common.Mod` (module IDs) | `FORGE-IMPORT` | `FabricLoader.getInstance().isModLoaded()` |
| `net.minecraftforge.common.capabilities.Capability` | `FORGE-IMPORT` | Fabric Transfer API / interface checks |
| `net.minecraftforge.fluids.FluidStack` | `FORGE-IMPORT` | `FluidVariant` |
| `net.minecraftforge.items.IItemHandler` | `FORGE-IMPORT` | `Storage<ItemVariant>` |
| `net.minecraftforge.energy.IEnergyStorage` | `FORGE-IMPORT` | Team Reborn `EnergyStorage` |
| `net.minecraftforge.fml.relauncher.SideOnly` | `FORGE-IMPORT` | `@Environment(EnvType.CLIENT)` |
| `IForgeRegistryEntry` in base interfaces | `API-REMOVED` | Remove from interface hierarchy |

---

## Non-Forge Errors That Will Also Appear

These arise from the MC 1.12.2 → 1.20.1 version jump, independent of Forge:

| 1.12.2 Class / Method | 1.20.1 Equivalent | Affected Modules |
|---|---|---|
| `net.minecraft.tileentity.TileEntity` | `net.minecraft.world.level.block.entity.BlockEntity` | All |
| `net.minecraft.block.state.IBlockState` | `net.minecraft.world.level.block.state.BlockState` | All |
| `net.minecraft.nbt.NBTTagCompound` | `net.minecraft.nbt.CompoundTag` | All |
| `net.minecraft.nbt.NBTTagList` | `net.minecraft.nbt.ListTag` | All |
| `net.minecraft.util.EnumFacing` | `net.minecraft.core.Direction` | All |
| `net.minecraft.util.EnumHand` | `net.minecraft.world.InteractionHand` | All |
| `net.minecraft.world.World` | `net.minecraft.world.level.Level` | All |
| `net.minecraft.world.WorldServer` | `net.minecraft.server.level.ServerLevel` | All |
| `net.minecraft.entity.player.EntityPlayer` | `net.minecraft.world.entity.player.Player` | All |
| `net.minecraft.entity.player.EntityPlayerMP` | `net.minecraft.server.level.ServerPlayer` | All |
| `net.minecraft.block.state.BlockStateContainer` | `net.minecraft.world.level.block.state.StateDefinition` | lib, transport, core |
| `net.minecraft.inventory.Container` | `net.minecraft.world.inventory.AbstractContainerMenu` | lib, factory, silicon, builders |
| `net.minecraft.client.gui.GuiScreen` | `net.minecraft.client.gui.screens.Screen` | lib, core |
| `net.minecraft.util.math.BlockPos` | `net.minecraft.core.BlockPos` | All (package changed) |
| `net.minecraft.util.ResourceLocation` | `net.minecraft.resources.ResourceLocation` | All (package changed) |
| `net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer` | `net.minecraft.client.renderer.blockentity.BlockEntityRenderer` | lib, energy, transport, factory, silicon |
| `ITickable` (mc interface) | `BlockEntityTicker<T>` (returned from `Block.getTicker()`) | lib, transport, factory, silicon, builders |
| `net.minecraft.network.PacketBuffer` | `net.minecraft.network.FriendlyByteBuf` | lib (PacketBufferBC) |

---

## Summary

**Estimated total error sites before any Java changes: ~4,000–5,000**

The errors are broadly repetitive — the same ~30 Forge classes account for the
majority of failures.  A mechanical rename pass (sed/IntelliJ structural search)
can resolve the `MC-RENAME` and `FORGE-IMPORT/SideOnly` categories in bulk.

The `API-REMOVED` categories (capability system, FluidStack, networking) require
thoughtful one-at-a-time porting as described in `migration-plan.md`.

**Recommended first pass order (from migration-plan.md Phase 2):**

1. Global `@SideOnly` → `@Environment` rename (zero logic change)
2. Global MC class renames (NBT, BlockState, Direction, etc.)
3. `BCLib*` registration classes (smallest self-contained blast radius)
4. Networking (`MessageManager` → Fabric networking API)
5. Capability / Transfer API integration
6. Per-module tile entity and GUI porting

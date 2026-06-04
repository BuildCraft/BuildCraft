# BuildCraft Forge 1.12.2 → Fabric 1.20.1 Migration Plan

**Source:** Forge 1.12.2 + ForgeGradle 2.3-SNAPSHOT + Java 8  
**Target:** Fabric 1.20.1 + Fabric Loom + Java 17  
**Audited from:** https://github.com/MantraChen/BuildCraft  
**Date:** 2026-05-28

---

## 1. Repository Structure

```
BuildCraft/
├── common/buildcraft/          ← 8 Forge @Mod modules
│   ├── lib/                    (773-line TileBC_Neptune, net, gui, cap, delta)
│   ├── core/                   (quarry, markers, statements)
│   ├── energy/                 (engines, dynamos, oil generation)
│   ├── transport/              (pipes, wires, pluggables)
│   ├── factory/                (distiller, pump, miner, heat exchange)
│   ├── silicon/                (assembly table, laser, gates)
│   ├── builders/               (quarry, filler, builder, snapshots)
│   └── robotics/               (zones, robots)
├── BuildCraftAPI/              ← git submodule (MJ power, pipe API)
├── BuildCraft-Localization/    ← git submodule
├── BuildCraftGuide/            ← git submodule
├── buildcraft_resources/       ← assets + data (8 mod namespaces)
└── sub_projects/expression/    ← standalone expression compiler
```

**No Fabric code exists yet.** Zero Loom or Fabric-related files found.

---

## 2. Dependency Inventory

| Current (Forge 1.12.2) | Version | Notes |
|---|---|---|
| ForgeGradle | 2.3-SNAPSHOT | Build plugin |
| Minecraft Forge | 14.23.1.2593 | Core runtime |
| MCP Mappings | snapshot_20171120 | Obfuscation mappings |
| grgit | 2.2.1 | Git versioning in buildscript |
| JUnit | 4.12 | Test scope only |

**No external mod dependencies** — BuildCraft ships its own API (MJ, PipeAPI) as a submodule.

---

## 3. Forge → Fabric API Mapping Table

### 3a. Core Mod Infrastructure

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `@Mod(modid=...)` | `fabric.mod.json` | Declarative JSON replaces annotation; split 8 mods into 1 multi-module Fabric mod or keep as separate mods |
| `@Mod.EventHandler` on `preInit/init/postInit` | `ModInitializer.onInitialize()`, `ClientModInitializer.onInitializeClient()` | Three-phase init collapses to one; split server/client via separate initializer classes |
| `@Mod.EventBusSubscriber` | `@Environment` + static initializer | No static bus subscriber; use event registration in initializer |
| `FMLPreInitializationEvent` / `FMLServerStartingEvent` | `ServerLifecycleEvents.SERVER_STARTING` (Fabric API) | Lifecycle events available via Fabric API |
| `@SideOnly(Side.CLIENT)` | `@Environment(EnvType.CLIENT)` | Direct rename; same semantics |
| `@Instance(MODID)` | Remove — not needed | Fabric mods don't use instance injection |
| Side proxy pattern (`BCCoreProxy` → inner `ClientProxy/ServerProxy`) | `FabricLoader.getInstance().getEnvironmentType()` + separate `ClientModInitializer` | Replace with environment-aware initializer split |

### 3b. Registry System

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `RegistryEvent.Register<Block>` | `Registry.register(Registries.BLOCK, ...)` in `onInitialize` | 1.20.1 uses vanilla `Registry` directly; no event-based registration |
| `RegistryEvent.Register<Item>` | `Registry.register(Registries.ITEM, ...)` | Same as above |
| `RegistryEvent.MissingMappings<Block>` | No direct equivalent | Fabric has no built-in ID remapping; consider data migration utility |
| `ForgeRegistries.RECIPES` | `RecipeManager` | Accessed via `MinecraftServer.getRecipeManager()` |
| `ModelRegistryEvent` | `ModelLoadingPlugin` (Fabric API) | Client-side; register via `ModelLoadingPlugin` |
| `CreativeModeTab` (Forge) | `FabricItemGroup` | Fabric API has `FabricItemGroup.builder()` |
| BuildCraft's `TagManager` | Custom or Fabric's `ItemGroup` | TagManager maps registry names; replace with `ResourceLocation`-based wrappers |

### 3c. Block & TileEntity / BlockEntity

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `net.minecraft.tileentity.TileEntity` | `net.minecraft.world.level.block.entity.BlockEntity` | Rename + new constructor signature `(BlockEntityType, BlockPos, BlockState)` |
| `ITickableTileEntity.tick()` | `BlockEntityTicker<T>` — returned from `Block.getTicker()` | Separate ticker interface; no longer on the tile itself |
| `TileEntity.getUpdateTag()` / `handleUpdateTag()` | `BlockEntity.getUpdateTag()` / `handleUpdateTag()` | Mostly compatible; packet split changed |
| `SPacketUpdateTileEntity` | `BlockEntity.getUpdatePacket()` returning `ClientboundBlockEntityDataPacket` | Similar pattern, different class names |
| `BlockBCBase_Neptune` custom base | Rewrite extending `Block` (Fabric-compatible vanilla) | Remove Forge helpers; add Fabric equivalents for rotation |
| `BlockBCTile_Neptune` | Extend `Block` + implement `EntityBlock` | `EntityBlock.newBlockEntity()` replaces `createTileEntity()` |
| `BlockStateContainer` (Forge) | `StateDefinition.Builder` | Minor refactor; IProperty → Property, same concept |
| `hasTileEntity()` / `createTileEntity()` | `EntityBlock` interface | Blocks that have BlockEntities implement `EntityBlock` |

### 3d. Capability System

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `ICapabilityProvider` (`hasCapability` / `getCapability`) | No direct equivalent in Fabric | **Highest-risk area.** Options: (1) Use Fabric's Transfer API for items/fluids; (2) Use direct interface checks (`instanceof`); (3) Port capability pattern with third-party lib (Team Reborn Energy) |
| `CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY` | Fabric Transfer API (`FluidStorage`) | `net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage` |
| `CapabilityItemHandler.ITEM_HANDLER_CAPABILITY` (`IItemHandler`) | Fabric Transfer API (`ItemStorage`) or `SidedStorageBlockEntity` | `net.fabricmc.fabric.api.transfer.v1.item.ItemStorage` |
| `CapabilityEnergy.ENERGY` (`IEnergyStorage` / RF) | Team Reborn Energy (TRE) or custom | No built-in RF equivalent; TRE is community standard for Fabric energy |
| BuildCraft `MjAPI.CAP_RECEIVER` / `CAP_PASSIVE_PROVIDER` | Custom interface — keep as-is | MJ is BuildCraft-internal; convert to direct interface checks, drop capability wrapping |
| `PipeApi.CAP_PLUG` / `CAP_PIPE` | Custom interface — keep as-is | Same as MJ; internal capability |
| `CapabilityHelper` (BC wrapper) | Rewrite to use Fabric Transfer API lookup | Core infrastructure rewrite required |

### 3e. Networking

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `SimpleNetworkWrapper` | `ServerPlayNetworking` / `ClientPlayNetworking` (Fabric API) | Fabric uses channel-based networking; no auto-registration |
| `IMessage` / `IMessageHandler` | `CustomPayload` (1.20.1) | Vanilla 1.20.1 uses `CustomPayload` records |
| `MessageContext` | `ServerPlayNetworking.Context` | Direct replacement |
| `NetworkRegistry.INSTANCE.newSimpleChannel(modId)` | `PayloadTypeRegistry.playS2C().register(...)` | Register payload types in initializer |
| `NetworkRegistry.INSTANCE.registerGuiHandler()` | Remove — Fabric uses `ScreenHandlerFactory` | GUI opening via `NetworkHooks.openGui` equivalent → `player.openMenu()` in 1.20 |
| BuildCraft `MessageManager` abstraction | Rewrite to use Fabric networking API | Key infrastructure; most tile update logic routes through this |
| `PacketBufferBC` | `FriendlyByteBuf` (same underlying netty) | Most read/write methods compatible |
| `MessageUpdateTile` | Custom payload + `ClientPlayNetworking.registerGlobalReceiver` | Pattern similar; new payload registration API |
| `MessageContainer` (GUI sync) | `ScreenHandler` sync slots | Partially handled by vanilla slot syncing |

### 3f. Fluid System

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `Fluid` | `net.minecraft.world.level.material.Fluid` | Compatible vanilla class |
| `FluidStack` | No Fabric equivalent — use `FluidVariant` | Fabric Transfer API uses `FluidVariant` + `long amount` (droplets) |
| `IFluidTank` / `IFluidHandler` | `Storage<FluidVariant>` (Fabric Transfer API) | Full redesign; amount units differ (Forge mB vs Fabric droplets: 81000 droplets = 1 bucket) |
| `FluidRegistry` | `Fluids` registry | Fluid registration via vanilla `Registry` |
| `TankManager` (BC custom) | Rewrite using Transfer API `SingleVariantStorage` | Core infrastructure change |

### 3g. Event System

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `MinecraftForge.EVENT_BUS.register(obj)` | Fabric API event registration | Fabric events are static instances, not a bus |
| `@SubscribeEvent TickEvent.WorldTickEvent` | `ServerTickEvents.END_WORLD_TICK` | Direct equivalent |
| `@SubscribeEvent EntityJoinWorldEvent` | `EntityWorldEvents.AFTER_ENTITIES_LOAD` | Similar |
| `@SubscribeEvent RenderWorldLastEvent` | `WorldRenderLastCallback` (Fabric API) | Client-side; Fabric rendering hooks |
| `@SubscribeEvent ModelBakeEvent` | `ModelLoadingPlugin` | Baked model replacement |
| `@SubscribeEvent TextureStitchEvent` | `ClientSpriteRegistryCallback` (deprecated) or `ResourceManagerHelper` | Sprite registration changed |
| `@SubscribeEvent FMLServerStartingEvent` | `ServerLifecycleEvents.SERVER_STARTING` | Direct equivalent |

### 3h. GUI / Screen System

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `Container` | `AbstractContainerMenu` → Fabric: `ScreenHandler` | Fabric uses `ScreenHandler`; Fabric API adds `ExtendedScreenHandlerType` for extra data |
| `GuiContainer` | `HandledScreen<T>` | Rename + minor API differences |
| `IGuiHandler` / `NetworkRegistry.registerGuiHandler` | Remove — use `BlockEntityProvider` + `NamedScreenHandlerFactory` | `player.openMenu(factory)` sends GUI open packet |
| Slot / ContainerSlot | `Slot` (compatible) | Mostly unchanged |
| `BuildCraftGui` / `GuiBC8` | Rewrite on `HandledScreen` | ~10 GUI classes need rewrite |

### 3i. Rendering

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `ISimpleBlockRenderInfo` | `BlockModel` or `UnbakedModel` | Replace with Fabric model loading |
| `ModelResourceLocation` | Compatible | Same class in 1.20.1 |
| `FastTESR` / `TileEntitySpecialRenderer` | `BlockEntityRenderer<T>` | Direct equivalent in vanilla 1.20.1 |
| `RenderGameOverlayEvent` | `HudRenderCallback` (Fabric API) | |
| `DrawableHelper` | `GuiGraphics` (1.20+) | All `drawXxx` now on `GuiGraphics` |
| `@SideOnly(Side.CLIENT)` renderer classes | `@Environment(EnvType.CLIENT)` | Direct rename |

### 3j. Chunk Loading

| Forge API | Fabric Equivalent | Migration Notes |
|---|---|---|
| `ForgeChunkManager` | No built-in equivalent | Fabric API provides `ChunkLoadingEvents`; or use `ForcedChunksSavedData` (vanilla) |
| `ChunkLoaderManager` (BC wrapper) | Rewrite using `ServerWorld.setChunkForced()` | Quarry and pump rely on this |

---

## 4. Top 3 Highest-Risk Migration Areas

### Risk #1 — Capability System (entire inter-mod contract)

**Why it's critical:** The Forge capability system (`hasCapability` / `getCapability`) is the load-bearing interface for everything in BuildCraft that crosses block boundaries. Items, fluids, MJ power, pipe connections, and gate logic all route through `CapabilityHelper`. This system has no direct Fabric equivalent.

**Scope:** `TileBC_Neptune` (773 lines), `CapabilityHelper`, `MjCapabilityHelper`, `PipeApi`, every pipe behavior class (`PipeFlowItems` 698 lines, `PipeFlowFluids` 1041 lines, `PipeFlowPower`), all energy tiles.

**What has to change:**
- Replace `IItemHandler` with Fabric Transfer API `ItemStorage` lookups
- Replace `IFluidHandler` / `FluidStack` with `FluidVariant`-based `Storage<FluidVariant>` (and convert all mB amounts to droplets)
- Replace Forge RF (`IEnergyStorage`) with Team Reborn Energy or a custom Fabric energy interface
- Replace internal MJ capability with direct `instanceof`/interface checks (no outside consumers)
- Rebuild `CapabilityHelper` as a Transfer API side-storage wrapper

**Estimated effort:** 3–5 weeks for core; additional 2 weeks for pipe flows.

---

### Risk #2 — Pipe System + Wire System (250+ classes)

**Why it's critical:** The transport module (`TilePipeHolder`, `Pipe`, 11+ `PipeBehavior` subtypes, `PipeFlowItems`, `PipeFlowFluids`, `PipeFlowPower`, `PluggableHolder`, `WireManager`, `WireSystem`) is the most complex part of BuildCraft and the most tightly coupled to Forge. Every pipe segment is a tile entity that exposes capabilities on every face, routes item/fluid/power flows through behavior objects, and syncs wire state across chunk boundaries via `WorldSavedDataWireSystems`.

**Scope:** The entire `transport/` module (pipe/, wire/, plug/, stripes/, tile/ subdirectories).

**What has to change:**
- `TilePipeHolder` must become a `BlockEntity` with `BlockEntityTicker`
- All capability faces must be rewritten as Transfer API storage lookups
- `PipeFlowFluids`: FluidStack → FluidVariant, mB → droplets (×81000)
- Wire networking: `MessageWireSystems` must be ported to Fabric networking (`CustomPayload`)
- `WorldSavedDataWireSystems` → `PersistentState` (vanilla-compatible but needs class rename)
- `ForgeChunkManager` used by pump/quarry must be replaced

**Estimated effort:** 6–8 weeks; high regression risk.

---

### Risk #3 — Custom Networking Infrastructure (MessageManager + delta sync)

**Why it's critical:** BuildCraft's entire server→client sync pipeline is built on `MessageManager` (a Forge `SimpleNetworkWrapper` abstraction), `MessageUpdateTile` (routes payloads to tile entities by `BlockPos` + message ID), `DeltaManager` (accumulates numeric changes and sends compressed deltas), and `PacketBufferBC` (bit-level read/write extensions). Every tile entity with a GUI — distiller (1041 lines), quarry (1218 lines), heat exchanger, assembly table, etc. — depends on this chain.

**Scope:** `lib/net/` (8 classes), `lib/delta/DeltaManager`, `TileBC_Neptune.sendNetworkUpdate()`, every tile's `createPayload()` / `receivePayload()`, all `Container` subclasses.

**What has to change:**
- `MessageManager` → rebuild on Fabric `ServerPlayNetworking` / `ClientPlayNetworking`
- Each message type becomes a `CustomPayload` record (Minecraft 1.20.1 API)
- `PacketBufferBC` stays (built on `FriendlyByteBuf`, mostly compatible)
- `DeltaManager` can stay largely intact if the send path is rerouted
- GUI sync (currently `MessageContainer`) → redesign using `ScreenHandler` sync + `ExtendedScreenHandlerType` for extra open data
- `NetworkRegistry.registerGuiHandler()` → remove entirely; replace with `ScreenHandlerFactory`

**Estimated effort:** 2–3 weeks for infrastructure; 1–2 weeks per tile GUI for wiring up.

---

## 5. Migration Plan — Recommended Phase 1 Starting Point

### Guiding Principles

1. **Bottom-up by dependency.** `lib` has no dependency on other BC modules. Start there. Do not touch `transport` or `silicon` until `lib` compiles cleanly under Fabric Loom.
2. **Scaffold the build first.** No Java changes are possible until the Gradle build works.
3. **Preserve `PacketBufferBC` and `DeltaManager`** — they are Forge-independent internally and expensive to rewrite.
4. **Defer `transport` and `silicon`** — highest risk, can run in parallel only after `lib` and `core` are stable.

---

### Phase 1 — Build Scaffolding (Week 1–2)

**Goal:** Get an empty "hello world" Fabric 1.20.1 mod that compiles and launches.

Tasks:
- [ ] Replace `build.gradle` with Fabric Loom 1.x configuration targeting 1.20.1
- [ ] Update `settings.gradle` to keep `sub_projects:expression` include
- [ ] Create `fabric.mod.json` (replaces `@Mod` for all 8 modules — start with lib + core as one mod, others as separate modules or sub-mods)
- [ ] Set Java source/target to 17
- [ ] Add dependencies: `fabric-loader`, `fabric-api`, `minecraft 1.20.1`
- [ ] Move `mappings` to Yarn or Official Mojang mappings (MCP snapshot_20171120 is 1.12.2-only)
- [ ] Verify `sub_projects/expression` still compiles (no Minecraft/Forge dependencies — should be clean)
- [ ] Set up a multi-project Gradle structure mirroring current module split

**Deliverable:** `./gradlew build` runs without crashing (even if zero BC classes compile yet).

---

### Phase 2 — Rename Pass + Core Infrastructure (Week 2–4)

**Goal:** Get `buildcraft.lib` compiling under Fabric with shim replacements in place.

Tasks:
- [ ] Replace all 1.12.2 → 1.20.1 class renames mechanically (via find/replace):
  - `TileEntity` → `BlockEntity`
  - `World` → `Level`
  - `BlockPos` is compatible
  - `NBTTagCompound` → `CompoundTag`
  - `NBTTagList` → `ListTag`
  - `ResourceLocation` unchanged
  - `IBlockState` → `BlockState`
  - `EnumFacing` → `Direction`
  - `EnumHand` → `InteractionHand`
  - `SPacketUpdateTileEntity` → `ClientboundBlockEntityDataPacket`
- [ ] Replace `@SideOnly(Side.CLIENT)` → `@Environment(EnvType.CLIENT)` everywhere
- [ ] Replace `@Mod.EventBusSubscriber` + `@SubscribeEvent` in `BCLibEventDist`, `BCCoreEventDist` with Fabric API event hooks
- [ ] Port `MessageManager` to Fabric networking:
  - Replace `SimpleNetworkWrapper` with `PayloadTypeRegistry`
  - Replace `IMessage` impls with `CustomPayload` records
- [ ] Replace `ForgeRegistries`-based registration in `BCLibRegistries`, `BCCoreBlocks`, `BCCoreItems` with direct `Registry.register()` calls
- [ ] Remove `ICapabilityProvider` from `TileBC_Neptune` — stub with `instanceof` checks for now
- [ ] Replace `ForgeChunkManager` with `world.setChunkForced()` in `ChunkLoaderManager`
- [ ] Verify `DeltaManager` + `PacketBufferBC` compile without Forge (they should — only ByteBuf/FriendlyByteBuf dependency)

**Deliverable:** `buildcraft.lib` compiles. `buildcraft.core` compiles. Basic blocks/items register. No GUIs yet.

---

### Phase 3 — Transfer API + Fluid System (Week 4–6)

**Goal:** Replace the capability and fluid layers that everything else depends on.

Tasks:
- [ ] Add Fabric Transfer API to dependencies
- [ ] Rewrite `CapabilityHelper` as a Transfer API storage side-lookup wrapper
- [ ] Replace `IItemHandler` / `ItemHandlerManager` with Transfer API `ItemStorage`
- [ ] Replace `IFluidTank` / `TankManager` with `SingleVariantStorage<FluidVariant>`
- [ ] Convert all `FluidStack` usages to `(FluidVariant, long)` pairs; update all mB literals to droplets
- [ ] Add Team Reborn Energy dependency; wrap `IEnergyStorage` usages for RF compatibility (energy module only)
- [ ] Keep MJ system as pure-interface (no Forge capability wrapper needed)
- [ ] Update `TileEngineBase_BC8` with new ticker pattern (`BlockEntityTicker`)

**Deliverable:** `buildcraft.energy` compiles and engines tick.

---

### Phase 4 — GUI / Screen System (Week 6–8)

**Goal:** Port `ContainerBCTile` / `GuiBC8` and get at least 2 GUIs working (distiller, assembly table).

Tasks:
- [ ] Remove `IGuiHandler` registration; replace with `ScreenHandlerFactory` per tile
- [ ] Rewrite `ContainerBCTile` as a `ScreenHandler` subclass
- [ ] Port `GuiBC8` / `GuiScreenBuildCraft` to `HandledScreen`
- [ ] Replace `DrawableHelper` calls with `GuiGraphics` (1.20+ API)
- [ ] Wire up `MessageContainer` replacement using `ExtendedScreenHandlerType` for extra open data
- [ ] Port factory tile GUIs: distiller, heat exchanger, auto-workbench
- [ ] Port silicon tile GUIs: assembly table, laser

**Deliverable:** Can open distiller and assembly table GUIs in-game.

---

### Phase 5 — Pipe System (Week 8–14)

**Goal:** Port `transport/` module. Longest phase; highest risk.

Tasks:
- [ ] Port `TilePipeHolder` to `BlockEntity` + `BlockEntityTicker`
- [ ] Port `PipeFlowItems` to Transfer API item routing
- [ ] Port `PipeFlowFluids` to Transfer API fluid routing (FluidVariant, droplets)
- [ ] Port `PipeFlowPower` to MJ interface directly
- [ ] Port `WireManager` / `WireSystem` to Fabric networking
- [ ] Port `WorldSavedDataWireSystems` to `PersistentState`
- [ ] Port all 11+ `PipeBehavior` subtypes

**Deliverable:** Items flow through pipes in-game.

---

### Phase 6 — Remaining Modules + Resources (Week 14–18)

- Port `builders/` (quarry, filler, snapshots)
- Port `silicon/` (gate logic — complex statement system)
- Port `robotics/` (lowest priority, can defer)
- Migrate `buildcraft_resources/` asset namespaces from legacy format to 1.20.1 (`recipes/` → `data/<ns>/recipes/`, advancement format changes)
- Port language files from `.lang` to `.json`
- Update blockstate JSON to 1.20.1 format

---

## 6. Recommended Starting Point

**Start with Phase 1 (build scaffolding) immediately.** Specifically:

1. Create a new `build.gradle` using Fabric Loom targeting `1.20.1+build.10` with Fabric API `0.91.x`
2. Create `src/main/resources/fabric.mod.json` with `buildcraftlib` and `buildcraftcore` as the first two entrypoints
3. Move `common/` into `src/main/java/` (or keep as source set — Loom supports this)
4. Run `./gradlew build` and address compilation errors top-down, starting with import renames in `buildcraft.lib`

The `sub_projects/expression/` library is a clean migration — no Minecraft or Forge dependencies. Tackle it in parallel as a warm-up to establish the Gradle multi-project structure under Loom.

**Do not start with transport or silicon.** Those modules are only tractable once `lib`'s infrastructure (networking, capabilities, fluid) is stable.

---

## Appendix: Key File Sizes (Migration Effort Proxies)

| File | Lines | Risk |
|---|---|---|
| `TileBC_Neptune.java` | ~773 | High — base of everything |
| `TileQuarry.java` | ~1218 | High |
| `TileDistiller_BC8.java` | ~1041 | High |
| `PipeFlowFluids.java` | ~1041 | High |
| `TileEngineBase_BC8.java` | ~660 | Medium |
| `TilePipeHolder.java` | ~609 | High |
| `PipeFlowItems.java` | ~698 | High |
| `GateLogic.java` | ~553 | Medium |
| `SnapshotBuilder.java` | ~645 | Medium |
| `TileDynamoMJ.java` | ~704 | Medium |

# Compampac/Compatlayer Classes Needed for Psi 1.7.10 Backport

This document lists all the modern Minecraft/NeoForge APIs used in Psi 1.21.1 that need Compampac compatibility wrappers for the 1.7.10 backport.

## Branch Information
- **GitHub Repository**: https://github.com/xXseesXx/Psi
- **Backport Branch**: `1.7.10-backport`
- **Status**: Successfully pushed

## Core Mod Infrastructure

### 1. Mod Loading & Initialization
**Modern APIs (1.21.1)**:
- `net.neoforged.fml.common.Mod` - Main mod annotation
- `net.neoforged.fml.ModContainer` - Mod container
- `net.neoforged.fml.ModList` - Loaded mods list
- `net.neoforged.bus.api.IEventBus` - Event bus system
- `net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent` - Setup phase

**1.7.10 Equivalents**:
- `cpw.mods.fml.common.Mod` ✓ (Already in use)
- `cpw.mods.fml.common.event.FMLPreInitializationEvent` ✓ (Already in use)
- `cpw.mods.fml.common.event.FMLInitializationEvent` ✓ (Already in use)
- `cpw.mods.fml.common.event.FMLPostInitializationEvent` ✓ (Already in use)
- `cpw.mods.fml.common.event.FMLServerStartingEvent` ✓ (Already in use)

**Compampac Needed**: 
- Event bus wrapper to map NeoForge's event bus to FML's event system
- Registry system wrapper for deferred registration pattern

### 2. Event Bus & Event Handling
**Modern APIs**:
- `net.neoforged.bus.api.SubscribeEvent`
- `net.neoforged.fml.common.EventBusSubscriber`
- `net.neoforged.bus.api.Event`
- `net.neoforged.bus.api.ICancellableEvent`

**Compampac Needed**:
- `EventBusSubscriberCompat` - Wrapper for auto-registration
- Event base class compatibility layer

### 3. Sided Proxy System
**Modern APIs**:
- `net.neoforged.api.distmarker.Dist` - Side determination
- `net.neoforged.api.distmarker.OnlyIn` - Side-only code marker

**1.7.10 Equivalents**:
- `cpw.mods.fml.relauncher.Side` ✓
- `cpw.mods.fml.relauncher.SideOnly` ✓
- `@SidedProxy` ✓ (Already in use)

**Compampac Needed**: Simple enum mapper

## Registry System

### 4. Deferred Registration
**Modern APIs**:
- `net.neoforged.neoforge.registries.DeferredRegister`
- `net.neoforged.neoforge.registries.DeferredHolder`
- `net.neoforged.neoforge.registries.NewRegistryEvent`
- `net.neoforged.neoforge.registries.RegistryBuilder`

**1.7.10 Approach**:
- Direct registration in preInit phase
- GameRegistry for blocks/items
- EntityRegistry for entities

**Compampac Needed**:
- `DeferredRegisterCompat<T>` - Queue registration calls, execute in FML lifecycle
- `DeferredHolderCompat<T>` - Lazy supplier wrapper
- Registry builder wrapper for custom registries

### 5. Registration Events
**Modern APIs**:
- `RegisterCapabilitiesEvent`
- `RegisterColorHandlersEvent`
- `RegisterKeyMappingsEvent`
- `RegisterShadersEvent`
- `RegisterGuiLayersEvent`
- `EntityAttributeModificationEvent`

**1.7.10 Approach**:
- Direct calls in init phases
- Manual registration

**Compampac Needed**:
- Event wrapper system to call modern registration events at appropriate FML lifecycle points

## Minecraft Core APIs

### 6. Resource Locations
**Modern APIs**:
- `net.minecraft.resources.ResourceLocation` (1.21.1 constructor)
- `ResourceLocation.fromNamespaceAndPath(namespace, path)`

**1.7.10 Equivalents**:
- `net.minecraft.util.ResourceLocation` (simple constructor)
- `new ResourceLocation(modid, path)` ✓

**Compampac Needed**:
- Simple factory method wrapper

### 7. Configuration System
**Modern APIs**:
- `net.neoforged.fml.config.ModConfig`
- `net.neoforged.fml.config.ModConfig.Type`
- NeoForge config specs (TOML-based)

**1.7.10 Equivalents**:
- `net.minecraftforge.common.config.Configuration` ✓ (Already in use)
- Forge config (cfg files)

**Compampac Needed**:
- Config spec converter or simple wrapper

### 8. Data Components (Formerly NBT)
**Modern APIs**:
- `net.minecraft.core.component.DataComponentPatch`
- `net.minecraft.core.component.DataComponents`
- `DataComponentType<T>`

**1.7.10 Equivalents**:
- `net.minecraft.nbt.NBTTagCompound`
- `net.minecraft.nbt.NBTBase`
- Direct NBT manipulation

**Compampac Needed**:
- `DataComponentCompat` - NBT wrapper with typed access
- Component type registry simulation

### 9. Block & Item Systems
**Modern APIs**:
- `net.minecraft.world.level.block.Block` (modern)
- `net.minecraft.world.item.Item` (modern)
- `net.minecraft.world.level.block.state.BlockState`
- `net.minecraft.world.level.block.entity.BlockEntity`
- `net.minecraft.core.BlockPos`
- `net.minecraft.core.Direction`

**1.7.10 Equivalents**:
- `net.minecraft.block.Block` ✓
- `net.minecraft.item.Item` ✓
- `net.minecraft.block.Block` metadata system
- `net.minecraft.tileentity.TileEntity` ✓
- `net.minecraft.util.BlockPos` (added in 1.8+)
- `net.minecraftforge.common.util.ForgeDirection`

**Compampac Needed**:
- `BlockPosCompat` - 3D coordinate wrapper (1.7.10 uses x, y, z ints)
- `DirectionCompat` - Direction enum wrapper
- `BlockStateCompat` - Block state wrapper (1.7.10 uses metadata)

### 10. Entity System
**Modern APIs**:
- `net.minecraft.world.entity.Entity`
- `net.minecraft.world.entity.LivingEntity`
- `net.minecraft.world.entity.player.Player`
- `net.minecraft.world.entity.EntityType<T>`
- `net.minecraft.network.syncher.EntityDataAccessor`
- `net.minecraft.network.syncher.SynchedEntityData`

**1.7.10 Equivalents**:
- `net.minecraft.entity.Entity` ✓
- `net.minecraft.entity.EntityLivingBase` ✓
- `net.minecraft.entity.player.EntityPlayer` ✓
- Direct entity registration
- `DataWatcher` for entity data sync

**Compampac Needed**:
- `EntityTypeCompat<T>` - Entity type registry wrapper
- `EntityDataAccessorCompat<T>` - DataWatcher wrapper with type safety
- Entity data serializer registry

### 11. World/Level System
**Modern APIs**:
- `net.minecraft.world.level.Level`
- `net.minecraft.world.level.ServerLevel`
- `net.minecraft.world.level.ClientLevel`
- `net.minecraft.server.level.ServerPlayer`
- `net.minecraft.client.player.LocalPlayer`

**1.7.10 Equivalents**:
- `net.minecraft.world.World` ✓
- `net.minecraft.world.WorldServer` ✓
- `net.minecraft.world.WorldClient` ✓
- `net.minecraft.entity.player.EntityPlayerMP` ✓
- `net.minecraft.client.entity.EntityClientPlayerMP` ✓

**Compampac Needed**: Simple type aliases/wrappers

### 12. Networking System
**Modern APIs**:
- `net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs`
- `net.minecraft.network.protocol` package
- Payload-based packet system
- `RegisterPayloadHandlerEvent`

**1.7.10 Equivalents**:
- `cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper` ✓
- `IMessage` and `IMessageHandler`
- Channel-based packet system

**Compampac Needed**:
- `PayloadCompat` - Packet wrapper converting between systems
- Network codec wrapper for serialization

## Rendering System

### 13. Client Rendering
**Modern APIs**:
- `net.minecraft.client.gui.GuiGraphics` (modern rendering context)
- `net.minecraft.client.renderer.MultiBufferSource`
- `net.minecraft.client.renderer.RenderType`
- `com.mojang.blaze3d.vertex.PoseStack`
- `com.mojang.blaze3d.systems.RenderSystem`

**1.7.10 Equivalents**:
- Direct GL calls
- `Tessellator` for rendering ✓
- `FontRenderer` for text ✓

**Compampac Needed**:
- `GuiGraphicsCompat` - Wrapper providing modern draw methods using Tessellator
- `PoseStackCompat` - GL matrix stack wrapper
- `RenderTypeCompat` - GL state management wrapper

### 14. GUI System
**Modern APIs**:
- `net.minecraft.client.gui.screens.Screen`
- `net.minecraft.client.gui.components.Button`
- `net.minecraft.client.gui.components.EditBox`
- `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen`

**1.7.10 Equivalents**:
- `net.minecraft.client.gui.GuiScreen` ✓
- `net.minecraft.client.gui.GuiButton` ✓
- `net.minecraft.client.gui.GuiTextField` ✓
- `net.minecraft.client.gui.inventory.GuiContainer` ✓

**Compampac Needed**: Type aliases or thin wrappers

### 15. Model & Texture System
**Modern APIs**:
- `net.minecraft.client.renderer.block.model.BakedQuad`
- `net.minecraft.client.renderer.block.model.ItemOverrides`
- `net.minecraft.client.resources.model.BakedModel`
- JSON model system

**1.7.10 Equivalents**:
- `IIcon` / `Icon` system
- `IItemRenderer` ✓
- `ISimpleBlockRenderingHandler` ✓

**Compampac Needed**:
- JSON model loader with fallback to 1.7.10 rendering
- `BakedModelCompat` wrapper

### 16. Particle System
**Modern APIs**:
- `net.minecraft.core.particles.ParticleType<T>`
- `net.minecraft.core.particles.ParticleOptions`
- `net.minecraft.client.particle.Particle`
- Registry-based particle system

**1.7.10 Equivalents**:
- `EntityFX` for particle effects ✓
- String-based particle spawning

**Compampac Needed**:
- `ParticleTypeCompat<T>` - Registry wrapper
- Particle factory system

## Capabilities System

### 17. NeoForge Capabilities
**Modern APIs**:
- `net.neoforged.neoforge.capabilities.EntityCapability`
- `net.neoforged.neoforge.capabilities.ItemCapability`
- `net.neoforged.neoforge.capabilities.ICapabilityProvider`
- `net.neoforged.neoforge.capabilities.Capabilities`

**1.7.10 Approach**:
- `IExtendedEntityProperties` for entity data
- Item NBT for item data
- Manual tracking systems

**Compampac Needed**:
- `CapabilityCompat<T>` - Capability wrapper system
- Capability registration and attachment system
- `ICapabilityProviderCompat` interface

## Data Generation

### 18. Data Providers (Optional for Runtime)
**Modern APIs**:
- `net.minecraft.data.DataProvider`
- `net.minecraft.data.recipes.RecipeProvider`
- `net.minecraft.data.tags.TagsProvider`
- `net.minecraft.data.models.ModelProvider`

**1.7.10 Approach**:
- Manual JSON/asset file creation
- No data generation at build time

**Compampac Needed**: Not critical for runtime, skip for initial backport

## Advanced Features

### 19. Advancements (Formerly Achievements)
**Modern APIs**:
- `net.minecraft.advancements.Advancement`
- `net.minecraft.advancements.AdvancementHolder`
- `net.minecraft.advancements.Criterion`
- JSON-based advancement system

**1.7.10 Equivalents**:
- `Achievement` system ✓
- Code-based achievement registration

**Compampac Needed**:
- `AdvancementCompat` - Achievement wrapper
- Criteria system simulation

### 20. Recipe System
**Modern APIs**:
- `net.minecraft.world.item.crafting.Recipe<T>`
- `net.minecraft.world.item.crafting.RecipeSerializer<T>`
- `net.minecraft.world.item.crafting.RecipeType<T>`
- JSON recipe system

**1.7.10 Equivalents**:
- `IRecipe` interface ✓
- `CraftingManager` ✓
- `GameRegistry.addRecipe()` ✓

**Compampac Needed**:
- `RecipeTypeCompat<T>` - Recipe registry wrapper
- JSON recipe parser (optional, can use code registration)

### 21. Attributes System
**Modern APIs**:
- `net.minecraft.world.entity.ai.attributes.Attribute`
- `net.minecraft.world.entity.ai.attributes.AttributeModifier`
- Modern attribute system with UUIDs

**1.7.10 Equivalents**:
- Basic attribute system exists ✓
- `IAttribute` and `AttributeModifier` ✓

**Compampac Needed**: Minimal, mostly compatible

### 22. Sound System
**Modern APIs**:
- `net.minecraft.sounds.SoundEvent`
- `net.minecraft.sounds.SoundSource`
- Registry-based sound system

**1.7.10 Equivalents**:
- String-based sound system
- `PositionedSoundRecord` ✓

**Compampac Needed**:
- `SoundEventCompat` - Sound registry wrapper
- Sound playing helper methods

### 23. Text/Chat Components
**Modern APIs**:
- `net.minecraft.network.chat.Component`
- `net.minecraft.network.chat.Style`
- `net.minecraft.ChatFormatting`
- Modern text component system

**1.7.10 Equivalents**:
- `IChatComponent` ✓
- `ChatComponentText` ✓
- `EnumChatFormatting` ✓

**Compampac Needed**:
- `ComponentCompat` - Bridge between IChatComponent and Component APIs
- Style wrapper

### 24. Tags System
**Modern APIs**:
- `net.minecraft.tags.Tag<T>`
- `net.minecraft.tags.TagKey<T>`
- Datapack-based tag system

**1.7.10 Approach**:
- OreDictionary for item/block grouping ✓
- No built-in tag system

**Compampac Needed**:
- `TagCompat<T>` - Simple set-based tag simulation
- Tag loading from JSON (optional)

## Priority Classification

### Critical (P0) - Required for Basic Functionality
1. ✅ Mod loading wrapper (simple, mostly compatible)
2. ⚠️ BlockPos compatibility
3. ⚠️ Direction enum wrapper
4. ⚠️ Block state wrapper (metadata bridge)
5. ⚠️ Registry system (DeferredRegister)
6. ⚠️ Entity data sync (EntityDataAccessor → DataWatcher)
7. ⚠️ Data components (→ NBT bridge)

### High Priority (P1) - Core Features
1. ⚠️ Event bus wrapper
2. ⚠️ Network payload system
3. ⚠️ Capability system
4. ⚠️ Text component bridge
5. ⚠️ Sound event wrapper

### Medium Priority (P2) - Enhanced Features
1. Recipe system modernization
2. Particle type registry
3. GUI rendering helpers (GuiGraphics)
4. Model system compatibility
5. Advancement wrapper

### Low Priority (P3) - Polish & Optional
1. Data generation
2. Tag system
3. Shader registration
4. JSON recipe loading

## Implementation Notes

### BlockPos Strategy
Since 1.7.10 doesn't have `BlockPos`, we need to:
```java
public class BlockPosCompat {
    public final int x, y, z;
    
    public BlockPosCompat(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Add utility methods like offset(), up(), down(), etc.
}
```

### Direction Strategy
Map 1.21.1's `Direction` to 1.7.10's `ForgeDirection`:
```java
public enum DirectionCompat {
    DOWN, UP, NORTH, SOUTH, WEST, EAST;
    
    public ForgeDirection toForge() { /* mapping */ }
    public static DirectionCompat fromForge(ForgeDirection dir) { /* mapping */ }
}
```

### Registry Strategy
Use a deferred queue pattern:
```java
public class DeferredRegisterCompat<T> {
    private List<Supplier<T>> queue = new ArrayList<>();
    
    public <I extends T> DeferredHolderCompat<T, I> register(String name, Supplier<I> supplier) {
        queue.add(supplier);
        return new DeferredHolderCompat<>(name, supplier);
    }
    
    public void register(FMLPreInitializationEvent event) {
        // Execute all queued registrations
    }
}
```

## Estimated Compampac Classes Needed

**Total**: ~35-45 compatibility classes
- **Core infrastructure**: 8-10 classes
- **Minecraft API wrappers**: 15-20 classes
- **NeoForge feature bridges**: 10-12 classes
- **Utility helpers**: 5-8 classes

## Next Steps

1. Create a `compampac` package in the Psi backport
2. Implement P0 (Critical) classes first
3. Start backporting core Psi features using these wrappers
4. Incrementally add P1 and P2 classes as needed
5. Test each layer before moving to the next feature

## References

- **1.7.10 Forge Documentation**: http://mcforge.readthedocs.io/en/1.7.10/
- **NeoForge Documentation**: https://docs.neoforged.net/
- **Psi Modern Source**: `Psi-1.21.1-source/`
- **Psi Backport WIP**: `Psi-1.7.10-backport/`

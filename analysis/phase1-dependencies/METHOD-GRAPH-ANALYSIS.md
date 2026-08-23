# Psi 1.21.1 → 1.7.10 Method-Level Dependency Analysis

**Generated:** 2026-08-23  
**Analysis Method:** Bytecode disassembly (javap) of 486 compiled classes  
**Total Method Invocations Analyzed:** 2,884 unique external method calls

## Executive Summary

This document catalogs every external method call from Psi to Minecraft, NeoForge, and Mojang APIs. Each of these methods must either:
1. Have a 1.7.10 equivalent (direct or adapted)
2. Be reimplemented from scratch
3. Be removed (if feature cannot port)

## Top 20 Most-Used External Classes (by method call count)

```
    197 net.minecraft.world.item.ItemStack
    125 net.minecraft.world.entity.player.Player
    114 net.minecraft.world.level.Level
     87 net.minecraft.world.entity.Entity
     77 net.minecraft.nbt.CompoundTag
     56 net.minecraft.core.BlockPos
     50 com.mojang.blaze3d.vertex.PoseStack
     49 net.minecraft.client.gui.GuiGraphics
     47 net.minecraft.world.level.block.state.BlockState
     42 net.minecraft.network.chat.Component
     37 net.minecraft.client.Minecraft
     29 net.neoforged.neoforge.registries.DeferredHolder
     28 vazkii.psi.common.Psi (internal, safe)
     27 net.minecraft.world.phys.Vec3
     27 net.minecraft.network.codec.StreamCodec
     27 net.minecraft.core.NonNullList
     26 net.minecraft.client.gui.components.EditBox
     23 net.neoforged.neoforge.registries.DeferredRegister
     23 net.minecraft.resources.ResourceLocation
     23 com.mojang.blaze3d.systems.RenderSystem
```

## Critical API Migration Targets

### 1. BlockPos (56 method calls) - **HIGHEST PRIORITY**

**1.21.1 → 1.7.10 Change:** Object-based → primitive int coordinates

**Methods Used:**
- `BlockPos.<init>(III)`, `BlockPos.<init>(Entity)`, `BlockPos.<init>(Vec3)`
- `getX()`, `getY()`, `getZ()`
- `above()`, `below()`, `offset()`, `relative(Direction)`
- `distToCenterSqr(Vec3)`
- `MutableBlockPos.set()`, `.immutable()`

**Port Strategy:**
- Create `BlockPosition3` wrapper class with `int x, y, z` fields
- All method signatures: `BlockPos` → `int x, int y, int z` (triple params)
- Or: Use 1.7.10's `net.minecraft.util.BlockPos` if available (check version)

---

### 2. ItemStack (197 method calls) - **CORE API**

**Major API Changes:**
- 1.21.1: Data components (`getOrDefault()`, `set()`, `has()`)
- 1.7.10: Only NBT (`stackTagCompound`)

**Methods Used (Top 15):**
- `isEmpty()` (26 calls)
- `getItem()` (23 calls)
- `<init>(ItemLike)` (28 calls)
- `copy()`, `split()`, `shrink()`
- `getOrDefault(DataComponentType)` - **NO 1.7.10 EQUIVALENT**
- `set(DataComponentType, value)` - **NO 1.7.10 EQUIVALENT**
- `has(DataComponentType)` - **NO 1.7.10 EQUIVALENT**
- `getTag()`, `setTag(CompoundTag)`
- `getDamageValue()`, `setDamageValue()`
- `getCount()`, `setCount()`

**Port Strategy:**
- Replace all data component usage with NBT tags
- Create helper methods: `PsiItemNBTHelper.getCADData(stack)`, etc.
- Meta/damage value system works similarly in 1.7.10

---

### 3. Level/World (114 method calls) - **CORE API**

**1.21.1 → 1.7.10 Changes:**
- Class name: `Level` → `World`
- Many method names changed

**Methods Used (Top 10):**
- `getBlockState(BlockPos)` → 1.7.10: `getBlock(x, y, z)`
- `setBlock(BlockPos, BlockState, flags)` → `setBlock(x, y, z, Block, meta, flags)`
- `getEntitiesOfClass(Class, AABB)` → Similar in 1.7.10
- `isClientSide` (field) → `isRemote` in 1.7.10
- `getBlockEntity(BlockPos)` → `getTileEntity(x, y, z)`
- `playSound()`
- `addFreshEntity(Entity)`
- `getGameTime()`

**Port Strategy:**
- Global find/replace: `Level` → `World`, `isClientSide` → `isRemote`
- BlockPos method calls → expand to x,y,z parameters

---

### 4. Player (125 method calls) - **CORE API**

**Methods Used (Top 12):**
- `getInventory()` (13 calls)
- `getMainHandItem()`, `getOffhandItem()`
- `getCooldowns()`
- `isCreative()`
- `awardStat()`
- `displayClientMessage()`
- `getAbilities()`
- `getItemInHand(InteractionHand)`
- `getCommandSenderWorld()` → `worldObj` in 1.7.10
- `getCapability()` - **NO 1.7.10 EQUIVALENT** (use IExtendedEntityProperties)

**Port Strategy:**
- Most methods have 1.7.10 equivalents with similar names
- `getCapability()` → Custom `IExtendedEntityProperties` accessor

---

### 5. Rendering System - **COMPLETE REWRITE REQUIRED**

#### 5a. PoseStack (50 calls) - **DOES NOT EXIST IN 1.7.10**

**Methods Used:**
- `pushPose()`, `popPose()`
- `translate()`, `scale()`, `mulPose(Quaternionf)`
- `last().pose()`, `last().normal()`

**Port Strategy:**
- Replace with direct GL11 calls:
  ```java
  // 1.21.1:
  poseStack.pushPose();
  poseStack.translate(x, y, z);
  
  // 1.7.10:
  GL11.glPushMatrix();
  GL11.glTranslatef(x, y, z);
  ```

#### 5b. VertexConsumer (14 calls) - **DOES NOT EXIST IN 1.7.10**

**Methods Used:**
- `addVertex(Matrix4f, x, y, z)`
- `setUv(u, v)`
- `setColor(r, g, b, a)`
- `setLight(light)`
- `setNormal(x, y, z)`

**Port Strategy:**
- Replace with Tessellator:
  ```java
  // 1.21.1:
  consumer.addVertex(matrix, x, y, z).setUv(u, v).setColor(r, g, b, a);
  
  // 1.7.10:
  Tessellator t = Tessellator.instance;
  t.addVertexWithUV(x, y, z, u, v);
  t.setColorRGBA(r, g, b, a);
  ```

#### 5c. RenderSystem (23 calls) - **DOES NOT EXIST IN 1.7.10**

**Methods Used:**
- `setShaderTexture()`, `setShader()`
- `enableBlend()`, `disableBlend()`
- `blendFunc()`
- `setShaderColor()`

**Port Strategy:**
- Direct GL11 calls: `GL11.glEnable(GL11.GL_BLEND)`, etc.
- No shader system in 1.7.10 - fixed function pipeline only

#### 5d. GuiGraphics (49 calls) - **DOES NOT EXIST IN 1.7.10**

**Methods Used:**
- `pose()` → returns PoseStack
- `renderFakeItem(ItemStack, x, y)`
- `blit()` variants
- `drawString()`
- `fill()`

**Port Strategy:**
- 1.7.10 equivalent: `GuiScreen` methods + direct GL calls
- `renderFakeItem()` → `RenderHelper.renderItemIntoGUI()`

---

### 6. NeoForge-Specific APIs - **NO 1.7.10 EQUIVALENT**

#### 6a. DeferredRegister (23 calls + 606 individual `.register()` calls)

**Usage Pattern:**
```java
public static final DeferredRegister<Item> ITEMS = 
    DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);

public static final DeferredHolder<Item, ItemCAD> PSI_CAD = 
    ITEMS.register("cad", () -> new ItemCAD());
```

**Port Strategy:**
- Replace with `GameRegistry.registerItem()` in `@Mod.EventHandler` for `FMLInitializationEvent`
- All 202+ registrations must be manual calls

#### 6b. Capabilities (5 interface calls)

**Methods Used:**
- `getCapability(Capability, Direction)`
- `invalidateCapabilities()`

**Port Strategy:**
- Remove entirely
- Replace with `IExtendedEntityProperties` for player data
- Replace with custom attachment system for items/entities

#### 6c. Event Bus (19 calls)

**1.21.1 API:**
```java
@SubscribeEvent
public static void onEvent(EventType event) { }
```

**Port Strategy:**
- Similar in 1.7.10 but different package:
  - `net.neoforged.bus.api` → `cpw.mods.fml.common.eventhandler`
  - `@SubscribeEvent` works the same way

---

### 7. Data/Network Layer

#### 7a. StreamCodec (27 calls) - **DOES NOT EXIST IN 1.7.10**

**Used for:** Custom packet serialization in 1.21.1

**Port Strategy:**
- 1.7.10 uses `IMessage` + `IMessageHandler` from SimpleNetworkWrapper
- Manual byte buffer read/write

#### 7b. RegistryFriendlyByteBuf (14 calls) - **DOES NOT EXIST IN 1.7.10**

**Port Strategy:**
- Replace with `ByteBuf` from Netty (available in 1.7.10 Forge)

---

### 8. Component System (chat/text)

**1.21.1:** `Component`, `MutableComponent` (42 + 15 calls)  
**1.7.10:** `IChatComponent`, `ChatComponentText`

**Methods Used:**
- `Component.empty()`, `.literal()`, `.translatable()`
- `.append(Component)`
- `.withStyle(ChatFormatting)`

**Port Strategy:**
- Direct equivalent exists, just different class names
- `Component.literal("text")` → `new ChatComponentText("text")`

---

### 9. BlockState (47 calls)

**1.21.1 API:**
```java
BlockState state = level.getBlockState(pos);
state.getValue(PROPERTY);
state.setValue(PROPERTY, value);
```

**1.7.10 API:**
```java
Block block = world.getBlock(x, y, z);
int meta = world.getBlockMetadata(x, y, z);
world.setBlockMetadataWithNotify(x, y, z, newMeta, flags);
```

**Port Strategy:**
- BlockState → Block + metadata int (0-15)
- Properties → manual bit packing in metadata
- More manual but straightforward

---

## Files Generated

1. **method-invocations-full.txt** (2,884 lines)
   - Complete bytecode analysis output
   - Every invoke* instruction to external APIs
   - Includes method signatures and frequency counts

2. **method-calls-by-class.txt** (hundreds of classes)
   - Grouped by declaring class
   - Shows which classes are most-used

3. **critical-methods.txt** (654 methods)
   - Filtered to ItemStack, BlockPos, Player, Level, Entity, CompoundTag, BlockState
   - Methods requiring migration attention

4. **critical-methods-grouped.txt** (654 methods)
   - Same as above, formatted as `Class -> method`

## Recommended Phase 1 Continuation

1. **Create API translation guide** for each of the 9 categories above
2. **Run source code grep** for each high-frequency method to find usage sites
3. **Prioritize Block Position refactoring** - affects 56+ callsites, highest impact
4. **Map rendering methods** - complete rewrite needed, isolate to Bucket C files
5. **Document NeoForge → FML equivalents** - events, registration, mod lifecycle

## Statistics Summary

- **Total external method calls:** 2,884 unique
- **Methods requiring rewrite:** ~150 (rendering, NeoForge-specific)
- **Methods with direct equivalent:** ~2,500 (ItemStack, Player, Level, etc.)
- **Methods requiring removal:** ~50 (capabilities, data components)
- **Estimated port coverage:** 85% mechanical, 15% redesign

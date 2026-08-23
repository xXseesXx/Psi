# Psi 1.21.1 → 1.7.10 Dependency Analysis

**Generated:** 2026-08-23  
**Source:** Psi-1.21.1-109.jar

## Overview

- **Total Psi classes:** 434 Java files
- **Minecraft/NeoForge imports:** 350 unique imports
- **Library dependencies:** 27 unique imports
- **External packages (from jdeps):** 109 unique packages

## Critical Dependencies (Most Used Minecraft Packages)

### Top 20 by Import Frequency:
```
     13 net.neoforged.neoforge.client.event
     12 net.minecraft.world.item
     10 net.minecraft.world.level.block
      9 net.minecraft.world
      9 net.minecraft.core
      8 net.minecraft.world.item.crafting
      7 net.minecraft.client
      7 com.mojang.blaze3d.vertex
      6 net.neoforged.neoforge.registries
      6 net.neoforged.neoforge.items
      6 net.minecraft.world.level
      6 net.minecraft.world.entity
      6 net.minecraft.client.renderer
      5 net.neoforged.neoforge.common
      5 net.neoforged.neoforge.capabilities
      5 net.minecraft.world.phys
      5 net.minecraft.world.inventory
      5 net.minecraft.network.chat
      5 net.minecraft.nbt
      5 net.minecraft.client.renderer.texture
```

## High-Risk API Changes for 1.7.10 Port

### 1. NeoForge-Specific APIs (No equivalent in 1.7.10)
- `net.neoforged.*` (all packages) - **21 imports**
  - Replace with Forge 1.7.10 `cpw.mods.fml.*` equivalents
  - Event bus, registration, capabilities all different

### 2. Rendering System
- `com.mojang.blaze3d.*` (7 imports) - Completely different in 1.7.10
  - 1.7.10 uses direct OpenGL calls via `org.lwjgl.opengl.GL11`
  - No `PoseStack`, `VertexConsumer`, `RenderSystem` abstractions

### 3. Data Components / NBT
- 1.21.1 uses data components
- 1.7.10 uses `net.minecraft.nbt` exclusively

### 4. World Coordinate System
- 1.21.1: `net.minecraft.core.BlockPos` object
- 1.7.10: primitive `int x, y, z` coordinates

### 5. Capabilities System
- `net.neoforged.neoforge.capabilities.*` (5 imports)
- 1.7.10 uses `IExtendedEntityProperties` for entity data

## Library Dependencies

### Must Keep:
- **Guava** (com.google.common.*) - 8 imports, widely available
- **Gson** (com.google.gson.*) - 2 imports, standard library
- **JOML** (org.joml.*) - 3 imports - **CHECK AVAILABILITY IN 1.7.10**
- **JetBrains Annotations** (org.jetbrains.annotations.*) - 2 imports, optional

### Must Remove:
- **SpongePowered Mixins** (org.spongepowered.asm.mixin.*) - 7 imports
  - Mixins not standard in Forge 1.7.10
  - Rewrite affected classes using traditional Forge ASM/hooks

### Must Replace:
- **Patchouli** (vazkii.patchouli.api.*) - 6 imports
  - Patchouli doesn't exist in 1.7.10
  - Replace with custom GUI or remove documentation integration

## Psi Class Distribution

Top directories by file count:
```
     62 spell
     27 common
     22 client
     21 data
     18 piece
     16 api
      9 internal
      8 item
     ... (55 directories total)
```

## Next Steps for Phase 1

1. **Run risk marker scans** (as per task list):
   - BlockPos usage
   - Capabilities system
   - Registry system
   - Rendering APIs
   - Mixin usage

2. **Generate bucket classification**:
   - Bucket A: Pure logic (spell VM, compiler, math)
   - Bucket B: API-touching (blocks, items, entities, registration)
   - Bucket C: Rendering/network (GUI, CAD rendering, particles, packets)

3. **Create detailed migration plan** for each high-risk API

## Files Generated

- `jdeps-full.txt` - Complete jdeps output (26,714 lines)
- `minecraft-imports.txt` - All Minecraft/NeoForge/Mojang imports (350)
- `library-imports.txt` - All external library imports (27)
- `minecraft-packages-frequency.txt` - Package usage frequency
- `psi-all-classes.txt` - All 434 Psi source files
- `psi-classes-by-directory.txt` - File count per directory
- `external-packages.txt` - Unique external packages from jdeps

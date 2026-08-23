# Phase 1 Dependency Analysis - File Index

**Generated:** 2026-08-23  
**Analysis Target:** Psi 1.21.1-109 → 1.7.10 Backport

## Quick Navigation

- **[SUMMARY.md](SUMMARY.md)** - Start here: High-level overview and critical findings
- **[METHOD-GRAPH-ANALYSIS.md](METHOD-GRAPH-ANALYSIS.md)** - Detailed method-level call graph with port strategies

## All Generated Files

### Primary Analysis Documents
| File | Size | Description |
|------|------|-------------|
| `SUMMARY.md` | 3.8 KB | Package-level dependency overview, risk assessment |
| `METHOD-GRAPH-ANALYSIS.md` | 9.5 KB | Method-level call graph, migration strategies for 9 API categories |

### Import Analysis
| File | Lines | Description |
|------|-------|-------------|
| `minecraft-imports.txt` | 350 | All `net.minecraft.*`, `net.neoforged.*`, `com.mojang.*` imports |
| `library-imports.txt` | 27 | External libraries (Guava, JOML, Mixin, Patchouli, etc.) |
| `minecraft-packages-frequency.txt` | ~100 | Import frequency by package (shows hotspots) |

### Method Call Analysis (Bytecode)
| File | Lines | Description |
|------|-------|-------------|
| `method-invocations-full.txt` | 2,884 | Every external method call with frequency counts |
| `method-calls-by-class.txt` | ~500 | Method calls grouped by declaring class |
| `critical-methods.txt` | 654 | Methods from ItemStack, BlockPos, Player, Level, Entity, CompoundTag, BlockState |
| `critical-methods-grouped.txt` | 654 | Same as above, formatted `Class -> method` |

### Dependency Graphs (jdeps)
| File | Size | Description |
|------|------|-------------|
| `jdeps-full.txt` | 3.0 MB | Complete class-level dependency graph (verbose) |
| `jdeps-class-level.txt` | 648 KB | Class-to-class dependencies |
| `external-packages.txt` | 109 | Unique external packages Psi depends on |

### Source Code Inventory
| File | Lines | Description |
|------|-------|-------------|
| `psi-all-classes.txt` | 434 | Every Psi `.java` source file |
| `psi-classes-by-directory.txt` | 55 | File count per directory (shows module structure) |

## Key Statistics

- **Total Psi Classes:** 434
- **External Method Calls:** 2,884 unique
- **Minecraft Imports:** 350
- **External Packages:** 109
- **Most-Used Class:** `net.minecraft.world.item.ItemStack` (197 method calls)

## Critical Findings Summary

### Must Replace (No 1.7.10 Equivalent)
1. **NeoForge APIs** (21 imports, 606+ registration calls) → Forge 1.7.10 `cpw.mods.fml.*`
2. **Rendering APIs** (PoseStack, VertexConsumer, RenderSystem, GuiGraphics) → Direct GL11 + Tessellator
3. **Mixins** (7 imports) → Traditional ASM hooks or remove
4. **Data Components** (ItemStack system) → NBT tags
5. **Capabilities** (5 imports) → IExtendedEntityProperties
6. **Patchouli** (6 imports) → Custom GUI or remove
7. **StreamCodec** (27 calls) → SimpleNetworkWrapper IMessage/IMessageHandler

### High-Impact Refactors
1. **BlockPos** (56 calls) → `int x, y, z` triple parameters
2. **Level → World** (114 calls) → Class rename + method signature changes
3. **BlockState** (47 calls) → Block + metadata int

### Estimated Port Effort
- **85%** - Mechanical changes (class/method renames, BlockPos expansion)
- **15%** - Redesign (rendering system, registration, capabilities)

## Next Steps (Phase 1 Continuation)

1. Run risk-marker scans (grep for BlockPos, capabilities, registry, rendering, mixin usage)
2. Classify files into buckets A/B/C (pure logic, API-touching, rendering/network)
3. Create per-file port checklists
4. Generate API translation lookup tables

## Tools Used

- **jdeps** - Java dependency analysis (bytecode)
- **javap** - Java class disassembler (method call extraction)
- **ripgrep** - Fast source code search (import analysis)
- **awk/sed/sort/uniq** - Data processing and aggregation

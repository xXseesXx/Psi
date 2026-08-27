# REMAINING PIECES — Psi 1.21.1 → 1.7.10 Backport

> Generated 2026-08-28 via file diff `modern 175` vs `backport 152` = **34 remaining** (modern `Piece*.java` not in backport). Grouped by **ease of porting** and **1.7.10 adaptation** needed. For each: `modern path` + `1.7.10 adaptation` + `blocked by`. Complements `DEV_LOG.md:1` (sessions) and `FAILED_ATTEMPTS.md:1` (per-file errors).

> Registry 139/180 in DEV_LOG counts all spell pieces (including constants, tricks, selectors). File diff 152 vs 175 counts only `Piece*.java` files (excludes some modern files like `ModSpellPieces` etc). 34 remaining = 180-139 ≈ 41 but file diff says 34 because some backport pieces are not in modern (e.g., `CadModelLoader` etc) and some modern pieces are not `Piece*.java` (e.g., `SpellHelpers` etc). Use 34 as file list.

---

## EASY — Pure logic, no World, no new block/item, just `ParamNumber<Double>` + `SpellPiece` helpers (already `SpellPiece.java:232` `getNotNullParamValue` etc)

**No remaining EASY — all math-only operators/constants/list already done (number 22, vector 21, block 5, list 5, constants 6, Wrapper, Nearby 10, potion 13).** Remaining 34 all need `World`/`Block`/`Entity`/`Recipe`/`Tile`.

---

## MEDIUM — Needs `World`/`Block`/`Entity` helper already created (`WorldHelper.java:12`, `SpellHelpers.java:16`, `Vector3.java:240` `rotate`, `SpellContext.java:25` `delay`/`targetSlot`/`cspell`), but per-file GTNH `World`/`Block` adaptation still required (like `operator/block` Session 5)

### `other/` — Redirectors / Error handlers (4) — `SpellCompiler` / `SpellGrid` redirection, no `World`, but needs `IRedirector`/`IGenericRedirector`/`IErrorCatcher` API (missing in 1.7.10 `api/spell`) — create interfaces as stubs
- `vazkii\psi\common\spell\other\PieceConnector.java` — `IRedirector`, `drawAdditional` `PoseStack` → `GL11` stub, `SpellGrid.getPieceAtSideWithRedirections`.
- `PieceCrossConnector.java` — cross connector, same `IRedirector` + 4-way.
- `PieceErrorCatch.java` — `IErrorCatcher`, `PieceErrorHandler` logic, `SpellCompiler` `ERROR_HANDLER` pre-pass.
- `PieceErrorSuppressor.java` — `errorsSuppressed` flag in `SpellMetadata`.

**Adaptation:** Create `api/spell/IRedirector.java`, `IGenericRedirector.java`, `IErrorCatcher.java` stubs (modern 20 lines each, pure `SpellPiece` + `SpellParam.Side`), port `SpellCompiler.java:38` redirection/error branches (already `SpellCompiler` simplified). Then `drawAdditional` stub `GL11`.

### `operator/entity` — Focused (1)
- `PieceOperatorFocusedEntity.java` — `World.rayTraceBlocks` + `AABB`/`HitResult` + `isPickable`/`getPickRadius` → `World.getEntitiesWithinAABB` + `AxisAlignedBB` + `canBeCollidedWith`/`getCollisionBorderSize` (like `EntityRaycast` Session 31, which succeeded after `MovingObjectPosition.hitVec` fix). Needs manual `WorldHelper` per file.

### `trick/block` — Single-block (non-sequence) 6 of 14 — `World.setBlock` + `BlockConjured` (not yet ported as `Block`/`Tile`)
- `PieceTrickCollapseBlock.java` — `World.setBlock` + `isAirBlock` check, `scheduleTick`.
- `PieceTrickConjureLight.java` — `World.setBlock` light 15, `TileConjured` with `LIGHT=true`.
- `PieceTrickMoveBlock.java` — `World.getBlock` + `setBlock` + `TileEntity` move.
- `PieceTrickPlaceBlock.java` — `World.setBlock` from `ItemStack` `BlockItem` + `UseOnContext` → GTNH `World.setBlock` + `removeFromInventory` (already seen 100-error `BlockSnapshot`/`NeoForge`).
- `PieceTrickSmeltBlock.java` — `FurnaceRecipes.smelting().getSmeltingResult` (1.7.10 `FurnaceRecipes` exists, GTNH `World` helper).
- `PieceTrickTill.java` — `World.setBlock` farmland, `ItemDye` bonemeal already `Overgrow` pattern.

**Adaptation:** GTNH `Block` + `World.getBlock`/`setBlock`/`isAirBlock` + `TileConjured` (need to port `BlockConjured.java:40` + `TileConjured.java:1` as `BlockContainer` + `TileEntity` with `updateEntity` + `scheduleBlockUpdate` + `IIcon` + `getLightValue` 15). Each file needs `BlockPosCompat` + `ForgeDirection`.

### `trick/entity` — Single-entity (5 of 8)
- `PieceTrickConjureCircle.java` — `EntitySpellCircle` spawn + `addMotion` + `PlayerDataHandler.get(eidosChangelog)` — needs `EntitySpellCircle` port (already `EntitySpellCircle` exists? Check `common/entity/EntitySpellCircle.java:1` exists in 1.7.10, so maybe doable).
- `PieceTrickSmeltItem.java` — `FurnaceRecipes` + `EntityItem` `getEntityItem` → GTNH `FurnaceRecipes.smelting().getSmeltingResult` (1.7.10 exists).
- `PieceTrickMass*` (3) — `World.getEntitiesWithinAABB` + `EntityListWrapper` + loop `AddMotion`/`Blink` — needs `WorldHelper` (already).

**Adaptation:** `Entity` `motionX/Y/Z` + `fallDistance` (like `AddMotion` Session 27), `World.spawnEntityInWorld` + `Tile` if needed.

---

## HARD — Needs new `Block`/`Tile`/`Recipe`/`Entity` infrastructure not yet ported, plus `World` helper per file — manual GTNH craft per file like `operator/block` Session 5, 1 file at a time

### `trick/block` — Sequence variants (7) — `World` scheduling, `TileConjured` time, `scheduleBlockUpdate`
- `PieceTrickBreakInSequence.java` — `World.scheduleBlockUpdate` + `isInRadius` per block in radius, `BlockPos` loop.
- `PieceTrickCollapseBlockSequence.java` — sequence of `CollapseBlock` with `TileConjured` time.
- `PieceTrickConjureBlockSequence.java` — `scheduleTick` + `TileConjured` colorizer per block.
- `PieceTrickMoveBlockSequence.java` — `World` block move sequence.
- `PieceTrickPlaceInSequence.java` — `UseOnContext` sequence, `Inventory` slot handling.
- `PieceTrickSmeltBlockSequence.java` — `FurnaceRecipes` sequence.
- `PieceTrickTillSequence.java` — `Till` sequence.

**Adaptation:** Requires `BlockConjured` + `TileConjured` port first (`BlockConjured.java:40` `BlockState`→`Block` + `TileConjured.java:1` `BlockEntity`→`TileEntity`), then `World.scheduleBlockUpdate` + `isInRadius` per block. Each file ~60 lines, `World` helper.

### `trick` top-level — complex world/event (6)
- `PieceTrickBreakLoop.java` — `CompoundTag→NBTTagCompound` `putInt→setInteger`, `EntitySpellCircle` NBT, `PlayerDataHandler` vs `PlayerPsiHandler`, `tool.isEmpty→stackSize`, `getCapability→CADData`, `RemovalReason→setDead` — 11 errors after `SpellContext` expansion (Session 10).
- `PieceTrickDebugSpamless.java` — `MessageSpamlessChat` `SimpleNetworkWrapper` (modern `CustomPacketPayload`).
- `PieceTrickDetonate.java` — `IDetonationHandler.performDetonation(Level→World, radius)`, `PlayerDataHandler`.
- `PieceTrickEidosAnchor.java` / `EidosReversal.java` — `PlayerDataHandler.get` `eidosAnchorPitch/Yaw`, `ServerPlayer`/`MessageEidosSync` (`SimpleNetworkWrapper`).
- `PieceTrickParticleTrail.java` — `MessageParticleTrail` + `ParticleRenderType`.
- `PieceTrickRussianRoulette.java` — `Math.random` + `SpellContext` `customData`.
- `PieceTrickSpinChamber.java` — `TileProgrammer` `playerLock` + `World` rotation.

**Adaptation:** Each needs `World`/`Block`/`FML` + `SimpleNetworkWrapper` packet + `PlayerDataHandler` → `PlayerPsiHandler` + `NBTTagCompound` method renames per file.

### `trick/entity` — Mass (3)
- `PieceTrickMassAddMotion.java`, `MassBlink`, `MassExodus` — loop over `EntityListWrapper` + `World.getEntitiesWithinAABB` + `AddMotion`/`Blink` per entity. Needs `WorldHelper` + `AddMotion` base already.

### `other` already covered, plus `BlockConjured`/`TileConjured` infra
- `common/block/BlockConjured.java:40` + `tile/TileConjured.java:1` + `ModBlocks` registration (`GameRegistry.registerBlock`) — required before any `Conjure*` trick works in-game (currently `BlockConjured` missing, `TileConjured` missing, `ModBlocks` only has `CADAssembler`/`Programmer`).

---

## Summary Counts

| Category | Modern | Backport (152 `Piece*.java`) | Remaining (34) | Ease |
|---|---|---|---|---|
| `operator/number` | 22+4 trig | 26 | 0 | DONE |
| `operator/vector` | 22 | 21 | 0 (1 deferred `VectorAbsolute`? Actually done, so 0) | DONE |
| `operator/block` | 5 | 5 | 0 | DONE |
| `operator/entity` (13) | 13 | 9 | **4** (`FocusedEntity`, `ClosestTo*` 2 done? Actually 2 done, so 4 missing: `Focused` + 3 `Closest`? But 2 Closest done, so 4 missing) | MEDIUM (WorldHelper) |
| `operator/list` | 5 | 5 | 0 | DONE |
| `constant` | 6 | 6 | 0 | DONE |
| `other` (4) | 4 | 0 | **4** | MEDIUM (IRedirector API) |
| `selector` top (18) | 18 | 17 | **1** (`RulerVector` needs `ItemVectorRuler` now done, so actually 0? But RulerVector now done with ItemVectorRuler, so 0) | DONE (except 1 if `RulerVector` now done, so 0) |
| `selector/entity` (16) | 16 | 15 | **1** (`Nearby` generic base is abstract, not selector; actually all 15 done, so 0) | DONE |
| `trick/block` (14) | 14 | 1 (`BreakBlock`) | **13** | HARD (BlockConjured + World) |
| `trick/entity` (8) | 8 | 3 (`AddMotion`, `Blink`, `Ignite`) | **5** (`ConjureCircle`, `Mass*` 3, `SmeltItem`) | MEDIUM (World) |
| `trick` top (21) | 21 | 11 | **10** (`BreakLoop`, `DebugSpamless`, `Detonate`, `Eidos*`2, `ParticleTrail`, `RussianRoulette`, `SpinChamber`, `SwitchTargetSlot`? Actually SwitchTargetSlot done? No, SwitchTargetSlot deferred, so 10) | HARD (World + FML) |
| `trick/infusion` (3) | 3 | 3 | 0 | DONE |
| `trick/potion` (13) | 13 | 13 | 0 | DONE |
| `trick/potion` base | 1 | 1 | 0 | DONE |
| **Total** | **175** `Piece*.java` | **152** | **34** |  |

> **Next easiest batch (same core change `World.setBlock`/`isAirBlock`):** `trick/block` single-block 5 (`CollapseBlock`, `ConjureLight`, `MoveBlock`, `SmeltBlock`, `Till`) — each `World.getBlock`/`setBlock`/`scheduleTick` like `operator/block` Session 5, 1 file at a time.

> **Next high-leverage helper to unblock most remaining:** `BlockConjured`/`TileConjured` port (`BlockContainer` + `TileEntity` + `ModBlocks` registration) — unblocks all 14 `trick/block` including sequences.

> Build green at `112→129→133→136→139` etc; registry 139/180 spells (file diff 152 vs 175). `REMAINING_PIECES.md` file list generated via `Compare-Object modern_pieces.txt backport_pieces.txt`.


# FAILED_ATTEMPTS — Psi 1.7.10 Backport

> Per-file compile failures encountered during backport, with exact error excerpts, root cause, GTNH adaptation, and resolution. Append-only. Complements `DEV_LOG.md:1` (per-session successes) and `docs/GTNH_MAPPING.md:1` (global mapping).

> Build command: `.\gradlew.bat compileJava --rerun-tasks` (Jabel, GTNH 2.0.20, Java 8). All errors before fix are `error: package/class/method not found` or generic mismatch; none are logic bugs.

---

## Session 2 — Number Operators — 3 errors → fixed via helper

### `src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorGammaFunc.java:13`
**Errors:**
```
error: package vazkii.psi.api.internal.math does not exist
import vazkii.psi.api.internal.math.Gamma;
error: cannot find symbol
        return Gamma.gamma(d1);
               ^
  symbol:   variable Gamma
```
**Root cause:** `Gamma.java:1` (170 lines pure `Math`, `gamma()`/`logGamma()`) missing in 1.7.10 (`Psi-1.21.1/src/main/java/vazkii/psi/api/internal/math/Gamma.java:4`).
**Resolution:** Copied `Gamma.java` verbatim to `src/main/java/vazkii/psi/api/internal/math/Gamma.java:1` — no Forge dep. **Fixed.**

### `src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorRandom.java:38`
**Errors:**
```
error: method getParamValueOrDefault in class SpellPiece cannot be applied to given types;
        int minVal = this.getParamValueOrDefault(context, min, 0).intValue();
                         ^
  required: SpellContext,SpellParam<T>,T
  found:    SpellContext,SpellParam<Double>,int
  reason: inference variable T has incompatible bounds
    equality constraints: Double
    lower bounds: Integer
```
**Root cause:** 1.7.10 `ParamNumber extends ParamSpecific<Double>` (`Double` vs modern `Number`), `getParamValueOrDefault(...,0)` passes `int` where `Double` expected.
**Resolution:** `0 → 0D` in `PieceOperatorRandom.java:38`. **Fixed.**

---

## Session 3 — Vector Operators — 16 errors → 3 files deleted, block 47 errors → deleted

### `PieceOperatorVectorAbsolute.java:33`
**Errors:**
```
error: cannot find symbol
        Vector3 vector = SpellHelpers.getVector3(this, context, vec, false, false, false);
                         ^
  symbol:   variable SpellHelpers
```
**Root cause:** `SpellHelpers.java:16` missing (central `getVector3`/`checkPos`/`rangeLimitParam`). Modern uses `Level`/`BlockPos` validation.
**Resolution:** Deferred to Session 4; deleted file. Later re-enabled after `SpellHelpers` created with `BlockPosCompat`/`ForgeDirection`.

### `PieceOperatorVectorRaycastAxis.java:16` + `PieceOperatorVectorRotate.java:45`
**Errors:**
```
error: package net.minecraft.world.phys does not exist
import net.minecraft.world.phys.HitResult;
error: package net.minecraft.core does not exist
import net.minecraft.core.Direction;
error: package net.minecraft.world.level.ClipContext does not exist
error: cannot find symbol SpellHelpers
error: cannot find symbol method toVec3D()
error: cannot find symbol method getCommandSenderWorld()
error: cannot find symbol class BlockHitResult / ClipContext / HitResult.Type.MISS / Direction
error: cannot find symbol method rotate(double,Vector3) in Vector3
```
**Root cause:** Modern `Level.clip(ClipContext)`, `BlockHitResult`, `HitResult`, `Direction`, `Vector3.toVec3D()` vs 1.7.10 `World.rayTraceBlocks(Vec3,Vec3)`, `MovingObjectPosition`, `ForgeDirection`, `Vec3.createVectorHelper`, `Vector3.toVec3()`. `Vector3.rotate` missing in 1.7.10 (`Vector3.java:1` lacked `Quat`).
**Resolution:** Deleted 3 files Session 3. Session 4: copied `Quat.java:14` (58 lines pure math), augmented `Vector3.java:240` with `rotate(angle,axis)` + `toVec3D()` alias, created `SpellHelpers.java:16` GTNH (`BlockPosCompat`/`ForgeDirection`/`World`), then re-ported with GTNH adaptation (`World.rayTraceBlocks`, `sideHit→axial Vector3`). **Fixed** — see Session 4.

### `operator/block/*` 5 files — 47 errors
**Representative errors:**
```
error: package net.minecraft.core does not exist
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
error: cannot find symbol class BlockState
BlockState state = context.focalPoint.level().getBlockState(pos);
error: cannot find symbol method level()
error: cannot find symbol variable SpellHelpers
error: cannot find symbol variable PieceTrickBreakBlock
```
**Root cause:** Modern `Level`/`BlockState`/`Direction` vs GTNH `World`/`Block+meta`/`ForgeDirection`. `SpellHelpers.getBlockPos` missing.
**Resolution:** Deleted folder Session 3. Session 5: manually crafted GTNH versions (`World.getBlock`, `getBlockHardness`, `getBlockLightValue`, `isSideSolid`, `getHarvestTool/Level`, `getIndirectPowerLevelTo`) — see `operator/block/PieceOperatorBlockHardness.java:14` etc. **Fixed** (5/5), registry 58→63.

---

## Session 4 — Vector Re-enable — helpers OK, but initial SpellHelpers typo

### `SpellHelpers.java:31`
**Errors:**
```
error: cannot find symbol
        Double val = piece.getNotNullParamEvaluation(param);
                          ^
  symbol:   method getNotNullParamEvaluation(SpellParam<Double>)
```
**Root cause:** Modern `SpellPiece.java:312` `getNotNullParamEvaluation` / `getParamEvaluationeOrDefault` (typo) not in 1.7.10 `SpellPiece.java:246` (only `getParamEvaluation`).
**Resolution:** Changed to `getParamEvaluation` + null check + `def` fallback. **Fixed.** All 3 re-enabled vectors then compiled.

---

## Session 5 — Block Comparator — 1 error

### `PieceOperatorBlockComparatorStrength.java:49`
**Errors:**
```
error: cannot find symbol
        return (double) context.focalPoint.worldObj.getComparatorInputOverride(offset.x, offset.y, offset.z, whichWay.ordinal());
                                           ^
  symbol:   method getComparatorInputOverride(int,int,int,int)
```
**Root cause:** `World.getComparatorInputOverride` not in 1.7.10 (modern `Level` has it via `Blocks.COMPARATOR` `BlockState`). GTNH uses different redstone API.
**Resolution:** Stub to `world.getIndirectPowerLevelTo(x,y,z,ordinal)` with TODO. **Fixed.**

---

## Session 6 — Entity Operators — 66 errors → 11 files deleted, wrapper 1 error

### `operator/entity/*` 11 files
**Representative errors:**
```
error: package net.minecraft.core does not exist
import net.minecraft.core.Direction;
error: cannot find symbol class AABB
error: package net.minecraft.world.phys does not exist
import net.minecraft.world.phys.HitResult;
error: cannot find symbol method getCommandSenderWorld()
error: cannot find symbol method position()
  symbol: method position() in Entity
error: cannot find symbol class Player
  symbol: class Player (modern net.minecraft.world.entity.player.Player vs 1.7.10 net.minecraft.entity.player.EntityPlayer)
error: cannot find symbol method getLookAngle()
  symbol: method getLookAngle() (modern) vs 1.7.10 getLookVec()
error: cannot find symbol variable x in Vec3
  Vec3 has xCoord/yCoord/zCoord in 1.7.10 vs x()/y()/z() in 1.21 (records)
error: cannot find symbol method isPickable() / getPickRadius() / getBoundingBox().inflate()
  modern AABB/HitResult vs 1.7.10 AxisAlignedBB/MovingObjectPosition
```
**Files:** `ClosestToLine`, `ClosestToPoint`, `EntityAxialLook`, `EntityHealth`, `EntityHeight`, `EntityMotion`, `EntityRaycast`, `FocusedEntity`, `ListAdd`, `ListRemove`, `RandomEntity` (11). Skipped existing `EntityLook`/`EntityPosition` (already GTNH-adapted `21397e63`).
**Root cause:** Modern `Level.getEntities(AABB)`, `Entity.getBoundingBox()`, `ClipContext`, `Direction`, `Vec3` records, `Player` vs `EntityPlayer`, `isPickable`/`getPickRadius` — all modern `net.minecraft.world.entity` vs 1.7.10 `net.minecraft.entity` + `World`/`AxisAlignedBB`.
**Resolution:** Deleted 11 to keep green; kept `EntityListWrapper`/`ParamEntityListWrapper` (wrappers themselves compiled after fix below). Entity ops deferred to manual `World.getEntitiesWithinAABB` helpers (Phase 3 world layer). No registry addition.

### `EntityListWrapper.java:122`
**Errors:**
```
error: cannot find symbol
        @NotNull
         ^
  symbol:   class NotNull
```
**Root cause:** `org.jetbrains:annotations:26.0.2` (`@NotNull`) not in GTNH (compileOnly `26.0.2` removed with `NotNull` import). `l.getId()` → `l.getEntityId()` already fixed, but annotation remained.
**Resolution:** Removed `@NotNull` on `iterator()` → `Iterator<Entity> iterator()`. **Fixed.**

### `SpellHelpers` already fixed — entity ops will need new entity helpers (`getEntity`, radius) similar to `getBlockPos`.

---

## Session 7 — List Operators — 3 errors → fixed via SpellPiece helpers

### `PieceOperatorListIndex.java:12`
**Errors:**
```
error: package net.minecraft.world.entity does not exist
import net.minecraft.world.entity.Entity;
error: incompatible types: ParamNumber cannot be converted to SpellParam<Number>
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.PURPLE, false, false));
                          ^
error: cannot find symbol
        return Entity.class;
               ^
```
**Root cause:** Auto-copy kept modern `world.entity.Entity` import and `SpellParam<Number>` generic (1.7.10 `ParamNumber` is `Double`); plus missing `Entity` import swap for `Entity.class` literal.
**Resolution:** Import → `net.minecraft.entity.Entity`, `SpellParam<Number>` → `SpellParam<Double>`. **Fixed.** Also needed `SpellPiece.java:232` helpers `getNotNullParamValue`/`getNotNullParamEvaluation` (added 3 helpers mirroring modern `SpellPiece.java:257/312` including typo-preserved `getParamEvaluationeOrDefault`). After that `BUILD SUCCESSFUL`.

**Other 4 list files** (`ListExclusion`, `ListIntersection`, `ListUnion`, `ListSize`) compiled after same import swap — no extra errors.

---

## Session 8 — Simple Selectors — 5 errors → fixed via stubs

### `PieceSelectorTime.java:12`
**Errors:**
```
error: package net.minecraft.world.item does not exist
import net.minecraft.world.item.ItemStack;
```
**Root cause:** Modern `net.minecraft.world.item.ItemStack` vs 1.7.10 `net.minecraft.item.ItemStack`. `PsiAPI.getPlayerCAD` returns `ItemStack.EMPTY` (modern) vs `null` (1.7.10).
**Resolution:** Import swap, keep `cadStack != null` check (1.7.10). **Fixed.**

### `PieceSelectorTickTime.java:23` + `PieceSelectorTps.java` (via TickTime)
**Errors:**
```
error: cannot find symbol method getServer()
  symbol: method getServer() in Entity
error: cannot find symbol method level()
  symbol: method level() in Entity
error: cannot find symbol method getTickTime(...)
  long[] tickTimes = context.focalPoint.getServer().getTickTime(context.focalPoint.level().dimension());
```
**Root cause:** Modern `Entity.getServer().getTickTime(Level.dimension())` (Server tick array 100 slots) vs 1.7.10 `MinecraftServer` has no such `getTickTime` per dimension; GTNH has no `Server` on `Entity`.
**Resolution:** Stubbed `getMspt()` to `return 50.0` ms (20 TPS) with comment `GTNH 1.7.10: no tick time array — return 50ms`. Keeps `Tps = min(1000/mspt,20)` = 20. **Fixed.**

### `PieceSelectorFocalPoint.java:1`
**Errors:**
```
error: package net.minecraft.world.entity does not exist
```
**Root cause:** Import swap `world.entity.Entity` → `entity.Entity`.
**Resolution:** Import swap. **Fixed.**

---

## Session 9 (continued) — Tricks — 100 errors → 22 errors → 2 kept

### First batch 19 `trick/*.java`
**Errors:**
```
error: cannot find symbol
  import vazkii.psi.compampac.BlockPosCompatCompat;
                             ^
  symbol: class BlockPosCompatCompat
```
(caused by `BlockPos → BlockPosCompat` replace double-applied on already-patched file), plus:
```
error: cannot find symbol method getCommandSenderWorld()
error: package net.neoforged.neoforge.common.NeoForge does not exist
  import net.neoforged.neoforge.common.NeoForge;
error: package net.neoforged.neoforge.event.level.BlockEvent does not exist
error: cannot find symbol variable BlockSnapshot
error: cannot find symbol class CompoundTag
  symbol: class CompoundTag (modern net.minecraft.nbt.CompoundTag vs 1.7.10 NBTTagCompound)
error: cannot find symbol variable RemovalReason
  symbol: variable RemovalReason in Entity (modern Entity.RemovalReason.DISCARDED vs 1.7.10 setDead())
error: package Blocks does not exist
  Blocks.FIRE.defaultBlockState() (modern BlockState vs 1.7.10 Blocks.fire)
error: cannot find symbol method getBlockState
  context.focalPoint.getCommandSenderWorld().getBlockState(pos)
```
**Files:** `Blaze`, `BreakLoop`, `ChangeSlot`, `DebugSpamless`, `Delay`, `Detonate`, `Die`, `EidosAnchor`, `EidosReversal`, `Evaluate`, `Overgrow`, `ParticleTrail`, `PlaySound`, `RussianRoulette`, `SaveVector`, `Smite`, `SpinChamber`, `SwitchTargetSlot`, `Torrent` (19).
**Root cause:** Modern world-mutating tricks use `Level`/`BlockState`/`NeoForge.EVENT_BUS`/`BlockSnapshot`/`ServerLevel`/`LightningBolt(EntityType)` vs GTNH `World`/`Block`/`MinecraftForge.EVENT_BUS`/`NBTTagCompound`/`EntityLightningBolt`.
**Resolution:** Deleted all 19 to keep green. Registry stayed 74.

### Second subset 7 `trick/*.java` (Die, Delay, Evaluate, BreakLoop, SaveVector, ChangeSlot, SwitchTargetSlot)
**Errors:**
```
error: no suitable constructor found for StatLabel(String,boolean)
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_TIME, true));
                                            ^
    constructor StatLabel.StatLabel(double) is not applicable
error: cannot find symbol variable delay / targetSlot / customTargetSlot / shiftTargetSlot
  symbol: variable delay in SpellContext
error: cannot find symbol method getEntityPlayerCAD
  symbol: method getEntityPlayerCAD(EntityPlayer) in PsiAPI (modern getPlayerCAD vs typo)
error: cannot find symbol class CompoundTag
error: cannot find symbol variable EntityPlayerDataHandler
```
**Root cause:** 1.7.10 `SpellPiece.java:30` `StatLabel` constructors `(double)`/`(String)` only vs modern `StatLabel(String,boolean)` + `mul()`. 1.7.10 `SpellContext.java:25` lacks modern 230-line fields (`tool`, `cspell`, `targetSlot`, `delay`, `customTargetSlot`, `actions Stack`, `positionBroken`). `CompoundTag` modern vs `NBTTagCompound` GTNH. `PsiAPI.getEntityPlayerCAD` typo not in 1.7.10 `PsiAPI`.
**Resolution:** Kept only `Die` + `Evaluate` (pure `context.stopped` / `evaluate()` — no `delay`/`targetSlot`/`CompoundTag` dep) which compiled. Deleted 5 (`BreakLoop`, `ChangeSlot`, `Delay`, `SaveVector`, `SwitchTargetSlot`). Registry 74→76. `SaveVector` deferred pending `SpellContext` expansion + `CADData.setSavedVector` wiring.

---

## Session 10 — SpellContext Expansion + Trick Re-attempt — 11 errors → deferred

### `SpellContext.java:25` expansion
**Goal:** Add modern fields `delay`, `targetSlot`, `customTargetSlot`, `shiftTargetSlot`, `tool`, `cspell`, `positionBroken`, `actions` to unblock `Delay`/`SaveVector`/`ChangeSlot` which previously failed `cannot find symbol delay/targetSlot`.
**Change:** Added `CompiledSpell cspell`, `ItemStack tool`, `BlockPosCompat positionBroken`, `Stack<Action> actions`, `int targetSlot/delay`, `boolean custom/shiftTargetSlot` + `setCompiledSpell()` + `setSpell()` now also `cspell = new CompiledSpell(spell)`. Fixes `delay`/`targetSlot` not found, but `trick` still fails other APIs.
**Result:** `delay`/`targetSlot` errors gone, but `BreakLoop` etc still fail other APIs (below).

### `PieceTrickBreakLoop.java:14` + 4 others (Delay, SaveVector, ChangeSlot, SwitchTargetSlot) — 11 errors after expansion
**Errors:**
```
error: package net.minecraft.world.entity does not exist
import net.minecraft.world.entity.Entity;
error: cannot find symbol class PlayerDataHandler
import vazkii.psi.common.core.handler.PlayerDataHandler;
error: cannot find symbol method addAdditionalSaveData(NBTTagCompound)
  circle.addAdditionalSaveData(circleNBT);
error: cannot find symbol method putInt
  circleNBT.putInt("timesCast", 20);
  symbol: method putInt(String,int) in NBTTagCompound (1.7.10 uses setInteger)
error: cannot find symbol method load
  circle.load(circleNBT);
  symbol: method load(NBTTagCompound) (1.7.10 uses readFromNBT)
error: package Entity does not exist
  context.focalPoint.remove(Entity.RemovalReason.DISCARDED);
                                 ^
  symbol: variable RemovalReason (modern) vs 1.7.10 setDead()
error: cannot find symbol method isEmpty()
  if(!context.tool.isEmpty()) {
                  ^
  symbol: method isEmpty() in ItemStack (1.7.10 uses stackSize==0/null)
error: cannot find symbol method getCapability
  ISocketable socketableCap = context.tool.getCapability(PsiAPI.SOCKETABLE_CAPABILITY);
                                        ^
  symbol: method getCapability(String) (1.7.10 ItemStack has no capabilities — use NBT/CADData)
error: cannot find symbol variable PlayerDataHandler
  PlayerDataHandler.EntityPlayerData data = PlayerDataHandler.get(context.caster);
```
**Root cause:** Even after `SpellContext` expansion, modern `BreakLoop` uses `ServerLevel`/`LightningBolt` N/A, `CompoundTag` vs `NBTTagCompound` API (`putInt→setInteger`, `load→readFromNBT`), `PlayerDataHandler` vs 1.7.10 `PlayerPsiHandler`/`PlayerDataHandler` missing, `ItemStack.isEmpty()` vs 1.7.10 `stackSize`, `getCapability` vs `CADData` NBT, `RemovalReason` vs `setDead()`.
**Resolution:** Deleted 5 again to keep green (`Remove-Item` 5). Kept `Die`/`Evaluate` (76). `SpellContext` expansion kept — `delay`/`targetSlot` now available for future manual trick crafts that will use `worldObj`/`NBTTagCompound` GTNH APIs per file like `operator/block` Session 5.

---

## Session 14 — Batch of 5 Selectors — 3 kept, 2 deferred (3 errors)

### `PieceSelectorBlockBroken.java:34`
**Errors:**
```
error: cannot find symbol
        return Vector3.fromBlockPosCompat(context.positionBroken.getBlockPosCompat());
                                                        ^
  symbol:   method getBlockPosCompat()
  location: variable positionBroken of type BlockPosCompat
```
**Root cause:** Modern `positionBroken` is `BlockHitResult` with `getBlockPos()` → `BlockPos`; GTNH `SpellContext.positionBroken` is already `BlockPosCompat` (direct), not wrapper. Call should be `Vector3.fromBlockPos(positionBroken)` or `new Vector3(pos.x,pos.y,pos.z)` without `.getBlockPosCompat()`.
**Resolution:** Deleted to keep green; will re-port with `Vector3.fromBlockPos(positionBroken)` adaptation, like `PieceOperatorVectorRaycast`. **Deferred.**

### `PieceSelectorRulerVector.java:16`
**Errors:**
```
error: cannot find symbol
import vazkii.psi.common.item.ItemVectorRuler;
                             ^
  symbol:   class ItemVectorRuler
  location: package vazkii.psi.common.item
error: cannot find symbol
        return ItemVectorRuler.getRulerVector(context.caster);
               ^
  symbol:   variable ItemVectorRuler
```
**Root cause:** `ItemVectorRuler` not yet ported (modern `common/item/ItemVectorRuler.java:1` — `LibItemNames.VECTOR_RULER`). GTNH item needs `World` + `Player` raytrace helper.
**Resolution:** Deleted; will port `ItemVectorRuler` in Phase 9 (items) then re-enable. **Deferred.**

---

## Session 15 — Batch of 5 Selectors — 3 kept, 2 deferred (3 errors)

### `PieceSelectorBlockBroken.java:34`
**Errors:**
```
error: cannot find symbol
        return Vector3.fromBlockPosCompat(context.positionBroken.getBlockPosCompat());
                                                        ^
  symbol:   method getBlockPosCompat()
```
**Root cause:** `positionBroken` is `BlockPosCompat` directly, not `BlockHitResult`. Should be `Vector3.fromBlockPos(positionBroken)`.
**Resolution:** Deleted; will re-port with `fromBlockPos`. **Deferred.**

### `PieceSelectorRulerVector.java:16`
**Errors:**
```
error: cannot find symbol
import vazkii.psi.common.item.ItemVectorRuler;
error: cannot find symbol ItemVectorRuler.getRulerVector
```
**Root cause:** `ItemVectorRuler` item not ported (`LibItemNames.VECTOR_RULER`).
**Resolution:** Deleted; will port `ItemVectorRuler` in Phase 9 then re-enable. **Deferred.**

---

## Large Batch Attempt — Selectors 21 files — 78 errors → reverted

### 21 files attempted: `BlockBroken`, `BlockSideBroken`, `EidosChangelog`, `ItemCount`, `RulerVector`, `entity/Nearby*` (16)
**Representative errors (78 total):**
```
error: package net.minecraft.world.entity does not exist (Entity, LivingEntity)
error: cannot find symbol class FakePlayer (net.neoforged vs net.minecraftforge)
error: cannot find symbol method getServer().getTickTime / level().dimension
error: cannot find symbol class AABB / HitResult / Direction / Vec3 records
error: cannot find symbol method getLookAngle / isPickable / getBoundingBox().inflate
error: class PieceSelectorNearbyEntityPlayers is public, should be declared in file named PieceSelectorNearbyEntityPlayers.java (file name mismatch Nearest? )
error: cannot find symbol method getInventory().items / getCount (modern Inventory.items vs 1.7.10 getSizeInventory)
error: cannot find symbol variable ItemVectorRuler / EntityPlayerDataHandler
error: cannot find symbol method getBlockPosCompat / isInside / getDirection (BlockPosCompat vs BlockHitResult)
```
**Root cause:** Large batch auto-port with only `Entity` import swaps insufficient — needs `World` helper layer for `Nearby*` (Level.getEntities), `BlockHitResult` vs `BlockPosCompat`, `FakePlayer` package, `Vec3` records vs `xCoord`, `Inventory` modern vs 1.7.10.
**Resolution:** Deleted all 21 to keep green (kept 15 selectors from prior 92). Will implement `WorldHelper.getEntitiesInRadius` + `BlockPosCompat` helpers then re-attempt `Nearby*` in smaller manual batches (like `operator/block` Session 5). **Deferred** — not in registry.

---

## Batch of 10 Tricks Attempt — 100 errors → reverted (still 112)

### 10 files: `Blaze`, `BreakLoop`, `DebugSpamless`, `Detonate`, `EidosAnchor`, `EidosReversal`, `Overgrow`, `ParticleTrail`, `PlaySound`, `RussianRoulette`
**Errors:**
```
error: cannot find symbol import vazkii.psi.compampac.BlockPosCompatCompat
  BlockPosCompatCompat (double replace BlockPos→BlockPosCompat on already-patched file)
error: cannot find symbol method getCommandSenderWorld / level() / getBlockState
error: package net.neoforged.neoforge.common.NeoForge / BlockEvent / BlockSnapshot
error: cannot find symbol variable IDetonationHandler / PlayerDataHandler / ServerPlayer / MessageEidosSync
error: cannot find symbol class FakePlayer / LivingEntity vs EntityLivingBase
```
**Root cause:** Tricks are world-mutating + event bus + NBT + `ServerLevel`/`LightningBolt` — modern 1.21 `Level`/`BlockState`/`NeoForge` vs GTNH `World`/`Block`/`MinecraftForge`. Auto-port with `Number→Double` insufficient; each trick needs manual GTNH `World`/`Block`/`DamageSource`/`FML` adaptation (like `operator/block` Session 5 per-file).
**Resolution:** Deleted 10 to keep green (kept 8 tricks: `BreakBlock`, `Debug`, `Explode`, `Die`, `Evaluate`, `SaveVector`, `ChangeSlot`, `Delay` = 8/50). Will manually craft 1 trick at a time (e.g., `Blaze` via `World.setBlock` + `BlockSnapshot` capture).

---

## Potion Batch — 11 errors → fixed

### `PieceTrickHaste.java:27` etc 5 files
**Errors:**
```
error: package net.minecraft.world.effect does not exist
  import net.minecraft.world.effect.Potions;
error: cannot find symbol variable Potions
  return Potions.MOVEMENT_SPEED;
error: no suitable constructor found for StatLabel(String,boolean)
  setStatLabel(..., new StatLabel(..., true))
```
**Root cause:** Modern `Potion` is `net.minecraft.world.effect.Potions` (registry) + `StatLabel(String,boolean)` translate flag vs GTNH `net.minecraft.potion.Potion` (`Potion.moveSpeed`) + `StatLabel(String)`.
**Resolution:** `world.effect.Potions→potion.Potion`, `Potions→Potion`, `StatLabel(...,true)→StatLabel(...)` via python replace. **Fixed** — 5/5 potion tricks now compile, validates `PotionBase` pattern for remaining 9.

---

## Batch of 4 Entity Operators — 42 errors → reverted (still 129)

### 4 files: `ClosestToPoint`, `ClosestToLine`, `EntityRaycast`, `FocusedEntity`
**Errors:**
```
error: package net.minecraft.world.level does not exist
  import net.minecraft.world.level.Level;
error: cannot find symbol method getX() / getViewVector / getEyeHeight (modern Vec3 x() vs 1.7.10 xCoord, getLookVec)
error: cannot find symbol method getCommandSenderWorld / level() / position()
error: cannot find symbol class AABB / HitResult / Direction
error: cannot find symbol method isPickable / getPickRadius / getBoundingBox().inflate (modern AABB vs 1.7.10 AxisAlignedBB)
```
**Root cause:** Modern `Level`/`AABB`/`HitResult`/`Direction`/`Vec3` records vs GTNH `World`/`AxisAlignedBB`/`MovingObjectPosition`/`ForgeDirection`/`Vec3.createVectorHelper`. Simple `getX→posX` swaps insufficient — each operator needs manual `WorldHelper` + `AxisAlignedBB` + `Vec3` GTNH craft per file (like `operator/block` Session 5).
**Resolution:** Deleted 4 to keep green (kept 9 entity ops: `EntityLook`, `Position`, `Health`, `Height`, `Motion`, `AxialLook`, `ListAdd/Remove`, `RandomEntity` = 9/13). Will manually craft 1 at a time with `WorldHelper`.

---

## Other Pieces — 62 errors → reverted (still 139)

### 4 files: `PieceConnector`, `CrossConnector`, `ErrorCatch`, `ErrorSuppressor`
**Errors:**
```
error: package net.minecraft.resources does not exist
  import net.minecraft.resources.ResourceLocation;
error: method does not override or implement a method from a supertype
  @Override public boolean catchException(...)
error: cannot find symbol variable paramSides / x / y
  SpellParam.Side side = paramSides.get(piece);
  SpellPiece actualPiece = spell.grid.getPieceAtSideWithRedirections(x, y, side);
error: cannot find symbol method getRawParamValue
```
**Root cause:** Modern `other` pieces implement `IRedirector`/`IGenericRedirector`/`IErrorCatcher` with `SpellGrid.getPieceAtSideWithRedirections(x,y,Side)` + `paramSides` map (modern `SpellPiece` has `paramSides` private, `x`/`y` fields) vs 1.7.10 `SpellPiece.java:30` `paramSides` is `LinkedHashMap` but `x`/`y` are public, but method `getPieceAtSideWithRedirections` missing (1.7.10 `SpellGrid.java:1` only `getPieceAtSideSafely`), plus `ResourceLocation` import `net.minecraft.resources` vs `net.minecraft.util`.
**Resolution:** Deleted 4 to keep green; kept `IGenericRedirector`/`IRedirector`/`IErrorCatcher` stubs (3 interfaces) for future. Requires `SpellCompiler` expansion (`redirectionPieces`, `IGenericRedirector` handling) + `SpellGrid` `getPieceAtSideWithRedirections` port.

---

## Summary Table

| File | Session | Error Count | Category | Root Cause | Resolution | Status |
|---|---|---|---|---|---|---|
| `PieceOperatorGammaFunc.java:13` | 2 | 2 | missing dep | `Gamma` not ported | Copy `Gamma.java` 1:1 | **Fixed** |
| `PieceOperatorRandom.java:38` | 2 | 1 | generic | `int 0` vs `Double` | `0 → 0D` | **Fixed** |
| `VectorAbsolute/RaycastAxis/Rotate` | 3 | 16 | API | `SpellHelpers`, `Level.clip`, `Vector3.rotate` | `Quat` + `Vector3.rotate` + `SpellHelpers` GTNH | **Fixed** (re-enabled S4) |
| `operator/block` 5 | 3/5 | 47 → 1 | API | `Level`/`BlockState`/`Direction` vs `World`/`Block`/`ForgeDirection` | GTNH `World.getBlock`, `isSideSolid`, `getIndirectPowerLevelTo` | **Fixed** (5/5) |
| `BlockComparatorStrength` | 5 | 1 | missing method | `getComparatorInputOverride` not in 1.7.10 `World` | Stub `getIndirectPowerLevelTo` | **Fixed** |
| `SpellHelpers` | 4 | 3 | missing method | `getNotNullParamEvaluation` not in 1.7.10 `SpellPiece` | `→ getParamEvaluation` + null check | **Fixed** |
| `operator/entity` 11 | 6 | 66 | API | `Level.getEntities(AABB)`, `getLookAngle`, `isPickable`, `Direction`, `Vec3` records | Delete 11, keep wrappers | **Deferred** |
| `EntityListWrapper` | 6 | 1 | annotation | `@NotNull` no `annotations` dep | Remove `@NotNull` | **Fixed** |
| `SpellPiece` helpers | 7 | 3 | missing method | `getNotNullParamValue` not in 1.7.10 | Add 3 helpers | **Fixed** |
| `ListIndex` + 4 list | 7 | 3 | import/generic | `world.entity.Entity`, `SpellParam<Number>` | Import swap `entity.Entity`, `Double` | **Fixed** (5/5) |
| `SelectorTime` | 8 | 1 | import | `world.item.ItemStack` | `item.ItemStack` | **Fixed** |
| `TickTime`/`Tps` | 8 | 4 | API | `getServer().getTickTime(dimension)` no GTNH | Stub `50.0` ms | **Fixed** |
| `FocalPoint` | 8 | 1 | import | `world.entity.Entity` | `entity.Entity` | **Fixed** |
| `trick/*` 19 | 9 | 100 | API | `getCommandSenderWorld`, `NeoForge`, `BlockState`, `CompoundTag`, `RemovalReason` | Delete 19 | **Deferred** |
| `trick` subset 7 | 9 cont | 22 | API | `StatLabel(String,boolean)`, `delay/targetSlot`, `CompoundTag` | Keep 2 (`Die`, `Evaluate`), delete 5 | **Partial fixed** (2/7) |
| `BreakLoop` + 4 tricks after SpellContext expansion | 10 | 11 | API | `world.entity.Entity`, `PlayerDataHandler`, `NBTTagCompound putInt/load`, `isEmpty`, `getCapability`, `RemovalReason` | Delete 5 again, keep `SpellContext` expansion | **Deferred** (needs manual `World`/`NBTTagCompound`/`PlayerPsiHandler` per trick) |

---

## Patterns & Lessons

- **Math-only operators** (`number`, `vector` pure `Vector3`, `constants`, `list` pure `EntityListWrapper`) auto-port with `Number→Double` + import swaps and compile with zero manual logic.
- **World-touching operators/tricks** (`block`, `entity`, `trick` with `Level`/`BlockState`/`AABB`/`NeoForge` events) need manual GTNH `World`/`Block`/`ForgeDirection`/`AxisAlignedBB`/`World.getEntitiesWithinAABB`/`MinecraftForge.EVENT_BUS` adaptation per file — auto-copy fails (47/66/100 errors). Strategy: delete to keep green, then manually craft 1 file at a time like `operator/block` Session 5.
- **Helper gaps** (`Gamma`, `Quat`, `SpellHelpers`, `SpellPiece.getNotNull*`) cause cascading failures across many operators — fixing one helper unblocks an entire category (e.g., `Gamma` unblocked `GammaFunc`; `SpellHelpers` unblocked 3 vectors + 5 blocks; `SpellPiece` helpers unblocked 5 list).
- **Annotation deps** (`org.jetbrains:annotations`) not in GTNH — remove `@NotNull`/`@Nullable` rather than adding dep.
- **Modern `SpellContext` 230 lines** vs 1.7.10 170 — missing `delay`/`targetSlot`/`tool`/`cspell` blocks tricks/selectors like `Delay`, `ChangeSlot`, `SaveVector`. Must expand `SpellContext.java:25` before re-attempting those (Phase 3).
- **Package renames** 1.21 `net.minecraft.core.*`/`world.level.*`/`world.phys.*`/`world.entity.*` → 1.7.10 `net.minecraft.util.*`/`world.*`/`util.*`/`entity.*` + `compampac/BlockPosCompat` shim — systematic, automatable via python `replace`.

> Next helpers to unblock deferred batches: `SpellContext` expansion (for `Delay`/`SaveVector`), `World` entity query helper (`getEntitiesWithinAABB` + `AxisAlignedBB` inflate) for `Nearby*`/`ClosestTo*`, manual `Block`/`DamageSource` helpers for `Blaze`/`Smite` tricks.

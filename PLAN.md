# Psi 1.21.1 -> 1.7.10 Full Backport Plan — Serviceable / Readable / Close-to-Source

**Goal:** `C:\Users\fabib\Documents\Minecraft\1.7.10\Psi\Psi-1.7.10\Psi` reads as a 1:1 line-by-line port of `C:\Users\fabib\Documents\Minecraft\1.7.10\Psi\Psi-1.21.1-source` with minimal 1.7.10 shims, fully documented.

**Current baseline verified:**
* `129 java` (backport) vs `434 java` (1.21.1) — delta **343 missing** + `9 json` vs `509 json`. ~12/180 spell pieces registered in `src/main/java/vazkii/psi/common/spell/SpellPieceRegistry.java:108`. Visuals ~80% done per `git log --oneline` (`b2abb8d1` CAD Models, `91735b1b` copy 176 spell textures, `f5f47acd` `PieceTextureAtlas.java:1`, `05fe0105` programmer refactor) — user statement confirmed. Build: `build.gradle.kts:1` GTNH convention (Java 8/Forge 10.13.4) vs `build.gradle:1` NeoForge 21.1.207/Java 21.

### 1. Philosophy: "Close to Source" in 1.7.10

1.  **Keep package/class/method/field names identical.** Do not move `vazkii.psi.common.spell.base.ModSpellPieces` to `SpellPieceRegistry`. Keep modern files even if commented.
2.  **Comment, don't delete, modern code:** `/* 1.21.1: Codec<SpellPiece> CODEC = ... — 1.7.10: NBT tag "_psi" */`. Enables `diff` and forward-merge.
3.  **Compat layer isolated:** Expand `src/main/java/vazkii/psi/compampac/BlockPosCompat.java:1` + `ResourceLocationCompat.java:1` + add `ComponentCompat`, `BlockStateCompat`. No version-specific `if` in core logic.
4.  **Document mapping once:** `docs/GTNH_MAPPING.md` — single table `DeferredRegister -> GameRegistry`, `DataComponent -> NBT`, `Capability -> ExtendedEntityProperties`, `CustomPacketPayload -> IMessage`, `Holder<MobEffect> -> Potion`.
5.  **Per-file header:** `// Modern counterpart: Psi-1.21.1/src/main/java/vazkii/psi/api/spell/SpellGrid.java:14 — GTNH adaptation: NBTTagCompound/List`

### 2. Delta Map (what must be ported)

| System | Modern count | Backport count | Delta | Difficulty |
|---|---|---|---|---|
| `common/spell/operator/{block,entity,list,number,vector,trig}` | ~70 | 4 | ~66 | Low-Med |
| `common/spell/trick/{block,entity,infusion,potion}` | ~45 | 3 | ~42 | High (Conjured/Potion) |
| `common/spell/selector+other+constant` | ~50 | 6 | ~44 | Med |
| `common/item` (tool, armor, sensors, ruler, drives, flash) | 24 | 11 | 13 | High (Exosuit) |
| `api/spell` (events, redirector, error handler) | 18 | 15 | 6 | Med |
| `common/network/message` | 17 | 6 (`Packet*`) | 11 | Med |
| `api/cad` + `common/block` (Conjured, plates, slots) | 17 | 10 | 7 | Med |
| `client/gui` (`GuiSocketSelect`, `GuiFlashRing`, 3 widgets) | 14 | 7 | 7 | Med (visuals done) |
| `api/internal` (Quat, Vector3, PsiRenderHelper) | 9 | 2 | 7 | Low |
| `common/lib` (`LibPieceNames:190`, `LibPieceGroups:28`) | 7 | 1 | 6 | Low but touches all |
| `client/jei`, `client/patchouli`, `data/*`, `mixin`, `attribute` | ~30 | 0 | 30 | Defer/Optional |

### 3. 1.7.10 Adaptation Cheatsheet

| 1.21.1 | 1.7.10 Replacement | Example File |
|---|---|---|
| `DeferredRegister<T>/DeferredHolder` | `GameRegistry.registerBlock/Item` + `HashMap<ResourceLocation,Class>` | `vazkii.psi.common.Psi.java:43` -> `common/core/proxy/CommonProxy.java:1` `preInit` |
| `DataComponentType<T>` (`ModDataComponents.java:20` 11 types) | `NBTTagCompound` key (`"spell"`, `"cadData"`) | `common/core/handler/capability/CADData.java:1` |
| `ICapability<EntityCapability>` | `IExtendedEntityProperties` + `EntityConstructing` event | `common/core/handler/PlayerPsiHandler.java:1` extend |
| `CustomPacketPayload + StreamCodec` | `SimpleNetworkWrapper` + `IMessage/IMessageHandler` (`PacketHandler.java:1`) | `common/network/MessageRegister.java:1` |
| `BlockState` / `BlockPos` / `Level` | `int x,y,z` / `World` / `Block` + metadata | `common/block/BlockConjured.java:1` |
| `MobEffectInstance(Holder<MobEffect>)` | `PotionEffect(Potion.id, amp, dur)` | `common/spell/trick/potion/PotionBase.java:1` |
| `Codec/StreamCodec` | `writeToNBT/readFromNBT` (`Spell.java:1`, `SpellGrid.java:14`) | `api/spell/SpellCompiler.java:21` |
| `Component.literal` / `PoseStack` | `I18n.format` / `GL11` + `Tessellator` | `client/gui/GuiProgrammer.java:1` |
| `ShaderHandler` / `WispParticleData` | `EntityFX` / `EffectRenderer` no-op | `client/fx/FXWisp.java:1` |
| `Mixins` (4) | Event hook or skip | `mixin/client/HumanoidArmorLayerMixin.java:1` |

Existing compat: `compampac/BlockPosCompat.java` + `ResourceLocationCompat.java` — extend pattern.

### 4. Phased Roadmap (Dependency-Ordered)

**Phase 0 — Foundation (1-2d, blocks all):** `LibPieceNames.java:11` (190 const) + `LibPieceGroups.java:15` (28) + `LibItemNames/BlockNames/AttributeNames` + `LibResources.java:10` + `PsiAPI.java:46` registry stubs. Copy verbatim, replace `ResourceLocation.fromNamespaceAndPath` -> `new ResourceLocation("psi", s)`. Verifies IDs match modern `assets/psi/textures/spell/*.png` (176 already copied `91735b1b`).

**Phase 1 — NBT Bridge (1w):** Replace `ModDataComponents.java:20` with NBT helpers. Keep class with same `static final String KEY_SPELL = "spell"` constants but impl `getSpell(ItemStack) { return tag.getTag("spell") }`. Fix `ItemSpellDrive`, `ItemCAD.java:1`, `CADData.java:1` — current `8d86d127` `shift-click crash` lives here. Document `DataComponent -> NBT` table.

**Phase 2 — Capability Bridge (1w):** Port `common/core/capability/CapabilityTriggerSensor.java:1` + `common/core/handler/capability/CapabilityHandler.java:1` -> `PlayerPsiHandler.java` + `CapabilityHandler` as `ExtendedProperties`. Keep `PsiAPI.SPELL_IMMUNE_CAPABILITY` as String key for doc. Add `ISpellImmune`, `IDetonationHandler`.

**Phase 3 — Compiler/Core (2w, critical path):** Expand `api/spell/SpellCompiler.java:28` (currently simplified, no `IErrorCatcher:1`/`IGenericRedirector:1`/`PieceErrorHandler:1`). Port `common/spell/SpellCompiler.java:38` logic (`Either<CompiledSpell, Ex>`, `redirectionPieces`, `SAME_SIDE_PARAMS`, `STAT_OVERFLOW`), `SpellCache.java:1`, `CompiledSpell.java:1`, `SpellContext.java:25` (add `ItemStack tool`, `BlockPos positionBroken`, `targetSlot`, `Stack<Action>`), `LoopcastTrackingHandler.java:1`, `InternalMethodHandler.java:1`, `PlayerDataHandler.java:1`. Keep `CompiledSpell.Action` API identical.

**Phase 4 — Operators (2-3w, parallelizable, first PR):** Bulk port `operator/number/*` (15: `PieceOperatorSum:1` already) + `operator/vector/*` (20) + `operator/block/*` (5) + `operator/entity/*` (10) + `operator/list/*` (5). Template: `common/spell/operator/entity/PieceOperatorEntityLook.java:1`, `PieceOperatorVectorRaycast.java:1` ported `21397e63` — reuse `Level->World` replacement. Low risk, gives 70 runnable pieces with GUI.

**Phase 5 — Selectors + Connectors (1.5w):** `selector/entity/Nearby*` (15: `PieceSelectorNearbyAnimals:1` etc), `selector/PieceSelectorSavedVector.java:1`, `PieceSelectorEidosChangelog`, `PieceSelectorLoopcastIndex`, `PieceSelectorItemPresence`, `other/PieceConnector.java:1`, `CrossConnector`, `ErrorCatch/Suppressor`. Needs `ParamEntityListWrapper -> ArrayList<Entity>` + `EntityListWrapper.java:1`.

**Phase 6 — Tricks Block/Entity + Conjured (3w, high):** `trick/block/*Sequence` (14: `ConjureBlockSequence`, `PlaceInSequence`) requires `BlockConjured.java:1` + `TileConjured.java:1` (`TileEntity` with `updateEntity` + `worldObj.scheduleUpdate`), `trick/entity/Mass*` (3). Swap `world.setBlockState -> world.setBlock(x,y,z,block,meta,2)`.

**Phase 7 — Infusion/Potion (2w):** `trick/infusion/*` (3) + `trick/potion/PotionBase.java:1` + 12 effects. Map `Holder<MobEffect>` -> `Potion.fieldId`. Infusion needs `TrickRecipe.java:1` -> `GameRegistry.addRecipe`.

**Phase 8 — Exosuit + Tools (3w, deferrable):** `api/exosuit/*` (4) + `item/armor/*Exosuit*` (4) + `item/tool/*Psimetal*` (4) + `PsimetalArmorMaterial.java:1` -> `EnumHelper.addArmorMaterial`, `ModAttributes.java:1` -> `SharedMonsterAttributes`, 5 sensors + `SelectorCasterEnergy/Battery`. Fire via `LivingHurtEvent`, `PlayerTickEvent`.

**Phase 9 — Blocks/Items/Crafting + Slots (1w):** `ModItems.java:1` (40 items: psidust, psimetal, ebony/ivory, 16 colorizers, 6 bullets, drives), `ModBlocks.java:1` (plates, psidust/metal/gem blocks), `ModCraftingRecipes:10` (`AssemblyScavengeRecipe`, `BulletUpgradeRecipe`, `SensorAttachRecipe`), `common/block/tile/container/slot/*` (4 slot validators fixing `GuiCADAssembler.java:1`).

**Phase 10 — Network (1w):** 11 missing `Message*` (`MessageDataSync`, `DeductPsi`, `LoopcastSync`, `EidosSync`, `FlashRingSync`, `SpellModified`, `TriggerJumpSpell`, `ParticleTrail`) -> `Packet*` via `PacketHandler.java:1` `SimpleNetworkWrapper("psi")` + `ByteBuf` NBT.

**Phase 11 — GUI Polish (2w):** `GuiSocketSelect.java:1` vs `GuiCADSelect.java:1`, `GuiFlashRing.java:1` + `CallbackTextFieldWidget.java:1`, `SpellCostsWidget`, `StatusWidget`, `GuiButtonSpellPiece/Page/SideConfig` — visuals 80% done (`4549310a`, `0616bb44`), remaining logic: assembler validators, socketable switching via `MessageChangeSocketableSlot`.

**Phase 12 — Rendering/Misc (1w, optional):** `ModParticles.java:1` -> `FXSparkle/FXWisp` + `EntityFX`, `ShaderHandler.java:1` no-op, `ModEntities.java:1`, `ContributorSpellCircleHandler.java:1`, `DetonationHandler.java:1`, `ModTags.java:1`. Skip `data/DataGenerator.java:1` (manual json) + `JEI/Patchouli` -> NEI later.

**Estimate:** 15-20w solo, 8-10w with 2 contributors (Phase 4/5 parallel). Fastest servicable slice: Phase 0->3->4 = 5w for 80+ runnable tricks with existing GUI.

### 5. Resources Pipeline

Keep `textures/spell/*`, `textures/block/*`, `textures/item/*` names identical (already 179 vs 176 png). Convert `assets/psi/models/item/*.json` (93 modern) -> `IItemRenderer` using existing `client/model/cad/CadModelLoader.java:1` + `CadBakedModel.java:1` + `CadModels.java:1`. `models/block` + `blockstates` -> `IIcon` + `TileEntitySpecialRenderer` (`BlockMachineRenderer.java:1` already). `lang/en_us.json` -> `en_US.lang` (`c13b24e7` already does param names). Datagen (`data/Psi*Provider.java:1`) kept as reference, not runtime. Patchouli books `patchouli_books/encyclopaedia_psionica` -> skip, or manual guide later.

### 6. Documentation & Readability Checklist

* `docs/GTNH_MAPPING.md` + root `README.md` section "Porting Notes"
* `spotless` already run `527f0aec` — keep GTNH convention `exclude .git`
* Each new file: license header + `1.7.10 Backport:` javadoc + line ref to modern source
* `COMPAMPAC_REQUIREMENTS.md:1` update with new shims
* Tests: extend `common/command/CommandPsiTest.java:1` for compile round-trip (`_params` clipboard format `d1288971`)

### 7. Immediate Next Actions (2-week vertical slice)

1.  Phase 0: copy `Lib*.java` verbatim — fixes `SpellPieceRegistry:72` hardcoded 12 -> 190.
2.  Phase 1: NBT bridge — fixes `8d86d127` bullet slotting + `CADData` shift-click.
3.  Phase 3: expand `SpellCompiler` to include redirectors — unblocks all future pieces.
4.  Bulk Phase 4 number/vector operators (copy-paste 20 files, 1:1 `Level->World`).

This ordering delivers a readable, diff-friendly codebase that stays mergeable with upstream `v109` while being fully functional on 1.7.10 Forge/GTNH.
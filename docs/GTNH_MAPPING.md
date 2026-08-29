# Psi 1.21.1 -> 1.7.10 GTNH Mapping Reference

> Goal: keep `C:\Users\fabib\Documents\Minecraft\1.7.10\Psi\Psi-1.21.1-source` readable as 1:1 diff with `C:\Users\fabib\Documents\Minecraft\1.7.10\Psi\Psi-1.7.10\Psi`.
> Every modern file kept with same package/class/method names; adaptations are isolated in `compampac` or inline `// GTNH` comments.

## Build / Toolchain

| Modern | GTNH 1.7.10 | File |
|---|---|---|
| `build.gradle:1` NeoForge ModDev 2.0.107, Java 21, `parchment 2024.11.17`, `spotless 7.2.1`, `runs {client/server/data --mod psi}` | `build.gradle.kts:1` `id("com.gtnewhorizons.gtnhconvention")`, Java 8, `gradle.properties:8` `minecraftVersion=1.7.10 forgeVersion=10.13.4.1614` | `gradle.properties`, `settings.gradle.kts` |
| `gradle.properties:16` `minecraft_version=1.21.1 neo_version=21.1.207`, `version=109` | `gradleTokenVersion=VERSION` generates `vazkii.psi.common.lib.LibMisc:16` | `LibMisc` (generated) |

## Registry

| Modern | GTNH 1.7.10 | Notes |
|---|---|---|
| `DeferredRegister<T>/DeferredHolder` in `common/Psi.java:51` + `ModSpellPieces.java:50` `SPELL_PIECES`, `ADVANCEMENT_GROUPS` | `GameRegistry.registerBlock/Item` in `common/core/proxy/CommonProxy.java:1` `preInit` + `common/spell/SpellPieceRegistry.java:30` `HashMap<String,Supplier<SpellPiece>>` | Keep modern field names as comments `// DeferredHolder -> GameRegistry` |
| `RegistryBuilder<SpellPiece>` `PsiAPI.java:47` `SPELL_PIECE_REGISTRY` | `SpellPieceRegistry` only | `api/PsiAPI.java:47` string keys `SPELL_PIECE_REGISTRY_TYPE_KEY` |
| `TagKey<Item/Block>` `common/lib/ModTags.java:20` | `common/lib/ModTags.java:1` plain `String` ore-dict names (`"ingotPsimetal"`) | Modern `TagKey.create(Registries.ITEM, Psi.location(...))` -> GTNH `OreDictionary` check |

## Data / Capabilities

| Modern | GTNH 1.7.10 |
|---|---|
| `DataComponentType<T>` `common/item/base/ModDataComponents.java:22` 12 types: `SPELL, CAD_DATA, BULLETS, SELECTED_SLOT, REGEN_TIME, SENSOR, etc` with `Codec + StreamCodec` | `common/item/base/ModDataComponents.java:1` string NBT keys + static helpers `getSpell(ItemStack)` -> `ItemSpellBullet.getSpell` / `CADData NBT "PsiCADData"` — see `common/item/ItemSpellBullet.java:30`, `common/core/handler/capability/CADData.java:38` |
| `EntityCapability<ISpellImmune>` `api/PsiAPI.java:40` + `ItemCapability<ICADData>` 6 caps | `IExtendedEntityProperties` registration in `PlayerDataHandler` / `PlayerPsiHandler.java:1` + ItemStack NBT (`CADData` tag) | `api/PsiAPI.java:40` exposes same constant names as `String` keys `SPELL_IMMUNE_CAPABILITY = "psi:spell_immune"` |
| `CompoundTag` / `ListTag` | `NBTTagCompound` / `NBTTagList` | `api/spell/Spell.java:1`, `SpellGrid.java:14` `writeToNBT/readFromNBT` use `"_psi"` underscore IDs + `dual params/paramSides` compat |
| `Holder<MobEffect>` / `MobEffectInstance` | `Potion` id `int` + `PotionEffect` | `common/spell/trick/potion/PotionBase.java:1` maps `Holder` to `Potion.fieldId` |

## World / Level

| Modern | GTNH 1.7.10 |
|---|---|
| `Level`, `BlockPos`, `BlockState`, `BlockHitResult`, `Direction`, `Holder` | `World`, `int x,y,z`, `Block` + `int meta`, `MovingObjectPosition`, `ForgeDirection` | `common/block/BlockConjured.java:1` `world.setBlock(x,y,z,block,meta,2)` + `world.scheduleBlockUpdate`; compat in `compampac/BlockPosCompat.java:1` |
| `LivingEntity`, `Player` | `EntityLivingBase`, `EntityPlayer` | `api/spell/SpellContext.java:25` fields `EntityPlayer, ItemStack tool, BlockPos` |
| `ItemStack.EMPTY`, `Component.literal`, `PoseStack` | `null` / `I18n.format`, `GL11` + `Tessellator` | `client/gui/GuiProgrammer.java:1` replaces `PoseStack` with `GL11` |

## Network

| Modern | GTNH 1.7.10 |
|---|---|
| `CustomPacketPayload` + `StreamCodec` 16 messages `common/network/MessageRegister.java:1` `MessageDataSync, DeductPsi, LoopcastSync, EidosSync, etc` | `SimpleNetworkWrapper("psi")` + `IMessage/IMessageHandler` 6 packets `common/network/PacketHandler.java:1` `PacketSpellUpdate, PacketPsiSync, PacketLoopcastSync, etc` | Keep modern channel name, manual `ByteBuf` `writeToNBT` |

## Rendering

| Modern | GTNH 1.7.10 |
|---|---|
| `RenderType`, `ShaderHandler.java:1`, `PsiParticleRenderType.java:1`, `WispParticleData` | `Tessellator` / `EntityFX` no-op (`client/fx/FXWisp.java:1`, `FXSparkle.java:1`) |
| `BlockEntityRenderer` + `ModelLayers` | `TileEntitySpecialRenderer` (`client/render/tile/RenderTileProgrammer.java:1`) + `ISimpleBlockRenderingHandler` (`client/model/cad/CadModelLoader.java:1`) |
| `Mixin` 4 files (`HumanoidArmorLayerMixin`, `ParticleEngineMixin`) | Event hook or ASM `Transformer` / skip | `build.gradle` `usesMixins=false` |

## GUI

| Modern | GTNH 1.7.10 |
|---|---|
| `GuiGraphics`, `Component`, `AbstractWidget`, `EditBox` (`CallbackTextFieldWidget.java:1`) | `GuiScreen`, `GuiButton`, `GuiTextField` | `client/gui/GuiProgrammer.java:1` + `client/gui/widget/PiecePanelWidget.java:1` math preserved, swap `PoseStack->GL11` |

## Lib Constants

All `common/lib/*` now 1:1 with modern: `LibPieceNames.java:11` 190, `LibPieceGroups.java:15` 28, `LibItemNames.java:11` 92, `LibBlockNames.java:11` 14, `LibResources.java:18`, `LibAttributeNames:1` etc. Copied verbatim; only `LibPieceGroups` needed `ResourceLocation` adapt `net.minecraft.resources.ResourceLocation -> net.minecraft.util.ResourceLocation` via `Psi.location(String)` shim `common/Psi.java:73`.

## Per-file header convention

```java
// Modern counterpart: Psi-1.21.1/src/main/java/vazkii/psi/api/spell/SpellGrid.java:14
// GTNH adaptation: NBTTagCompound/List instead of CompoundTag/ListTag
```

Keep modern imports commented where replaced:
```java
// modern: import net.minecraft.core.BlockPos;
// GTNH: import vazkii.psi.compatockPosCompat;
```

## Verification

After any phase run:

```
./gradlew compileJava --rerun-tasks
./gradlew runClient  # manual visual check: programmer arrows, CAD assembly, spell conjure
```

Trace with `common/command/CommandPsiTest.java:1` — `psi-test compile <spellName>` should round-trip `_params` NBT `d1288971`.

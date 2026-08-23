# Psi → 1.7.10 (GTNH) Backport Task List

Prereq assumed done: repo forked, cloned, `./gradlew build` and `./gradlew runClient` work on 1.21.1.

---

## Phase 0 — Reference & Environment Setup

- [ ] Tag/branch the working 1.21.1 checkout as read-only reference (`git checkout -b ref/1.21.1-source`), never edit it directly.
- [ ] Create new branch/repo for the port: `git checkout -b port/1.7.10`.
- [ ] Set up a second, separate 1.7.10 Forge MDK (Forge 10.13.4.x) as the actual build target — do not try to reuse the 1.21.1 Gradle config.
- [ ] Confirm the 1.7.10 MDK itself builds and `runClient` launches vanilla Forge before touching Psi code.
- [ ] Get a reference 1.7.10 mod with similar systems checked out locally for API lookup, e.g.:
  ```
  git clone https://github.com/VazkiiMods/Botania -b 1.7.10-final ref/botania-1.7.10
  ```
- [ ] Install CLI static-analysis tools: `ripgrep`, `jdeps` (bundled with JDK), optionally `jqassistant`.

---

## Phase 1 — Static Mapping (before writing any port code)

- [ ] Run risk-marker scan against the reference source, save output to file for triage:
  ```
  rg -l "util\.math\.BlockPos" src/main/java > markers_blockpos.txt
  rg -l "common\.capabilities" src/main/java > markers_capabilities.txt
  rg -l "registries\.IForgeRegistry|RegistryEvent|ObjectHolder" src/main/java > markers_registry.txt
  rg -l "renderer\.block\.model|renderer\.texture\.TextureAtlasSprite" src/main/java > markers_rendering.txt
  rg -l "org\.spongepowered\.asm\.mixin" src/main/java > markers_mixin.txt
  ```
- [ ] Run `jdeps` scoped to the mod package to get class-level dependency graph:
  ```
  jdeps -v -filter:package -include 'vazkii\.psi\..*' build/libs/*.jar > psi_deps.txt
  ```
- [ ] Merge marker files into one "touched by version-specific API" class list (dedupe with `sort -u`).
- [ ] Cross-reference `jdeps` output against that list to find *transitively* affected classes (files that call into a flagged class but weren't flagged themselves).
- [ ] Produce three buckets from the merged data:
  - **Bucket A — pure logic** (no hits): spell VM, math tricks, compiler — port near-verbatim.
  - **Bucket B — API-touching** (direct hits): block/entity interaction tricks, registration, capabilities.
  - **Bucket C — rendering/network subsystem** (Mixin/model/texture hits): GUI, CAD/bullet rendering, particles, packets.
- [ ] Commit the bucket lists into the repo (`docs/port-buckets.md`) as the source of truth for task assignment later.

---

## Phase 2 — Foundation (serial, do not parallelize, blocks everything else)

- [ ] Coordinate shim: replace `BlockPos` usage with 1.7.10-style `int x, y, z` (or a thin wrapper class if it reduces diff size) — start with Bucket B files only.
- [ ] Replace Capability-based player/entity data with `IExtendedEntityProperties` implementation for caster stats (Psi amount, overflow, etc.).
- [ ] Rebuild registration layer using 1.7.10 `GameRegistry` calls (no `RegistryEvent`/`ObjectHolder`).
- [ ] Port the spell VM / compiler (Bucket A) — should require minimal changes; treat any compile error here as a red flag, not routine.
- [ ] Stand up base classes only, no full feature set yet: CAD item, Bullet item, Socket/Piece base classes, Spell/SpellContext.
- [ ] Set up networking channel (`SimpleNetworkWrapper`, available in Forge 10.13) for CAD/spell sync.
- [ ] **Gate check:** project compiles, a CAD item can be held and right-clicked with no crash, before moving to Phase 3.

---

## Phase 3 — Foundation Validation Spells (small, deliberate, not parallel yet)

Port just enough pieces to run these four, one per subsystem:

- [ ] Spell 1 — pure math + debug/print trick (validates VM only, no world interaction).
- [ ] Spell 2 — raycast + break/place block (validates coordinate shim — highest risk item).
- [ ] Spell 3 — entity-affecting trick (damage/potion) (validates IEEP + entity API).
- [ ] Spell 4 — visible particle/spell-circle effect (validates GL11/TESR rendering, independent subsystem).
- [ ] Run Spell 2–4 on a **dedicated server + separate client**, not singleplayer only — catches sync/desync bugs early.
- [ ] Only after all four pass: declare foundation stable, tag it (`git tag foundation-stable`).

---

## Phase 4 — Bulk Trick/Piece Porting (parallelizable — safe to split across contributors here)

- [ ] From `docs/port-buckets.md`, generate a per-piece checklist (one line per Trick/Operator/Selector class).
- [ ] Port Bucket A remaining pieces first (lowest risk, builds confidence/pattern reuse).
- [ ] Port Bucket B pieces next, reusing the coordinate/IEEP patterns established in Phase 2/3.
- [ ] Port Bucket C pieces (rendering-heavy) last, reusing the TESR/particle pattern from Spell 4.
- [ ] Require each ported piece to compile + have a minimal test spell before merge (no batch-merging unverified pieces).
- [ ] Keep a single maintainer/small group reviewing merges for consistency (helper method duplication, naming drift).

---

## Phase 5 — Integration & Hardening (serial again)

- [ ] Full GUI pass: Spell Programmer drag-and-drop grid, tooltips, error highlighting.
- [ ] Dependency decision: port a minimal AutoRegLib/Patchouli equivalent, or hand-register everything directly (check what other GTNH forks did for precedent).
- [ ] Multiplayer stress test: multiple players casting simultaneously, CAD state persistence across relog/chunk unload.
- [ ] Save/load testing: NBT round-trip on CAD/spell data across world reloads.
- [ ] Compatibility pass against core GTNH mods (ID conflicts, ASM/coremod collisions — check against Angelica/other coremods in the pack).
- [ ] Full regression run through every ported piece via test spells before calling it release-ready.

---

## Ongoing

- [ ] Keep `docs/port-buckets.md` and the per-piece checklist updated as the single tracking artifact — this is what makes Phase 4 parallelizable across contributors without a bigger project management tool.

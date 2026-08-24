# Psi 1.7.10 Backport - Development Status

**Last Updated**: 2026-08-24 15:32  
**Goal**: Barebones spell execution via command  
**Current Phase**: Phase 0 Complete ✅ → Phase 1 Ready

## Quick Links
- Main Roadmap: `../Kiro/BAREBONES_SPELL_ROADMAP.md`
- Compampac Requirements: `COMPAMPAC_REQUIREMENTS.md`
- Phase Progress: `../Kiro/phase_N_progress.md`

## Development Philosophy
**Incremental, testable, focused**:
1. Port one class at a time
2. Test individually before integration
3. Document progress and blockers
4. Focus on barebones functionality first

## Current Status Summary

### ✅ Phase 0: Foundation Classes (Complete)
**Time**: ~30 minutes  
**Files**: 3/3 created

- ✅ **Vector3** - 3D math foundation (pure Java, no Minecraft deps)
- ✅ **BlockPosCompat** - Block position wrapper (1.7.10 doesn't have BlockPos)
- ✅ **ResourceLocationCompat** - Resource location factory

### ✅ Phase 1: Spell Core API (Complete)
**Time**: ~5 minutes  
**Files**: 11/11 created

Core spell system without Minecraft world interaction:
- ✅ EnumSpellStat, EnumPieceType
- ✅ SpellRuntimeException, SpellCompilationException
- ✅ SpellParam + param types (ParamAny, ParamNumber, ParamVector, ParamEntity)
- ✅ StatLabel
- ✅ SpellPiece (minimal stub)

**Next**: Phase 2 - Spell Context & Execution

### ⏳ Phase 2: Spell Context & Execution (Not Started)
**Estimated**: 2-3 hours  
**Files**: ~5 classes

Spell execution framework:
- SpellContext (minimal version)
- SpellMetadata
- SpellPiece (base class)
- PieceTrick, PieceSelector, PieceOperator

### ⏳ Phase 3: First Working Spell (Not Started)
**Estimated**: 3-4 hours  
**Files**: ~3 classes + command

**Milestone Goal**: `/psitest debug "Hello World"` shows chat message

- Spell container
- PieceTrickDebug (chat message)
- CommandPsiTest

### ⏳ Phase 4: Selectors & Operators (Not Started)
**Estimated**: 2-3 hours  
**Files**: ~5 classes

- PieceSelectorCaster
- PieceConstantNumber  
- PieceOperatorSum
- Enhanced test command

### ⏳ Phase 5: World Interaction (Not Started)
**Estimated**: 4-5 hours  
**Files**: ~5 classes

**Milestone Goal**: `/psitest break` destroys looked-at block

- DirectionCompat
- PieceSelectorRaycast
- PieceTrickBreakBlock

### ⏳ Phase 6: Explosion Trick (Not Started)
**Estimated**: 1-2 hours  
**Files**: 1 class

**Milestone Goal**: `/psitest explode 5` creates explosion

- PieceTrickExplode

### ⏳ Phase 7: Spell Piece Linking (Not Started)
**Estimated**: 4-6 hours  
**Files**: ~2 classes + major refactor

**Milestone Goal**: Multi-piece spells with parameter resolution

- SpellGrid
- Parameter resolution system
- Spell piece linking

## Milestones

1. ✅ **M0**: Foundation classes compile
2. ✅ **M0.5**: Core spell API compiles (Phase 1)
3. ⏳ **M1**: `/psitest debug` shows chat message
4. ⏳ **M2**: `/psitest math` performs calculation
5. ⏳ **M3**: `/psitest break` breaks looked-at block
6. ⏳ **M4**: `/psitest explode` creates explosion
7. ⏳ **M5**: Multi-piece spell with parameter linking

## Testing Strategy

### Per-Phase Testing
- Unit tests for pure logic (math, data structures)
- Integration tests with mock Minecraft objects
- In-game verification at milestone boundaries

### Test Deferred Features
- GUI (not in scope)
- Rendering (not in scope)
- Networking beyond commands (not in scope)
- Advanced operators (post-MVP)

## Files Ported So Far

```
Psi-1.7.10-backport/
├── src/main/java/vazkii/psi/
│   ├── compampac/
│   │   ├── BlockPosCompat.java           ✅
│   │   └── ResourceLocationCompat.java   ✅
│   │
│   ├── api/
│   │   ├── internal/
│   │   │   └── Vector3.java              ✅
│   │   │
│   │   └── spell/
│   │       ├── EnumSpellStat.java        ✅
│   │       ├── EnumPieceType.java        ✅
│   │       ├── StatLabel.java            ✅
│   │       ├── SpellCompilationException.java  ✅
│   │       ├── SpellRuntimeException.java      ✅
│   │       ├── SpellParam.java           ✅
│   │       ├── SpellPiece.java (stub)    ✅
│   │       │
│   │       └── param/
│   │           ├── ParamSpecific.java    ✅
│   │           ├── ParamAny.java         ✅
│   │           ├── ParamNumber.java      ✅
│   │           ├── ParamVector.java      ✅
│   │           └── ParamEntity.java      ✅
│   │
│   ├── common/
│   │   ├── Psi.java                      ✅ (stub)
│   │   └── core/
│   │       ├── handler/
│   │       │   └── ConfigHandler.java    ✅ (stub)
│   │       └── proxy/
│   │           └── CommonProxy.java      ✅ (stub)
│   │
│   └── client/core/proxy/
│       └── ClientProxy.java              ✅ (stub)
│
├── COMPAMPAC_REQUIREMENTS.md             ✅
└── STATUS.md                             ✅ (this file)
```

## Next Immediate Actions

1. ✅ Complete Phase 0
2. ✅ Complete Phase 1
3. ⏳ Start Phase 2.1 - Port SpellContext (minimal version)
4. ⏳ Port SpellMetadata
5. ⏳ Port SpellPiece base class (replace stub)

## Blockers & Issues

**Current**: None

**Anticipated**:
- Parameter linking system will be complex
- NBT serialization might need additional work
- Spell compilation logic needs careful porting

## Development Time Estimates

| Phase | Estimated | Actual | Status |
|-------|-----------|--------|--------|
| Phase 0 | 4-6h | 0.5h | ✅ Complete |
| Phase 1 | 2-3h | 0.08h | ✅ Complete |
| Phase 2 | 2-3h | - | ⏳ Not Started |
| Phase 3 | 3-4h | - | ⏳ Not Started |
| Phase 4 | 2-3h | - | ⏳ Not Started |
| Phase 5 | 4-5h | - | ⏳ Not Started |
| Phase 6 | 1-2h | - | ⏳ Not Started |
| Phase 7 | 4-6h | - | ⏳ Not Started |
| **Total** | **22-32h** | **0.58h** | **8% Complete** |

## Communication & Knowledge Base

All development knowledge is tracked in:
- `../Kiro/` directory (outside repo for safety)
- This STATUS.md file (in repo)
- Git commit messages
- Code comments

Knowledge includes:
- Porting notes per class
- Design decisions
- Test results
- Blocker resolutions

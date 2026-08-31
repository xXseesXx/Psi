package vazkii.psi.common.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.lib.LibPieceNames;
import vazkii.psi.common.spell.constant.PieceConstantE;
import vazkii.psi.common.spell.constant.PieceConstantNumber;
import vazkii.psi.common.spell.constant.PieceConstantPi;
import vazkii.psi.common.spell.constant.PieceConstantString;
import vazkii.psi.common.spell.constant.PieceConstantTau;
import vazkii.psi.common.spell.constant.PieceConstantWrapper;
import vazkii.psi.common.spell.other.PieceConnector;
import vazkii.psi.common.spell.other.PieceCrossConnector;
import vazkii.psi.common.spell.other.PieceErrorCatch;
import vazkii.psi.common.spell.other.PieceErrorSuppressor;
import vazkii.psi.common.spell.operator.PieceOperatorSum;
import vazkii.psi.common.spell.operator.block.PieceOperatorBlockComparatorStrength;
import vazkii.psi.common.spell.operator.block.PieceOperatorBlockHardness;
import vazkii.psi.common.spell.operator.block.PieceOperatorBlockLightLevel;
import vazkii.psi.common.spell.operator.block.PieceOperatorBlockMiningLevel;
import vazkii.psi.common.spell.operator.block.PieceOperatorBlockSideSolidity;
import vazkii.psi.common.spell.operator.entity.PieceOperatorClosestToLine;
import vazkii.psi.common.spell.operator.entity.PieceOperatorClosestToPoint;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityAxialLook;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityHealth;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityHeight;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityLook;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityMotion;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityPosition;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityRaycast;
import vazkii.psi.common.spell.operator.entity.PieceOperatorListAdd;
import vazkii.psi.common.spell.operator.entity.PieceOperatorListRemove;
import vazkii.psi.common.spell.operator.entity.PieceOperatorRandomEntity;
import vazkii.psi.common.spell.operator.list.PieceOperatorListExclusion;
import vazkii.psi.common.spell.operator.list.PieceOperatorListIndex;
import vazkii.psi.common.spell.operator.list.PieceOperatorListIntersection;
import vazkii.psi.common.spell.operator.list.PieceOperatorListSize;
import vazkii.psi.common.spell.operator.list.PieceOperatorListUnion;
import vazkii.psi.common.spell.operator.number.PieceOperatorAbsolute;
import vazkii.psi.common.spell.operator.number.PieceOperatorCeiling;
import vazkii.psi.common.spell.operator.number.PieceOperatorCube;
import vazkii.psi.common.spell.operator.number.PieceOperatorDivide;
import vazkii.psi.common.spell.operator.number.PieceOperatorFloor;
import vazkii.psi.common.spell.operator.number.PieceOperatorGammaFunc;
import vazkii.psi.common.spell.operator.number.PieceOperatorIntegerDivide;
import vazkii.psi.common.spell.operator.number.PieceOperatorInverse;
import vazkii.psi.common.spell.operator.number.PieceOperatorLog;
import vazkii.psi.common.spell.operator.number.PieceOperatorMax;
import vazkii.psi.common.spell.operator.number.PieceOperatorMin;
import vazkii.psi.common.spell.operator.number.PieceOperatorModulus;
import vazkii.psi.common.spell.operator.number.PieceOperatorMultiply;
import vazkii.psi.common.spell.operator.number.PieceOperatorPower;
import vazkii.psi.common.spell.operator.number.PieceOperatorRandom;
import vazkii.psi.common.spell.operator.number.PieceOperatorRoot;
import vazkii.psi.common.spell.operator.number.PieceOperatorRound;
import vazkii.psi.common.spell.operator.number.PieceOperatorSignum;
import vazkii.psi.common.spell.operator.number.PieceOperatorSquare;
import vazkii.psi.common.spell.operator.number.PieceOperatorSquareRoot;
import vazkii.psi.common.spell.operator.number.PieceOperatorSubtract;
import vazkii.psi.common.spell.operator.number.trig.PieceOperatorAcos;
import vazkii.psi.common.spell.operator.number.trig.PieceOperatorAsin;
import vazkii.psi.common.spell.operator.number.trig.PieceOperatorCos;
import vazkii.psi.common.spell.operator.number.trig.PieceOperatorSin;
import vazkii.psi.common.spell.operator.vector.PieceOperatorPlanarNormalVector;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorAbsolute;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorConstruct;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorCrossProduct;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorDivide;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorDotProduct;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorExtractX;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorExtractY;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorExtractZ;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorMagnitude;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorMaximum;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorMinimum;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorMultiply;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorNegate;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorNormalize;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorProject;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorRaycast;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorRaycastAxis;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorRotate;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorSignum;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorSubtract;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorSum;
import vazkii.psi.common.spell.selector.PieceSelectorAttackTarget;
import vazkii.psi.common.spell.selector.PieceSelectorAttacker;
import vazkii.psi.common.spell.selector.PieceSelectorBlockBroken;
import vazkii.psi.common.spell.selector.PieceSelectorBlockPresence;
import vazkii.psi.common.spell.selector.PieceSelectorBlockSideBroken;
import vazkii.psi.common.spell.selector.PieceSelectorCaster;
import vazkii.psi.common.spell.selector.PieceSelectorDamageTaken;
import vazkii.psi.common.spell.selector.PieceSelectorEidosChangelog;
import vazkii.psi.common.spell.selector.PieceSelectorEntityPosition;
import vazkii.psi.common.spell.selector.PieceSelectorFocalPoint;
import vazkii.psi.common.spell.selector.PieceSelectorItemCount;
import vazkii.psi.common.spell.selector.PieceSelectorItemPresence;
import vazkii.psi.common.spell.selector.PieceSelectorLoopcastIndex;
import vazkii.psi.common.spell.selector.PieceSelectorRaycast;
import vazkii.psi.common.spell.selector.PieceSelectorRulerVector;
import vazkii.psi.common.spell.selector.PieceSelectorSavedVector;
import vazkii.psi.common.spell.selector.PieceSelectorSneakStatus;
import vazkii.psi.common.spell.selector.PieceSelectorTickTime;
import vazkii.psi.common.spell.selector.PieceSelectorTime;
import vazkii.psi.common.spell.selector.PieceSelectorTps;
import vazkii.psi.common.spell.selector.entity.PieceSelectorCasterBattery;
import vazkii.psi.common.spell.selector.entity.PieceSelectorCasterEnergy;
import vazkii.psi.common.spell.selector.entity.PieceSelectorIsElytraFlying;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyAnimals;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyCharges;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyEnemies;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyFallingBlocks;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyGlowing;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyItems;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyLiving;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyPlayers;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyProjectiles;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbySmeltables;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearbyVehicles;
import vazkii.psi.common.spell.selector.entity.PieceSelectorSuccessCounter;
import vazkii.psi.common.spell.trick.PieceTrickBlaze;
import vazkii.psi.common.spell.trick.PieceTrickBreakBlock;
import vazkii.psi.common.spell.trick.PieceTrickChangeSlot;
import vazkii.psi.common.spell.trick.PieceTrickDebug;
import vazkii.psi.common.spell.trick.PieceTrickDelay;
import vazkii.psi.common.spell.trick.PieceTrickDie;
import vazkii.psi.common.spell.trick.PieceTrickEvaluate;
import vazkii.psi.common.spell.trick.PieceTrickExplode;
import vazkii.psi.common.spell.trick.PieceTrickOvergrow;
import vazkii.psi.common.spell.trick.PieceTrickPlaySound;
import vazkii.psi.common.spell.trick.PieceTrickSaveVector;
import vazkii.psi.common.spell.trick.PieceTrickSmite;
import vazkii.psi.common.spell.trick.PieceTrickTorrent;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakInSequence;
import vazkii.psi.common.spell.trick.block.PieceTrickCollapseBlock;
import vazkii.psi.common.spell.trick.block.PieceTrickConjureBlock;
import vazkii.psi.common.spell.trick.block.PieceTrickMoveBlock;
import vazkii.psi.common.spell.trick.block.PieceTrickPlaceBlock;
import vazkii.psi.common.spell.trick.block.PieceTrickTill;
import vazkii.psi.common.spell.trick.entity.PieceTrickAddMotion;
import vazkii.psi.common.spell.trick.entity.PieceTrickBlink;
import vazkii.psi.common.spell.trick.entity.PieceTrickIgnite;
import vazkii.psi.common.spell.trick.infusion.PieceTrickEbonyIvory;
import vazkii.psi.common.spell.trick.infusion.PieceTrickGreaterInfusion;
import vazkii.psi.common.spell.trick.infusion.PieceTrickInfusion;
import vazkii.psi.common.spell.trick.potion.PieceTrickFireResistance;
import vazkii.psi.common.spell.trick.potion.PieceTrickHaste;
import vazkii.psi.common.spell.trick.potion.PieceTrickInvisibility;
import vazkii.psi.common.spell.trick.potion.PieceTrickJumpBoost;
import vazkii.psi.common.spell.trick.potion.PieceTrickNightVision;
import vazkii.psi.common.spell.trick.potion.PieceTrickRegeneration;
import vazkii.psi.common.spell.trick.potion.PieceTrickResistance;
import vazkii.psi.common.spell.trick.potion.PieceTrickSlowness;
import vazkii.psi.common.spell.trick.potion.PieceTrickSpeed;
import vazkii.psi.common.spell.trick.potion.PieceTrickStrength;
import vazkii.psi.common.spell.trick.potion.PieceTrickWaterBreathing;
import vazkii.psi.common.spell.trick.potion.PieceTrickWeakness;
import vazkii.psi.common.spell.trick.potion.PieceTrickWither;

/**
 * Registry for spell pieces. Maps string IDs to constructor functions.
 * Used for NBT serialization - allows creating pieces by ID.
 */
public class SpellPieceRegistry {

    private static final Map<String, Function<Spell, SpellPiece>> REGISTRY = new HashMap<>();

    /**
     * Register a spell piece type with an ID and factory function.
     */
    public static void register(String id, Function<Spell, SpellPiece> factory) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Spell piece ID already registered: " + id);
        }
        REGISTRY.put(id, factory);
    }

    /**
     * Create a spell piece from its ID.
     * Returns null if ID not registered.
     */
    public static SpellPiece create(String id, Spell spell) {
        Function<Spell, SpellPiece> factory = REGISTRY.get(id);
        if (factory == null) {
            System.err.println("[Psi] Unknown spell piece ID: " + id);
            return null;
        }
        SpellPiece piece = factory.apply(spell);

        // Fix registryKey using reflection (piece's constructor sets it based on class name)
        // We need to override it with the actual registry ID
        try {
            java.lang.reflect.Field keyField = SpellPiece.class.getDeclaredField("registryKey");
            keyField.setAccessible(true);
            keyField.set(piece, new ResourceLocation(id));
        } catch (Exception e) {
            System.err.println("[Psi] Failed to set registryKey for " + id);
            e.printStackTrace();
        }

        return piece;
    }

    /**
     * Get the ID for a spell piece class.
     * Returns null if not registered.
     */
    public static String getID(SpellPiece piece) {
        if (piece == null) {
            return null;
        }

        // Search registry for this piece's class
        Class<? extends SpellPiece> pieceClass = piece.getClass();
        for (Map.Entry<String, Function<Spell, SpellPiece>> entry : REGISTRY.entrySet()) {
            // Create a test instance to check class
            // This is a bit hacky but works for barebones
            try {
                SpellPiece test = entry.getValue()
                    .apply(piece.spell);
                if (test.getClass() == pieceClass) {
                    return entry.getKey();
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        System.err.println("[Psi] No ID registered for piece class: " + pieceClass.getName());
        return null;
    }

    /** A stable snapshot for GUI population and addon-friendly piece pickers. */
    public static java.util.List<String> getRegisteredIds() {
        java.util.List<String> ids = new java.util.ArrayList<String>(REGISTRY.keySet());
        java.util.Collections.sort(ids);
        return ids;
    }

    /**
     * Initialize the registry with all piece types.
     * Called on mod init.
     */
    public static void init() {
        register("psi:" + LibPieceNames.CONNECTOR, PieceConnector::new);
        register("psi:" + LibPieceNames.CROSS_CONNECTOR, PieceCrossConnector::new);
        register("psi:" + LibPieceNames.ERROR_SUPPRESSOR, PieceErrorSuppressor::new);
        register("psi:" + LibPieceNames.ERROR_CATCH, PieceErrorCatch::new);
        // Constants
        register("psi:" + LibPieceNames.CONSTANT_NUMBER, PieceConstantNumber::new);
        register("psi:constant_string", PieceConstantString::new);
        register("psi:" + LibPieceNames.CONSTANT_PI, PieceConstantPi::new);
        register("psi:" + LibPieceNames.CONSTANT_E, PieceConstantE::new);
        register("psi:" + LibPieceNames.CONSTANT_TAU, PieceConstantTau::new);
        register("psi:" + LibPieceNames.CONSTANT_WRAPPER, PieceConstantWrapper::new);

        // Operators — number (modern: common/spell/operator/number/*)
        register("psi:" + LibPieceNames.OPERATOR_SUM, PieceOperatorSum::new); // also via number/ path for parity — flat
                                                                              // + number both valid
        register("psi:" + LibPieceNames.OPERATOR_SUBTRACT, PieceOperatorSubtract::new);
        register("psi:" + LibPieceNames.OPERATOR_MULTIPLY, PieceOperatorMultiply::new);
        register("psi:" + LibPieceNames.OPERATOR_DIVIDE, PieceOperatorDivide::new);
        register("psi:" + LibPieceNames.OPERATOR_ABSOLUTE, PieceOperatorAbsolute::new);
        register("psi:" + LibPieceNames.OPERATOR_INVERSE, PieceOperatorInverse::new);
        register("psi:" + LibPieceNames.OPERATOR_MODULUS, PieceOperatorModulus::new);
        register("psi:" + LibPieceNames.OPERATOR_INTEGER_DIVIDE, PieceOperatorIntegerDivide::new);
        register("psi:" + LibPieceNames.OPERATOR_MAX, PieceOperatorMax::new);
        register("psi:" + LibPieceNames.OPERATOR_MIN, PieceOperatorMin::new);
        register("psi:" + LibPieceNames.OPERATOR_POWER, PieceOperatorPower::new);
        register("psi:" + LibPieceNames.OPERATOR_SQUARE, PieceOperatorSquare::new);
        register("psi:" + LibPieceNames.OPERATOR_CUBE, PieceOperatorCube::new);
        register("psi:" + LibPieceNames.OPERATOR_SQUARE_ROOT, PieceOperatorSquareRoot::new);
        register("psi:" + LibPieceNames.OPERATOR_LOG, PieceOperatorLog::new);
        register("psi:" + LibPieceNames.OPERATOR_CEILING, PieceOperatorCeiling::new);
        register("psi:" + LibPieceNames.OPERATOR_FLOOR, PieceOperatorFloor::new);
        register("psi:" + LibPieceNames.OPERATOR_ROUND, PieceOperatorRound::new);
        register("psi:" + LibPieceNames.OPERATOR_SIGNUM, PieceOperatorSignum::new);
        register("psi:" + LibPieceNames.OPERATOR_RANDOM, PieceOperatorRandom::new);
        register("psi:" + LibPieceNames.OPERATOR_ROOT, PieceOperatorRoot::new);
        register("psi:" + LibPieceNames.OPERATOR_GAMMA_FUNCTION, PieceOperatorGammaFunc::new);
        // trig
        register("psi:" + LibPieceNames.OPERATOR_SIN, PieceOperatorSin::new);
        register("psi:" + LibPieceNames.OPERATOR_COS, PieceOperatorCos::new);
        register("psi:" + LibPieceNames.OPERATOR_ASIN, PieceOperatorAsin::new);
        register("psi:" + LibPieceNames.OPERATOR_ACOS, PieceOperatorAcos::new);

        register("psi:" + LibPieceNames.OPERATOR_ENTITY_POSITION, PieceOperatorEntityPosition::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_LOOK, PieceOperatorEntityLook::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_HEALTH, PieceOperatorEntityHealth::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_HEIGHT, PieceOperatorEntityHeight::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_MOTION, PieceOperatorEntityMotion::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_AXIAL_LOOK, PieceOperatorEntityAxialLook::new);
        register("psi:" + LibPieceNames.OPERATOR_CLOSEST_TO_POINT, PieceOperatorClosestToPoint::new);
        register("psi:" + LibPieceNames.OPERATOR_CLOSEST_TO_LINE, PieceOperatorClosestToLine::new);
        register("psi:" + LibPieceNames.OPERATOR_ENTITY_RAYCAST, PieceOperatorEntityRaycast::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_ADD, PieceOperatorListAdd::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_REMOVE, PieceOperatorListRemove::new);
        register("psi:" + LibPieceNames.OPERATOR_RANDOM_ENTITY, PieceOperatorRandomEntity::new);
        // vector
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_CONSTRUCT, PieceOperatorVectorConstruct::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_EXTRACT_X, PieceOperatorVectorExtractX::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_EXTRACT_Y, PieceOperatorVectorExtractY::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_EXTRACT_Z, PieceOperatorVectorExtractZ::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_MAGNITUDE, PieceOperatorVectorMagnitude::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_DOT_PRODUCT, PieceOperatorVectorDotProduct::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_CROSS_PRODUCT, PieceOperatorVectorCrossProduct::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_NORMALIZE, PieceOperatorVectorNormalize::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_NEGATE, PieceOperatorVectorNegate::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_SUM, PieceOperatorVectorSum::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_SUBTRACT, PieceOperatorVectorSubtract::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_MULTIPLY, PieceOperatorVectorMultiply::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_DIVIDE, PieceOperatorVectorDivide::new);
        register("psi:" + LibPieceNames.OPERATOR_PLANAR_NORMAL_VECTOR, PieceOperatorPlanarNormalVector::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_PROJECT, PieceOperatorVectorProject::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_MAXIMUM, PieceOperatorVectorMaximum::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_MINIMUM, PieceOperatorVectorMinimum::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_SIGNUM, PieceOperatorVectorSignum::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_ABSOLUTE, PieceOperatorVectorAbsolute::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_ROTATE, PieceOperatorVectorRotate::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_RAYCAST_AXIS, PieceOperatorVectorRaycastAxis::new);
        register("psi:" + LibPieceNames.OPERATOR_VECTOR_RAYCAST, PieceOperatorVectorRaycast::new); // flat existing,
                                                                                                   // vector package
                                                                                                   // shares id
        // block
        register("psi:" + LibPieceNames.OPERATOR_BLOCK_HARDNESS, PieceOperatorBlockHardness::new);
        register("psi:" + LibPieceNames.OPERATOR_BLOCK_LIGHT, PieceOperatorBlockLightLevel::new);
        register("psi:" + LibPieceNames.OPERATOR_BLOCK_SIDE_SOLIDITY, PieceOperatorBlockSideSolidity::new);
        register("psi:" + LibPieceNames.OPERATOR_BLOCK_MINING_LEVEL, PieceOperatorBlockMiningLevel::new);
        register("psi:" + LibPieceNames.OPERATOR_BLOCK_COMPARATOR_STRENGTH, PieceOperatorBlockComparatorStrength::new);
        // list
        register("psi:" + LibPieceNames.OPERATOR_LIST_EXCLUSION, PieceOperatorListExclusion::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_INTERSECTION, PieceOperatorListIntersection::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_UNION, PieceOperatorListUnion::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_SIZE, PieceOperatorListSize::new);
        register("psi:" + LibPieceNames.OPERATOR_LIST_INDEX, PieceOperatorListIndex::new);
        // block operators deferred — see Phase 4 block pass

        // Selectors
        register("psi:" + LibPieceNames.SELECTOR_CASTER, PieceSelectorCaster::new);
        register("psi:selector_raycast", PieceSelectorRaycast::new);
        register("psi:selector_entity_position", PieceSelectorEntityPosition::new);
        register("psi:" + LibPieceNames.SELECTOR_FOCAL_POINT, PieceSelectorFocalPoint::new);
        register("psi:" + LibPieceNames.SELECTOR_LOOPCAST_INDEX, PieceSelectorLoopcastIndex::new);
        register("psi:" + LibPieceNames.SELECTOR_SNEAK_STATUS, PieceSelectorSneakStatus::new);
        register("psi:" + LibPieceNames.SELECTOR_TIME, PieceSelectorTime::new);
        register("psi:" + LibPieceNames.SELECTOR_TICK_TIME, PieceSelectorTickTime::new);
        register("psi:" + LibPieceNames.SELECTOR_TPS, PieceSelectorTps::new);
        register("psi:" + LibPieceNames.SELECTOR_ATTACKER, PieceSelectorAttacker::new);
        register("psi:" + LibPieceNames.SELECTOR_ATTACK_TARGET, PieceSelectorAttackTarget::new);
        register("psi:" + LibPieceNames.SELECTOR_DAMAGE_TAKEN, PieceSelectorDamageTaken::new);
        register("psi:" + LibPieceNames.SELECTOR_SAVED_VECTOR, PieceSelectorSavedVector::new);
        register("psi:" + LibPieceNames.SELECTOR_BLOCK_PRESENCE, PieceSelectorBlockPresence::new);
        register("psi:" + LibPieceNames.SELECTOR_ITEM_PRESENCE, PieceSelectorItemPresence::new);
        register("psi:" + LibPieceNames.SELECTOR_BLOCK_BROKEN, PieceSelectorBlockBroken::new);
        register("psi:" + LibPieceNames.SELECTOR_BLOCK_SIDE_BROKEN, PieceSelectorBlockSideBroken::new);
        register("psi:" + LibPieceNames.SELECTOR_EIDOS_CHANGELOG, PieceSelectorEidosChangelog::new);
        register("psi:" + LibPieceNames.SELECTOR_ITEM_COUNT, PieceSelectorItemCount::new);
        register("psi:" + LibPieceNames.SELECTOR_RULER_VECTOR, PieceSelectorRulerVector::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_ANIMALS, PieceSelectorNearbyAnimals::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_ENEMIES, PieceSelectorNearbyEnemies::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_ITEMS, PieceSelectorNearbyItems::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_LIVING, PieceSelectorNearbyLiving::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_PLAYERS, PieceSelectorNearbyPlayers::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_FALLING_BLOCKS, PieceSelectorNearbyFallingBlocks::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_GLOWING, PieceSelectorNearbyGlowing::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_PROJECTILES, PieceSelectorNearbyProjectiles::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_VEHICLES, PieceSelectorNearbyVehicles::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_SMELTABLES, PieceSelectorNearbySmeltables::new);
        register("psi:" + LibPieceNames.SELECTOR_CASTER_BATTERY, PieceSelectorCasterBattery::new);
        register("psi:" + LibPieceNames.SELECTOR_CASTER_ENERGY, PieceSelectorCasterEnergy::new);
        register("psi:" + LibPieceNames.SELECTOR_IS_ELYTRA_FLYING, PieceSelectorIsElytraFlying::new);
        register("psi:" + LibPieceNames.SELECTOR_NEARBY_CHARGES, PieceSelectorNearbyCharges::new);
        register("psi:" + LibPieceNames.SELECTOR_SUCCESS_COUNTER, PieceSelectorSuccessCounter::new);

        // Tricks
        register("psi:" + LibPieceNames.TRICK_DEBUG, PieceTrickDebug::new);
        register("psi:" + LibPieceNames.TRICK_BREAK_BLOCK, PieceTrickBreakBlock::new);
        register("psi:" + LibPieceNames.TRICK_EXPLODE, PieceTrickExplode::new);
        register("psi:" + LibPieceNames.TRICK_DIE, PieceTrickDie::new);
        register("psi:" + LibPieceNames.TRICK_EVALUATE, PieceTrickEvaluate::new);
        register("psi:" + LibPieceNames.TRICK_SAVE_VECTOR, PieceTrickSaveVector::new);
        register("psi:" + LibPieceNames.TRICK_CHANGE_SLOT, PieceTrickChangeSlot::new);
        register("psi:" + LibPieceNames.TRICK_DELAY, PieceTrickDelay::new);
        register("psi:" + LibPieceNames.TRICK_BLAZE, PieceTrickBlaze::new);
        register("psi:" + LibPieceNames.TRICK_OVERGROW, PieceTrickOvergrow::new);
        register("psi:" + LibPieceNames.TRICK_PLAY_SOUND, PieceTrickPlaySound::new);
        register("psi:" + LibPieceNames.TRICK_TILL, PieceTrickTill::new);
        register("psi:" + LibPieceNames.TRICK_PLACE_BLOCK, PieceTrickPlaceBlock::new);
        register("psi:" + LibPieceNames.TRICK_BREAK_IN_SEQUENCE, PieceTrickBreakInSequence::new);
        register("psi:" + LibPieceNames.TRICK_COLLAPSE_BLOCK, PieceTrickCollapseBlock::new);
        register("psi:" + LibPieceNames.TRICK_MOVE_BLOCK, PieceTrickMoveBlock::new);
        register("psi:" + LibPieceNames.TRICK_CONJURE_BLOCK, PieceTrickConjureBlock::new);
        register("psi:" + LibPieceNames.TRICK_ADD_MOTION, PieceTrickAddMotion::new);
        register("psi:" + LibPieceNames.TRICK_BLINK, PieceTrickBlink::new);
        register("psi:" + LibPieceNames.TRICK_IGNITE, PieceTrickIgnite::new);
        register("psi:" + LibPieceNames.TRICK_SMITE, PieceTrickSmite::new);
        register("psi:" + LibPieceNames.TRICK_TORRENT, PieceTrickTorrent::new);
        register("psi:" + LibPieceNames.TRICK_INFUSION, PieceTrickInfusion::new);
        register("psi:" + LibPieceNames.TRICK_GREATER_INFUSION, PieceTrickGreaterInfusion::new);
        register("psi:" + LibPieceNames.TRICK_EBONY_IVORY, PieceTrickEbonyIvory::new);
        register("psi:" + LibPieceNames.TRICK_SPEED, PieceTrickSpeed::new);
        register("psi:" + LibPieceNames.TRICK_HASTE, PieceTrickHaste::new);
        register("psi:" + LibPieceNames.TRICK_STRENGTH, PieceTrickStrength::new);
        register("psi:" + LibPieceNames.TRICK_FIRE_RESISTANCE, PieceTrickFireResistance::new);
        register("psi:" + LibPieceNames.TRICK_WATER_BREATHING, PieceTrickWaterBreathing::new);
        register("psi:" + LibPieceNames.TRICK_INVISIBILITY, PieceTrickInvisibility::new);
        register("psi:" + LibPieceNames.TRICK_JUMP_BOOST, PieceTrickJumpBoost::new);
        register("psi:" + LibPieceNames.TRICK_NIGHT_VISION, PieceTrickNightVision::new);
        register("psi:" + LibPieceNames.TRICK_REGENERATION, PieceTrickRegeneration::new);
        register("psi:" + LibPieceNames.TRICK_RESISTANCE, PieceTrickResistance::new);
        register("psi:" + LibPieceNames.TRICK_SLOWNESS, PieceTrickSlowness::new);
        register("psi:" + LibPieceNames.TRICK_WEAKNESS, PieceTrickWeakness::new);
        register("psi:" + LibPieceNames.TRICK_WITHER, PieceTrickWither::new);
    }
}

package vazkii.psi.api.spell;

/**
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/IErrorCatcher.java:10
 */
public interface IErrorCatcher {

    boolean catchException(SpellPiece errorPiece, SpellContext context, SpellRuntimeException exception);

    Object supplyReplacementValue(SpellPiece errorPiece, SpellContext context, SpellRuntimeException exception);

    boolean catchParam(SpellParam<?> param);
}

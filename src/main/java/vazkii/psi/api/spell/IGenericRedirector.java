package vazkii.psi.api.spell;

/**
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/IGenericRedirector.java:10
 * Modern uses Redirector logic for SpellCompiler. GTNH stub: single method remapSide.
 */
public interface IGenericRedirector {

    SpellParam.Side remapSide(SpellParam.Side inputSide);
}

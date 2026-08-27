package vazkii.psi.api.spell;

/**
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/IRedirector.java:10
 */
public interface IRedirector extends IGenericRedirector {

    SpellParam.Side getRedirectionSide();

    @Override
    default SpellParam.Side remapSide(SpellParam.Side inputSide) {
        return getRedirectionSide();
    }
}

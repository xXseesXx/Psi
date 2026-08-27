package vazkii.psi.api.cad;

import java.util.Locale;

/** An enum defining all types of CAD components. */
public enum EnumCADComponent {
    /** If you define an item using this component, it must implement ICADAssembly. */
    ASSEMBLY,
    CORE,
    SOCKET,
    BATTERY,
    /** If you define an item using this component, it must implement ICADColorizer. */
    DYE;

    public String getName() {
        return "psi.component." + name().toLowerCase(Locale.ROOT);
    }
}

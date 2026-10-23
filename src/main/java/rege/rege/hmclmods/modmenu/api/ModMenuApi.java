package rege.rege.hmclmods.modmenu.api;

import java.util.Collections;
import java.util.Map;

/**
 * Main API entrypoint for HMCL Mod Menu integration, inspired by Minecraft's
 * Mod Menu.
 */
public interface ModMenuApi {
    /**
     * Returns the configuration screen factory for this mod.
     * @return a factory creating the configuration screen, or {@code null} if
     * none.
     */
    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> null;
    }

    /**
     * Used for providing configuration screen factories for other mods.
     * @return a map of mod ID to {@link ConfigScreenFactory}.
     */
    default Map<String, ConfigScreenFactory<?>>
    getProvidedConfigScreenFactories() {
        return Collections.emptyMap();
    }
}

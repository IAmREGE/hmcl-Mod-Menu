package rege.rege.hmclmods.modmenu.api;

import javafx.scene.Node;

/**
 * Factory for creating a configuration screen / pane for a mod.
 * @param <T> the type of JavaFX node (e.g. Region, DialogPane, or Node)
 */
@FunctionalInterface
public interface ConfigScreenFactory<T extends Node> {
    /**
     * Creates the configuration screen node to be displayed.
     * @param parent the parent node (e.g. the
     * {@link rege.rege.hmclmods.modmenu.ui.ModViewerPage} instance)
     * @return the configuration node to display (DialogPane or Region/Node to
     * navigate to), or {@code null} if the factory handled presentation
     * directly.
     */
    T create(Node parent);
}

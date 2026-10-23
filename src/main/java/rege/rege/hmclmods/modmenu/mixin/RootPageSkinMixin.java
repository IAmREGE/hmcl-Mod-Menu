package rege.rege.hmclmods.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import rege.rege.hmclmods.modmenu.ui.ModViewerPage;
import javafx.scene.Node;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.construct.AdvancedListBox;
import org.jackhuang.hmcl.ui.main.RootPage;
import org.jackhuang.hmcl.util.logging.Logger;

/**
 * Mixin that hooks into HMCL's main window skin to add a "Fabric 模组"
 * navigation item under the "通用" category in the left navigation sidebar.
 */
@Pseudo
@Mixin(targets = "org.jackhuang.hmcl.ui.main.RootPage$Skin", remap = false)
public abstract class RootPageSkinMixin {
    @Inject(
        method = "<init>(Lorg/jackhuang/hmcl/ui/main/RootPage;)V",
        at = @At("RETURN"), remap = false, require = 0
    )
    private void onInit(RootPage control, CallbackInfo ci) {
        try {
            if (control != null && control.getLeft() != null &&
                !control.getLeft().getChildren().isEmpty()) {
                Node leftNode = control.getLeft().getChildren().get(0);
                if (leftNode instanceof AdvancedListBox) {
                    String itemTitle = ModViewerPage.tr(
                        "modmenu.entry",
                        FabricLoader.getInstance().getAllMods().size()
                    );
                    ((AdvancedListBox)leftNode).addNavigationDrawerItem(
                        itemTitle, SVG.EXTENSION,
                        () -> Controllers.navigate(new ModViewerPage())
                    );
                }
            }
        } catch (Throwable t) {
            Logger.LOG.error(
                "[HMCL-ModMenu] Failed to register Fabric Mods navigation item", t
            );
        }
    }
}

package rege.rege.hmclmods.modmenu.ui;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.api.metadata.Person;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.construct.ComponentList;
import org.jackhuang.hmcl.ui.construct.DialogPane;
import org.jackhuang.hmcl.ui.construct.LineButton;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jackhuang.hmcl.ui.construct.SpinnerPane;
import org.jackhuang.hmcl.ui.main.AboutPage;
import org.jackhuang.hmcl.ui.main.FeedbackPage;
import org.jackhuang.hmcl.util.i18n.I18n;
import org.jetbrains.annotations.PropertyKey;
import rege.rege.hmclmods.modmenu.api.ConfigScreenFactory;
import rege.rege.hmclmods.modmenu.api.ModMenuApi;

/**
 * Lightweight Mod Viewer page for HMCL Fabric Loader mods, inspired by MC's
 * Mod Menu.
 */
public class ModViewerPage extends SpinnerPane {
    private final ComponentList componentList = new ComponentList();
    private final Label countLabel = new Label();
    private final List<ModContainer> allMods;
    private final Map<String, ConfigScreenFactory<?>> configFactories =
    new HashMap<>();

    public static String tr(
        @PropertyKey(resourceBundle = "assets.mod-menu.lang.I18N") String key,
        Object... args
    ) {
        return args.length == 0 ? I18n.i18n(key) : I18n.i18n(key, args);
    }

    public ModViewerPage() {
        VBox content = new VBox(14);
        content.getStyleClass().add("spinner-pane-content");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        FXUtils.smoothScrolling(scrollPane);
        this.setContent(scrollPane);

        // Fetch all loaded Fabric mods, sorting user mods first, then builtin
        // components
        this.allMods =
        new ArrayList<>(FabricLoader.getInstance().getAllMods());
        this.allMods.sort((a, b) -> {
            boolean aBuiltin = isBuiltin(a.getMetadata().getId());
            boolean bBuiltin = isBuiltin(b.getMetadata().getId());
            return aBuiltin != bBuiltin ? (aBuiltin ? 1 : -1) :
                   a.getMetadata().getName()
                   .compareToIgnoreCase(b.getMetadata().getName());
        });

        // 1. Top Bar: Title & "Open Mods Folder" button
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(tr("modmenu.title"));
        title.setStyle(
"-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -monet-on-surface;"
        );

        this.countLabel.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: -monet-on-surface-variant;"
        );

        VBox titleBox = new VBox(2, title, this.countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        JFXButton openFolderBtn =
        new JFXButton(tr("modmenu.modsFolder"));
        openFolderBtn.getStyleClass().add("button-raised");
        openFolderBtn.setOnAction(e -> {
            Path modsDir =
            FabricLoader.getInstance().getGameDir().resolve("mods");
            try {
                Files.createDirectories(modsDir);
                FXUtils.openFolder(modsDir);
            } catch (Exception ex) {
                Controllers.dialog(ex.getMessage(), "Error");
            }
        });

        topBar.getChildren().addAll(titleBox, openFolderBtn);
        content.getChildren().add(topBar);

        // 2. Search Field
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText(tr("modmenu.search"));
        searchField.setStyle("-fx-pref-height: 34px; -fx-font-size: 13px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty()
        .addListener((obs, oldVal, newVal) -> this.refreshModList(newVal));

        searchContainer.getChildren().add(searchField);
        content.getChildren().add(searchContainer);

        // 3. Mod List Section
        content.getChildren().add(ComponentList.createComponentListTitle(
            tr("modmenu.section.loaded")
        ));
        content.getChildren().add(this.componentList);

        this.loadConfigScreenFactories();
        this.refreshModList("");
    }

    private void refreshModList(String query) {
        this.componentList.getContent().clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        List<ModContainer> filtered = this.allMods.stream().filter(m -> {
            if (q.isEmpty()) {
                return true;
            }
            ModMetadata meta = m.getMetadata();
            return meta.getName().toLowerCase(Locale.ROOT).contains(q) ||
                   meta.getId().toLowerCase(Locale.ROOT).contains(q) ||
                   meta.getDescription().toLowerCase(Locale.ROOT).contains(q);
        }).collect(Collectors.toList());

        this.countLabel.setText(q.isEmpty() ? tr(
            "modmenu.loadedMods", this.allMods.size()
        ) : tr(
            "modmenu.loadedMods.matching", this.allMods.size(), filtered.size()
        ));

        if (filtered.isEmpty()) {
            LineButton emptyItem = new LineButton();
            emptyItem.setTitle(tr("modmenu.empty.title"));
            emptyItem.setSubtitle(tr("modmenu.empty.subtitle"));
            emptyItem.setLeading(SVG.ERROR);
            this.componentList.getContent().add(emptyItem);
            return;
        }

        for (ModContainer mod : filtered) {
            ModMetadata meta = mod.getMetadata();
            String modId = meta.getId();
            LineButton item = new LineButton();
            item.setLargeTitle(true);
            item.setTitle(meta.getName());

            String desc = meta.getDescription();
            if (desc != null) {
                item.setSubtitle(desc);
            }
            item.setTrailingText("v" + meta.getVersion().getFriendlyString());
            item.setTrailingIcon(SVG.ARROW_FORWARD);

            // Leading icon resolution:
            // 1. Try custom icon declared in fabric.mod.json
            boolean iconLoaded = false;
            Optional<String> iconPath = meta.getIconPath(32);
            if (iconPath.isPresent()) {
                Optional<Path> p = mod.findPath(iconPath.get());
                if (p.isPresent()) {
                    try (InputStream in = Files.newInputStream(p.get())) {
                        Image img = new Image(in, 32, 32, true, true);
                        if (!img.isError()) {
                            item.setLeading(img);
                            iconLoaded = true;
                        }
                    } catch (Exception ignored) {}
                }
            }
            boolean isHmcl = "hmcl".equals(modId);

            // 2. Fallback for HMCL host application (builtin mod)
            if (!iconLoaded && isHmcl) {
                try (InputStream in = ModViewerPage.class.getResourceAsStream(
                    "/assets/img/icon.png"
                )) {
                    if (in != null) {
                        Image img = new Image(in, 32, 32, true, true);
                        if (!img.isError()) {
                            item.setLeading(img);
                            iconLoaded = true;
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 3. Fallback to Material SVG icons
            if (!iconLoaded) {
                item.setLeading("mod-menu".equals(modId) ? SVG.LIST : (
                    isBuiltin(modId) ? SVG.DEPLOYED_CODE : SVG.EXTENSION
                ));
            }

            // 4. Configuration action button (gear icon)
            ConfigScreenFactory<?> configFactory =
            this.configFactories.get(modId);
            if (configFactory != null) {
                JFXButton gearBtn = new JFXButton();
                gearBtn.setGraphic(SVG.SETTINGS.createIcon(16.));
                gearBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                gearBtn.getStyleClass().add("toggle-icon-tiny");
                gearBtn.setFocusTraversable(false);
                gearBtn.setCursor(Cursor.HAND);
                gearBtn.setTooltip(new Tooltip(
                    isHmcl ? tr("modmenu.settings.launcher") :
                    tr("modmenu.settings.mod")
                ));
                gearBtn
                .addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
                gearBtn.setOnAction(e -> {
                    e.consume();
                    this.openConfig(modId, configFactory);
                });
                item.setTitleTrailing(gearBtn);
            }

            // Click action to view full details
            item.setOnAction(e -> this.showModDetails(mod));

            this.componentList.getContent().add(item);
        }
    }

    private void showModDetails(ModContainer mod) {
        ModMetadata meta = mod.getMetadata();
        String modId = meta.getId();
        StringBuilder sb = new StringBuilder();

        sb.append(escapeXml(tr("modmenu.details.title", meta.getName())))
          .append('\n');
        sb.append(escapeXml(tr("modmenu.details.id", modId))).append('\n');
        sb.append(escapeXml(tr(
            "modmenu.details.version", meta.getVersion().getFriendlyString()
        ))).append('\n');

        boolean builtin = isBuiltin(modId);
        sb.append(escapeXml(tr("modmenu.details.type")))
          .append(escapeXml(tr(builtin ? "modmenu.details.type.builtin" :
                            "modmenu.details.type.external"))).append('\n');

        Collection<Person> persons = meta.getAuthors();
        if (!persons.isEmpty()) {
            sb.append(escapeXml(tr("modmenu.details.authors")))
              .append(persons.stream().map(ModViewerPage::getPersonXml)
                      .collect(Collectors.joining(", ")))
              .append('\n');
        }
        persons = meta.getContributors();
        if (!persons.isEmpty()) {
            sb.append(escapeXml(tr("modmenu.details.contributors")))
              .append(persons.stream().map(ModViewerPage::getPersonXml)
                      .collect(Collectors.joining(", ")))
              .append('\n');
        }
        Collection<String> licenses = meta.getLicense();
        if (!licenses.isEmpty()) {
            sb.append(escapeXml(tr("modmenu.details.license")))
              .append(escapeXml(String.join(", ", licenses))).append('\n');
        }

        String desc = meta.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            sb.append('\n').append(escapeXml(tr("modmenu.details.description")))
              .append(escapeXml(desc.trim())).append('\n');
        }

        ContactInformation contact = meta.getContact();
        String homepage =
        contact.get("homepage").map(ModViewerPage::normalizeUrl).orElse(null);
        String sources =
        contact.get("sources").map(ModViewerPage::normalizeUrl).orElse(null);
        String issues =
        contact.get("issues").map(ModViewerPage::normalizeUrl).orElse(null);

        if (homepage != null) {
            sb.append('\n').append(escapeXml(tr("modmenu.details.homepage")))
              .append("<a href=\"").append(escapeXml(homepage)).append("\">")
              .append(escapeXml(homepage)).append("</a>");
        }
        if (sources != null) {
            sb.append('\n').append(escapeXml(tr("modmenu.details.sources")))
              .append("<a href=\"").append(escapeXml(sources)).append("\">")
              .append(escapeXml(sources)).append("</a>");
        }
        if (issues != null) {
            sb.append('\n').append(escapeXml(tr("modmenu.details.issues")))
              .append("<a href=\"").append(escapeXml(issues)).append("\">")
              .append(escapeXml(issues)).append("</a>");
        }

        try {
            ModOrigin origin = mod.getOrigin();
            if (origin != null) {
                if (origin.getKind() == ModOrigin.Kind.PATH) {
                    List<Path> paths = origin.getPaths();
                    if (paths != null && !paths.isEmpty()) {
                        sb.append("\n\n")
                          .append(escapeXml(tr("modmenu.details.filepath")))
                          .append(paths.stream().map(Path::toString)
                                  .map(ModViewerPage::escapeXml)
                                  .collect(Collectors.joining("\n")));
                    }
                } else if (origin.getKind() == ModOrigin.Kind.NESTED) {
                    sb.append("\n\n").append(escapeXml(tr(
                        "modmenu.details.nested", origin.getParentModId(),
                        origin.getParentSubLocation()
                    )));
                }
            }
        } catch (Throwable ignored) {}

        MessageDialogPane.Builder builder = new MessageDialogPane.Builder(
            sb.toString(), meta.getName() + " (" + modId + ')',
            MessageDialogPane.MessageType.INFO
        );

        boolean isHmcl = "hmcl".equals(modId);
        if (homepage != null) {
            builder.addHyperLink(tr("modmenu.details.homepage.btn"), homepage);
        } else if (sources != null) {
            builder.addHyperLink(tr("modmenu.details.homepage.btn"), sources);
        }
        if (isHmcl) {
            builder.addAction(
                tr("modmenu.details.issues.btn"),
                () -> Controllers.navigate(new FeedbackPage())
            );
            builder.addAction(
                tr("modmenu.details.contributors.btn"),
                () -> Controllers.navigate(new AboutPage())
            );
        } else if (issues != null) {
            builder.addHyperLink(tr("modmenu.details.issues.btn"), issues);
        }

        ConfigScreenFactory<?> configFactory =
        this.configFactories.get(modId);
        if (configFactory != null) {
            String text = isHmcl ? tr("modmenu.settings.launcher") :
                          tr("modmenu.settings.mod");
            JFXButton configBtn = new JFXButton(text);
            configBtn.setGraphic(SVG.SETTINGS.createIcon(16.));
            configBtn.getStyleClass().add("dialog-accept");
            configBtn
            .setOnAction(Icone -> this.openConfig(modId, configFactory));
            builder.addAction(configBtn);
        }

        builder.ok(null);
        Controllers.dialog(builder.build());
    }

    private void openConfig(String modId, ConfigScreenFactory<?> factory) {
        try {
            Node screen = factory.create(this);
            if (screen instanceof DialogPane) {
                Controllers.dialog((DialogPane)screen);
            } else if (screen != null) {
                Controllers.navigate(screen);
            }
        } catch (Throwable t) {
            Controllers.dialog(
                tr("modmenu.config.error.title"),
                t.getMessage() != null ? t.getMessage() : t.toString()
            );
        }
    }

    private void loadConfigScreenFactories() {
        this.configFactories.clear();

        // 1. Built-in config screen for HMCL host application (like Minecraft
        //    vanilla options in Mod Menu)
        this.configFactories.put("hmcl", parent -> {
            Controllers.navigate(Controllers.getSettingsPage());
            return null;
        });

        // 2. Discover ModMenuApi entrypoints from Fabric Loader
        List<EntrypointContainer<ModMenuApi>> entrypoints = new ArrayList<>();
        try {
            entrypoints.addAll(
                FabricLoader.getInstance().getEntrypointContainers(
                    "hmcl-mod-menu", ModMenuApi.class
                )
            );
        } catch (Throwable ignored) {}
        try {
            entrypoints.addAll(
                FabricLoader.getInstance().getEntrypointContainers(
                    "modmenu", ModMenuApi.class
                )
            );
        } catch (Throwable ignored) {}

        for (EntrypointContainer<ModMenuApi> container : entrypoints) {
            try {
                ModMenuApi api = container.getEntrypoint();
                String modId = container.getProvider().getMetadata().getId();
                ConfigScreenFactory<?> factory =
                api.getModConfigScreenFactory();
                if (factory != null) {
                    this.configFactories.put(modId, factory);
                }
                Map<String, ConfigScreenFactory<?>> provided =
                api.getProvidedConfigScreenFactories();
                if (provided != null) {
                    this.configFactories.putAll(provided);
                }
            } catch (Throwable ignored) {}
        }
    }

    private static String getPersonXml(Person person) {
        String name = person.getName();
        ContactInformation contact = person.getContact();
        Optional<String> url = contact.get("homepage");
        if (url.isPresent()) {
            return "<a href=\"" + escapeXml(normalizeUrl(url.get())) + "\">" +
                   escapeXml(name) + "</a>";
        }
        url = contact.get("email");
        if (url.isPresent()) {
            return "<a href=\"" + escapeXml(normalizeUrl(url.get())) + "\">" +
                   escapeXml(name) + "</a>";
        }
        return escapeXml(name);
    }

    private static String escapeXml(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        String u = url.trim();
        if (u.isEmpty()) {
            return null;
        }
        return u.startsWith("http://") || u.startsWith("https://") ||
               u.startsWith("mailto:") ? u : "https://" + u;
    }

    private static boolean isBuiltin(String id) {
        return "hmcl".equals(id) || "java".equals(id) ||
               "fabricloader".equals(id) || "mixinextras".equals(id);
    }
}

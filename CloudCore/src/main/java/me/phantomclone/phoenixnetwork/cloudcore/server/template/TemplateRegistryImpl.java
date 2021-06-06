/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server.template;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class TemplateRegistryImpl implements TemplateRegistry {

    private final List<Template> templates;

    public static TemplateRegistryImpl create() { return new TemplateRegistryImpl(); }

    private TemplateRegistryImpl() {
        this.templates = new ArrayList<>();
    }

    @Override
    public List<Template> getLoadedTemplates() {
        return templates;
    }

    @Override
    public boolean loadTemplate(@NonNull String name) {
        Objects.requireNonNull(name);
        if (getTemplate(name) != null) return true;
        Template template = new Template() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getWrapperName() {
                return null;
            }

            @Override
            public TemplateType getTemplateType() {
                return null;
            }

            @Override
            public ServerType getServerType() {
                return null;
            }

            @Override
            public int getStartPort() {
                return 0;
            }

            @Override
            public int getEndPort() {
                return 0;
            }

            @Override
            public int getMinServer() {
                return 0;
            }

            @Override
            public int getMinMemory() {
                return 0;
            }

            @Override
            public int getMaxMemory() {
                return 0;
            }
        };
        if (template.exist()) {
            this.templates.add(template.loadTemplate());
            return true;
        }
        return false;
    }

    @Override
    public boolean unloadTemplate(@NonNull String templateName) {
        Objects.requireNonNull(templateName);
        Template template = getTemplate(templateName);
        if (template == null) return false;
        this.templates.remove(template);
        return true;
    }

    @Override
    public void loadTemplates() {
        File folder = new File("./templates/");
        if (!folder.exists()) folder.mkdirs();
        if (folder.listFiles() == null) return;
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".json")) {
                loadTemplate(file.getName().replace(".json", ""));
            }
        }
    }

    @Override
    public Template getTemplate(@NonNull String name) {
        Objects.requireNonNull(name);
        return this.templates.stream().filter(template -> template.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public boolean saveTemplate(@NonNull String name) {
        Objects.requireNonNull(name);
        Template template = getTemplate(name);
        if (template == null) return false;
        template.save();
        return true;
    }

    @Override
    public void saveTemplates() {
        this.templates.forEach(template -> saveTemplate(template.getName()));
    }

    @Override
    public String getTemplateFolder() {
        return "./templates/";
    }
}
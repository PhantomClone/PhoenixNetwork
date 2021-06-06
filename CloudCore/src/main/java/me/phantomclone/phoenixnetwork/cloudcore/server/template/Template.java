/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server.template;

import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;

import java.io.File;
import java.util.Arrays;

/**
 * <p>A interface that represents a storage of a template data.</p>
 *
 * <p>It have some default methode to save and load templates.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Template {

    /**
     * Returns the name of the template.
     * @return Returns the name of the template.
     */
    String getName();

    /**
     * Returns the name wrapper of the template.
     * @return Returns the name wrapper of the template.
     */
    String getWrapperName();

    /**
     * Returns the {@link TemplateType} of the template.
     * @return Returns the {@link TemplateType} of the template.
     */
    TemplateType getTemplateType();

    /**
     * Returns the {@link ServerType} of the template.
     * @return Returns the {@link ServerType} of the template.
     */
    ServerType getServerType();

    /**
     * Return the start port of the template.
     * @return Return the start port of the template.
     */
    int getStartPort();

    /**
     * Return the start port of the template.
     * @return Return the start port of the template.
     */
    int getEndPort();

    /**
     * Return the end port of the template.
     * @return Return the end port of the template.
     */
    int getMinServer();

    /**
     * Return the min memory amount per server of the template.
     * @return Return the min memory amount per server of the template.
     */
    int getMinMemory();

    /**
     * Return the max memory amount per server of the template.
     * @return Return the max memory amount per server of the template.
     */
    int getMaxMemory();

    /**
     * Default methode to save the template with a {@link net.ghastgames.ghastcloud.cloudlib.config.Config}.
     */
    default void save() {
        ConfigImpl config = ConfigImpl.create();
        config.set("name", getName());
        config.set("wrapperName", getWrapperName());
        config.set("templateType", getTemplateType());
        config.set("serverType", getServerType());
        config.set("startPort", getStartPort());
        config.set("endPort", getEndPort());
        config.set("minServer", getMinServer());
        config.set("minMemory", getMinMemory());
        config.set("maxMemory", getMaxMemory());
        config.save(new File("./templates/" + getName() + ".json"));
    }

    /**
     * Default methode to load and return the template with a {@link net.ghastgames.ghastcloud.cloudlib.config.Config}.
     * @return Returns the loaded template if it exist else it return null.
     */
    default Template loadTemplate() {
        ConfigImpl config = ConfigImpl.create();
        File file = new File("./templates/" + getName() + ".json");
        if (!file.exists()) return null;
        config.read(file);

        String name = config.get("name").toString();
        String wrapperName = config.get("wrapperName").toString();
        TemplateType templateType = TemplateType.valueOf(config.get("templateType").toString());
        ServerType serverType = ServerType.valueOf(config.get("serverType").toString());
        int startPort = (int) (double) config.get("startPort");
        int endPort = (int) (double) config.get("endPort");
        int minServer = (int) (double) config.get("minServer");
        int minMemory = (int) (double) config.get("minMemory");
        int maxMemory = (int) (double) config.get("maxMemory");

        return new Template() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getWrapperName() {
                return wrapperName;
            }

            @Override
            public TemplateType getTemplateType() {
                return templateType;
            }

            @Override
            public ServerType getServerType() {
                return serverType;
            }

            @Override
            public int getStartPort() {
                return startPort;
            }

            @Override
            public int getEndPort() {
                return endPort;
            }

            @Override
            public int getMinServer() {
                return minServer;
            }

            @Override
            public int getMinMemory() {
                return minMemory;
            }

            @Override
            public int getMaxMemory() {
                return maxMemory;
            }
        };
    }

    /**
     * A default methode to delete the template file, it will be still loaded!
     */
    default void delete() {
        File file = new File("./templates/" + getName() + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Returns if the template file exist.
     * @return Return if the template file exist.
     */
    default boolean exist() {
        File folder = new File("./templates/");
        if (!folder.exists()) {
            folder.mkdirs();
            return false;
        }
        if (folder.listFiles() == null) return false;
        return Arrays.stream(folder.listFiles()).anyMatch(file -> file.getName().toLowerCase().equalsIgnoreCase(getName().toLowerCase() + ".json"));
    }

}
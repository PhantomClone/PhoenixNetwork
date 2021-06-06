/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server.template;

import lombok.NonNull;

import java.util.List;

/**
 * <p>A interface that represents a registry for the templates.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface TemplateRegistry {

    /**
     * Returns all loaded templates.
     * @return Returns all loaded templates.
     */
    List<Template> getLoadedTemplates();

    /**
     * Load a specific template. Returns false when the template is already loaded or it do not exist else it returns true.
     * @param name Search with the non-null name for the template.
     * @return Returns false when the template is already loaded or it do not exist else it returns true.
     */
    boolean loadTemplate(@NonNull String name);

    /**
     * Unload a specific templated. Returns false when the template is not loaded, else the template will be remove out of this registry and it returns true.
     * @param name Search with the non-null name for the template.
     * @return Returns true when the found template could be successfully unloaded, else it returns false.
     */
    boolean unloadTemplate(@NonNull String name);

    /**
     * Load all template which exist.
     */
    void loadTemplates();

    /**
     * Search and returns a specific template. If the template is not loaded it returns null.
     * @param name Search with the non-null name for the template.
     * @return Returns a specific template. If the template is not loaded it returns null.
     */
    Template getTemplate(@NonNull String name);

    /**
     * Search and save the found template. If the template is not found it returns false. If it can not be saved it returns also false, else it returns true.
     * @param name Search with the non-null name for the template.
     * @return If the template is not found it returns false. If it can not be saved it returns also false, else it returns true.
     */
    boolean saveTemplate(@NonNull String name);

    /**
     * Tries to save all loaded templates.
     */
    void saveTemplates();

    /**
     * Returns the template folder path as String.
     * @return Returns the template folder path as String.
     */
    String getTemplateFolder();
}
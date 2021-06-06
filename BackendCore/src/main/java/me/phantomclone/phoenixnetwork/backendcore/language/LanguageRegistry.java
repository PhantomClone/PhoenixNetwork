package me.phantomclone.phoenixnetwork.backendcore.language;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface LanguageRegistry {

    Language getLanguage(LanguageEnum languageEnum, String pluginName);

    String getMessage(LanguageEnum languageEnum, String pluginName, int id, Object... objects);

    void unload(String pluginName);

}

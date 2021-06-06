package me.phantomclone.phoenixnetwork.backendcore.language;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class LanguageRegistryImpl implements LanguageRegistry {

    private final Map<String, Language> languageMap = new HashMap<>();

    public static LanguageRegistryImpl create() {
        return new LanguageRegistryImpl();
    }

    private LanguageRegistryImpl() {}

    @Override
    public Language getLanguage(LanguageEnum languageEnum, String pluginName) {
        Language language = this.languageMap.get(pluginName.toLowerCase() + "[" + languageEnum.name() + "]");
        if (language == null) {
            language = new Language();
            this.languageMap.put(pluginName.toLowerCase() + "[" + languageEnum.name() + "]", language);
        }
        return language;
    }

    @Override
    public String getMessage(LanguageEnum languageEnum, String pluginName, int id, Object... objects) {
        Language language = this.languageMap.get(pluginName.toLowerCase() + "[" + languageEnum.name() + "]");
        return language == null ? "" : language.getMessage(id, objects);
    }

    @Override
    public void unload(String pluginName) {
        languageMap.keySet().stream().filter(s -> s.startsWith(pluginName.toLowerCase())).collect(Collectors.toList()).forEach(languageMap::remove);
    }
}

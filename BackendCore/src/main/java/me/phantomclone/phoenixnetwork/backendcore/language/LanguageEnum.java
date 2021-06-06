package me.phantomclone.phoenixnetwork.backendcore.language;

import java.util.Arrays;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public enum LanguageEnum {

    DE(0), EN(1);

    private final int id;

    LanguageEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LanguageEnum getLanguageOfId(int id) {
        return Arrays.stream(values()).filter(language -> language.getId() == id).findFirst().orElse(DE);
    }
}

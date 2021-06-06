package me.phantomclone.phoenixnetwork.backendspigot.storage;

import com.google.common.collect.Lists;
import me.phantomclone.phoenixnetwork.backendcore.language.LanguageEnum;
import me.phantomclone.phoenixnetwork.backendcore.storage.Storable;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageType;

import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Storable(storageType = StorageType.PROXYMINECRAFT)
public class BasicData {

    private String name;
    private long firstLogin, lastLogin, playtime;
    private List<String> nameHistory;
    private int language;

    public BasicData() {}

    public BasicData(String name, long firstLogin, long lastLogin, long playtime, LanguageEnum language) {
        this.name = name;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
        this.playtime = playtime;
        this.nameHistory = Lists.newArrayList();
        this.language = language.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(long firstLogin) {
        this.firstLogin = firstLogin;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public List<String> getNameHistory() {
        return nameHistory;
    }

    public void setNameHistory(List<String> nameHistory) {
        this.nameHistory = nameHistory;
    }

    public LanguageEnum getLanguage() {
        return LanguageEnum.getLanguageOfId(this.language);
    }

    public void setLanguage(LanguageEnum language) {
        this.language = language.getId();
    }
}

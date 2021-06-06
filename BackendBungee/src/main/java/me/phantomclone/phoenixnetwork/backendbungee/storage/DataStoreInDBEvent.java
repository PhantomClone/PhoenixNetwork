package me.phantomclone.phoenixnetwork.backendbungee.storage;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DataStoreInDBEvent extends Event {

    private final UUID uuid;
    private final String className;
    private final Object object;

    public DataStoreInDBEvent(UUID uuid, String className, Object object) {
        this.uuid = uuid;
        this.className = className;
        this.object = object;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getClassName() {
        return className;
    }

    public Object getObject() {
        return object;
    }

}

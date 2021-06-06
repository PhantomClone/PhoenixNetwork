package me.phantomclone.phoenixnetwork.backendspigot.storage;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DataLoadOutRedisEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final UUID uuid;
    private final String className;
    private final Object object;

    public DataLoadOutRedisEvent(UUID uuid, String className, Object object) {
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

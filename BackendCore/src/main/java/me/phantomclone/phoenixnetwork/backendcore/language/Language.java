package me.phantomclone.phoenixnetwork.backendcore.language;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class Language {

    private final Map<Integer, String> messages = new HashMap<>();

    public void setMessage(int id, String message) {
        this.messages.put(id, message);
    }

    public String getMessage(int id, Object... objects) {
        String message = this.messages.get(id);
        return message == null ? "" : objects == null || objects.length == 0 ? message : String.format(message, objects);
    }
}

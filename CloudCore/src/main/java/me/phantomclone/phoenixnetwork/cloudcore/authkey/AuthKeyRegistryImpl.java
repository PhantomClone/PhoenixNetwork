/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.authkey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class AuthKeyRegistryImpl implements AuthKeyRegistry {

    private final ArrayList<AuthKey> keys = new ArrayList<>();

    public static AuthKeyRegistryImpl create(){
        return new AuthKeyRegistryImpl();
    }

    private AuthKeyRegistryImpl() {}

    @Override
    public AuthKey getKey(UUID uuid) {
        Objects.requireNonNull(uuid);
        if (this.keys.stream().noneMatch(auth -> auth.getUUID().toString().equalsIgnoreCase(uuid.toString()))) {
            if (!new File("./authkeys/" + uuid.toString() + ".key").exists()) return null;

            AuthKey authKey = new AuthKey() {

                private final UUID id = uuid;
                private final File authKeyFile = new File("./authkeys/" + uuid.toString() + ".key");

                @Override
                public UUID getUUID() {
                    return id;
                }

                @Override
                public String getKeyString() throws IOException {
                    if (!this.authKeyFile.exists()) throw new IOException("File does not exist anymore!");
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(this.authKeyFile));
                    return bufferedReader.readLine();
                }
            };
            this.keys.add(authKey);
        }
        return this.keys.stream().filter(key -> key.getUUID().toString().equalsIgnoreCase(uuid.toString())).findFirst().orElse(null);
    }
}
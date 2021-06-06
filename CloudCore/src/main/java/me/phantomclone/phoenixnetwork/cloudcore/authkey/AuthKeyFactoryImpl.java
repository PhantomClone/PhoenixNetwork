/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.authkey;

import me.phantomclone.phoenixnetwork.cloudcore.config.SimpleFileWriter;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class AuthKeyFactoryImpl implements AuthKeyFactory, Utils, SimpleFileWriter {

    public static AuthKeyFactoryImpl create() {
        return new AuthKeyFactoryImpl();
    }

    private AuthKeyFactoryImpl() {}

    @Override
    public AuthKey createAuthKey(UUID uuid) {
        Objects.requireNonNull(uuid);
        File authKeyFile = new File("./authkeys/" + uuid.toString() + ".key");
        if (authKeyFile.exists()) {
            log("AuthKey File already exist!");
            return null;
        }else{
            log("Create new Authkey...");
        }
        AuthKey authKey = new AuthKey() {

            private final UUID id = uuid;
            private final String key = RandomStringUtils.randomAlphanumeric(120);

            @Override
            public UUID getUUID() {
                return id;
            }

            @Override
            public String getKeyString() throws IOException {
                return key;
            }
        };
        try {
            writeToFile(authKeyFile, authKey.getKeyString(), false);
            log("Created new AuthKey!");
        } catch (Exception e) {
            log("Failed creating AuthKey File!");
        }
        return authKey;
    }
}
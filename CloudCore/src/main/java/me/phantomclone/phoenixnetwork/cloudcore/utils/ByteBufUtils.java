/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <p>A interface that represents help for {@link ByteBuf}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ByteBufUtils {

    /**
     * Write the value as var int in the given {@link ByteBuf}.
     * @param value The value that is written into it.
     * @param byteBuf The non-null {@link ByteBuf} which buffered the value.
     */
    default void writeVarInt(int value, @NonNull ByteBuf byteBuf) {
        Objects.requireNonNull(byteBuf);
        byte part;
        do {
            part = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0)
                part |= 0x80;
            byteBuf.writeByte(part);
        } while (value != 0);
    }

    /**
     * Reads the var int and returns it out of the {@link ByteBuf}.
     * @param byteBuf The non-null {@link ByteBuf} which the var int get read out.
     * @return Reads the var int and returns it out of the {@link ByteBuf}.
     */
    default int readVarInt(@NonNull ByteBuf byteBuf) {
        Objects.requireNonNull(byteBuf);
        int out = 0, bytes = 0;
        byte part;
        do {
            part = byteBuf.readByte();
            out |= (part & 0x7F) << (bytes++ *7);
            if (bytes > 5)
                throw new DecoderException(String.format("VarInt is too long (%d > 5", bytes));
        } while ((part & 0x80) == 0x80);
        return out;
    }

    /**
     * Write the value as var int and byte array in the given {@link ByteBuf}.
     * @param string The string that is written into it.
     * @param byteBuf The non-null {@link ByteBuf} which buffered the value.
     */
    default void writeString(@NonNull String string, @NonNull ByteBuf byteBuf) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(byteBuf);
        byte[] values = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(values.length, byteBuf);
        byteBuf.writeBytes(values);
    }

    /**
     * Reads first the var int and then the byte array and but it in a new {@link String} with {@link StandardCharsets#UTF_8}.
     * @param byteBuf The non-null {@link ByteBuf} which the var int and the byte array get read out.
     * @return Returns the read String as var int and byte array packed together with {@link StandardCharsets#UTF_8}.
     */
    default String readString(@NonNull ByteBuf byteBuf) {
        Objects.requireNonNull(byteBuf);
        int integer = readVarInt(byteBuf);
        byte[] buffer = new byte[integer];
        byteBuf.readBytes(buffer, 0, integer);
        return new String(buffer, StandardCharsets.UTF_8);
    }
}

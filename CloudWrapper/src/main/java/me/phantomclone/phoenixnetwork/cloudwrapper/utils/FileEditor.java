/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface FileEditor {

    default void replaceLineWhereFind(File readFile, String findInLineContains, String newLine) throws Exception {
        BufferedReader file = new BufferedReader(new FileReader(readFile));
        StringBuffer inputBuffer = new StringBuffer();
        String line;

        while ((line = file.readLine()) != null) {
            inputBuffer.append(line.contains(findInLineContains) || line.equalsIgnoreCase(findInLineContains) ? newLine : line);
            inputBuffer.append('\n');
        }
        file.close();
        String inputStr = inputBuffer.toString();
        readFile.delete();
        readFile.createNewFile();
        FileOutputStream fileOut = new FileOutputStream(readFile);
        fileOut.write(inputStr.getBytes());
        fileOut.close();
    }

    default void replaceWordWhereFind(File readFile, String replaceWord, String newWord) throws Exception {
        BufferedReader file = new BufferedReader(new FileReader(readFile));
        StringBuffer inputBuffer = new StringBuffer();
        String line;

        while ((line = file.readLine()) != null) {
            inputBuffer.append(line.contains(replaceWord) || line.equalsIgnoreCase(replaceWord) ? line.replace(replaceWord, newWord) : line);
            inputBuffer.append('\n');
        }
        file.close();
        String inputStr = inputBuffer.toString();
        readFile.delete();
        readFile.createNewFile();
        FileOutputStream fileOut = new FileOutputStream(readFile);
        fileOut.write(inputStr.getBytes());
        fileOut.close();
    }

}

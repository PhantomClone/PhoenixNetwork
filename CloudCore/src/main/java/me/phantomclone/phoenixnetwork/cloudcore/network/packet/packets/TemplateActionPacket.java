/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.Packet;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Packet
public class TemplateActionPacket {

    @PacketValue
    String type;

    @PacketValue
    String name;
    @PacketValue
    String wrapperName;
    @PacketValue
    String templateType;
    @PacketValue
    String serverType;
    @PacketValue
    String startPort;
    @PacketValue
    String endPort;
    @PacketValue
    String minServer;
    @PacketValue
    String minMemory;
    @PacketValue
    String maxMemory;

    @PacketValue
    boolean templateFolder = false;
    @PacketValue
    private ArrayList<Byte> array = new ArrayList<>();

    public TemplateActionPacket() {}

    public TemplateActionPacket(Type type, Template template) {
        this.type = type.name();
        this.name = template.getName();
        this.wrapperName = template.getWrapperName();
        this.templateType = template.getTemplateType().name();
        this.serverType = template.getServerType().name();
        this.startPort = template.getStartPort() + "";
        this.endPort = template.getEndPort() + "";
        this.minServer = template.getMinServer() + "";
        this.minMemory = template.getMinMemory() + "";
        this.maxMemory = template.getMaxMemory() + "";
    }

    public boolean loadTemplateFolderInPacket(CloudLib cloudLib) {
        try {
            File file = new File(cloudLib.getTemplateRegistry().getTemplateFolder() + name + "/");
            zip(Arrays.asList(file.listFiles()), "./" + file.getName() + ".zip");
            byte[] array = new byte[0];
            array = IOUtils.toByteArray(new FileInputStream(new File("./" + file.getName() + ".zip").getPath()));
            for (byte b : array) {
                this.array.add(Byte.valueOf(b));
            }
            deleteDir(new File("./" + file.getName() + ".zip"));
            this.templateFolder = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void readAndSaveTemplateFolderFromPacket(CloudLib cloudLib) {
        String path = cloudLib.getTemplateRegistry().getTemplateFolder() + name + "\\";
        deleteDir(new File(path));
        try {
            new File(path).mkdirs();
            byte[] arrayy = new byte[this.array.size()];
            int index = 0;
            for (Object b : this.array) {
                arrayy[index] = Byte.parseByte((b+ "").split("\\.")[0]);
                index++;
            }
            FileUtils.writeByteArrayToFile(new File(new File(path, "copy.zip").getPath()), arrayy);
            unzip(new File(path, "copy.zip").getPath(), new File(path).getPath());
            deleteDir(new File(path, "copy.zip"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    void zip(List<File> listFiles, String destZipFile) throws FileNotFoundException, IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                zipFile(file, zos);
            }
        }
        zos.flush();
        zos.close();
    }

    void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[4096];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
    }

    void zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
    }
    private void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void deleteDir(File file) {
        if (!file.exists()) return;
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    public Type getType() {
        return Type.valueOf(type);
    }

    public Template getTemplate() {
        return new Template() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getWrapperName() {
                return wrapperName;
            }

            @Override
            public TemplateType getTemplateType() {
                return TemplateType.valueOf(templateType);
            }

            @Override
            public ServerType getServerType() {
                return ServerType.valueOf(serverType);
            }

            @Override
            public int getStartPort() {
                return Integer.parseInt(startPort);
            }

            @Override
            public int getEndPort() {
                return Integer.parseInt(endPort);
            }

            @Override
            public int getMinServer() {
                return Integer.parseInt(minServer);
            }

            @Override
            public int getMinMemory() {
                return Integer.parseInt(minMemory);
            }

            @Override
            public int getMaxMemory() {
                return Integer.parseInt(maxMemory);
            }
        };
    }

    public boolean hasTemplateFolder() {
        return templateFolder;
    }

    public enum Type {
        CREATE, UPDATE, REMOVE
    }

}

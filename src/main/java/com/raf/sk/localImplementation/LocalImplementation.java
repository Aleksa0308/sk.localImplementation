package com.raf.sk.localImplementation;

import com.raf.sk.specification.builders.DirectoryBuilder;
import com.raf.sk.specification.builders.FileBuilder;
import com.raf.sk.specification.io.IODriver;
import com.raf.sk.specification.io.IOManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LocalImplementation implements IODriver {

    static {
        IOManager.setIODriver(new LocalImplementation());
    }

    private String srcPath;

    @Override
    public void makeDirectory(String s) {
        Path path = Path.of(srcPath + s);
        if(Files.exists(path)){
            System.out.println("Directory already exists.");
        }else{
            try {
                Files.createDirectory(path);
                System.out.println("[DIRECTORY]: " + path.getFileName() + " has been created!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void makeFile(String s) {
        Path path = Path.of(srcPath + s);
        if(Files.exists(path)){
            System.out.println("File already exists.");
        }else{
            try {
                Files.createFile(path);
                System.out.println("[FILE]: " + path.getFileName() + " has been created!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteDirectory(String s) {
        Path path = Path.of(srcPath + s);
        try {
            Files.deleteIfExists(path);
            System.out.println("[DIRECTORY]: " + path.getFileName() + " has been deleted!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String s) {
        Path path = Path.of(srcPath + s);
        try {
            Files.deleteIfExists(path);
            System.out.println("[FILE]: " + path.getFileName() + " has been deleted!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveDirectory(String s, String s1) {
        s = srcPath + s;
        s1 = srcPath + s1;
        File sourceDir = new File(s);
        File targetDirTmp = new File(s1);
        if(sourceDir.isDirectory() && targetDirTmp.isDirectory()) {
            File targetDir = new File(s1 + "\\" + sourceDir.getName());
            try {
                Files.move(sourceDir.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[DIRECTORY]: " + sourceDir.getName() + " successfully moved to " + targetDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void moveFile(String s, String s1) {
        s = srcPath + s;
        s1 = srcPath + s1;
        File sourceFile = new File(s);
        File targetFile = new File(s1 + "\\" + sourceFile.getName());
        if(sourceFile.isFile()) {
            try {
                Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[FILE]: " + sourceFile.getName() + " successfully moved to " + targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void downloadDirectory(String s, String s1) {
        s = srcPath + s;
        s1 = srcPath + s1;
        File sourceDir = new File(s);
        File targetDirTmp = new File(s1);
        if(sourceDir.isDirectory() && targetDirTmp.isDirectory()) {
            File targetDir = new File(s1 + "\\" + sourceDir.getName());
            try {
                Files.copy(sourceDir.toPath(), targetDir.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FileBuilder uploadFile(String s, String s1) {
        s = srcPath + s;
        s1 = srcPath + s1;
        File targetPath = new File(s);
        File sourceFile = new File(s1);
        if(sourceFile.isFile() && targetPath.isDirectory()){
            try {
                Files.copy(sourceFile.toPath(), targetPath.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                return new FileBuilder(new DirectoryBuilder(), sourceFile.getName(), sourceFile.getTotalSpace());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void downloadFile(String s, String s1) {
        s = srcPath + s;
        s1 = srcPath + s1;
        File sourceFile = new File(s);
        File targetFile = new File(s1 + "\\" + sourceFile.getName());
        if(sourceFile.isFile()) {
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String readConfig(String s) {
        String result = null;
        try {
            result = Files.readString(Path.of(s));
        } catch (IOException e) {
            // #TODO ovde treba napraviti novi fajl
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Override
    public void writeConfig(String s, String absPath) {
        File targetFile = new File(absPath);
        try {
            FileWriter fileWriter = new FileWriter(targetFile);
            fileWriter.write(s);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull DirectoryBuilder initStorage(String s) {
        if (s.endsWith("/"))
            this.srcPath = s;
        else
            this.srcPath = s + "/";
        Path path = Path.of(Paths.get(s).toAbsolutePath().toString());
        String s2 = path.getParent().toString();
        File theFile = new File(s2);
            if(theFile.isDirectory()) {
                File[] files = theFile.listFiles();
                if (files == null) {
                    throw new RuntimeException(
                            "LocalImplementation: files[] object is null!"
                    );
                }
                if (files.length == 0) {
                    try {
                        Files.createDirectory(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new DirectoryBuilder();

                }
            }
        return new DirectoryBuilder();
    }
}
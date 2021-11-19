package com.raf.sk.localImplementation;

import com.raf.sk.specification.builders.DirectoryBuilder;
import com.raf.sk.specification.builders.FileBuilder;
import com.raf.sk.specification.builders.INodeBuilder;
import com.raf.sk.specification.exceptions.IODriverException;
import com.raf.sk.specification.io.IODriver;
import com.raf.sk.specification.io.IOManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        if (Files.exists(path)) {
            System.out.println("Directory already exists.");
        } else {
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
        if (Files.exists(path)) {
            System.out.println("File already exists.");
        } else {
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
        if (sourceDir.isDirectory() && targetDirTmp.isDirectory()) {
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
        if (sourceFile.isFile()) {
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
        if (sourceDir.isDirectory() && targetDirTmp.isDirectory()) {
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
        if (sourceFile.isFile() && targetPath.isDirectory()) {
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
        if (sourceFile.isFile()) {
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String readConfig(String absPath) {
        Path path = Path.of(Paths.get(absPath).toAbsolutePath().toString());
        srcPath = path.getParent().toString();
        try {
            return Files.readString(Path.of(absPath));
        } catch (IOException e) {
            // #TODO ovde treba napraviti novi fajl
            //e.printStackTrace();
            return null;
        }
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
    public @NotNull DirectoryBuilder initStorage() {
        File root = new File(srcPath);
        if (root.isDirectory()) {
            // #TODO ovo pokriva samo slučaj da je pozvan na direktorijumu koji postoji.
            // Dodati slučaj ako je metoda pozvana na path-u koji ne postoji (kreirati novi path i vratiti prazan
            // DirectoryBuilder)
            DirectoryBuilder db = new DirectoryBuilder(null, DirectoryBuilder.ROOT_DIRECTORY);
            return (DirectoryBuilder) traverse(db, root);
        } else {
            throw new IODriverException(
                    "Cannot initiate root on file!"
            );
        }
    }

    /**
     * Prolazi kroz direktorijum rekurzivno i pravi DirectoryBuilder.
     *
     * @param parent DirectoryBuilder koji se gradi.
     * @param file   Korenski File objekat.
     * @return Vraća korensko DirectoryBuilder stablo.
     */
    private INodeBuilder traverse(DirectoryBuilder parent, File file) {
        for (File f: file.listFiles()) {
            if (f.isFile()) {
                parent.addChild(new FileBuilder(
                        parent,
                        file.getName(),
                        file.length()
                ));
            } else {
                DirectoryBuilder db = new DirectoryBuilder(
                        parent,
                        file.getName()
                );
                parent.addChild(db);
                traverse(db, f);
            }
        }
        return parent;
    }
}

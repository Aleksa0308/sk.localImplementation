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
import java.util.Comparator;

public class LocalImplementation implements IODriver {

    private static final String INODE_SEPARATOR = "/";

    static {
        IOManager.setIODriver(new LocalImplementation());
    }

    private String srcPath;

    @Override
    public void makeDirectory(String s) {
        Path path = Path.of(resolvePath(s));
        if (Files.exists(path)) {
            System.out.println("Directory already exists.");
        } else {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void makeFile(String s) {
        Path path = Path.of(resolvePath(s));
        if (Files.exists(path)) {
            System.out.println("File already exists.");
        } else {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteDirectory(String s) {
        Path path = Path.of(resolvePath(s));
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String s) {
        Path path = Path.of(resolvePath(s));
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveDirectory(String s, String s1) {
        s = resolvePath(s);
        s1 = Path.of(resolvePath(s1)).getParent().toString();
        File sourceDir = new File(s);
        File targetDirTmp = new File(s1);
        if (!sourceDir.isDirectory()) {
            throw new IODriverException(
                    "Source node is not a directory: " + s
            );
        }
        if (!targetDirTmp.isDirectory()) {
            throw new IODriverException(
                    "Dest node is not a directory: " + s1
            );
        }
        File targetDir = new File(s1 + getSeparator() + sourceDir.getName());
        try {
            // #TODO ovde je moguće dobiti access error? Da li je ovo zbog implementacije ili samog okruženja?
            /*
            java.nio.file.AccessDeniedException: D:\fax\semestar-5\sk\projekat\sk\cli\target\maven-status -> D:\fax\semestar-5\sk\projekat\sk\cli\target\classes\maven-status
                at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:89)
                at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
                at java.base/sun.nio.fs.WindowsFileCopy.move(WindowsFileCopy.java:395)
                at java.base/sun.nio.fs.WindowsFileSystemProvider.move(WindowsFileSystemProvider.java:292)
                at java.base/java.nio.file.Files.move(Files.java:1426)
                at com.raf.sk.localImplementation.LocalImplementation.moveDirectory(LocalImplementation.java:101)
                at com.raf.sk.core.repository.Directory.move(Directory.java:250)
                at com.raf.sk.core.repository.INode.move(INode.java:146)
                at com.raf.sk.core.actions.ActionINodeMove.run(ActionINodeMove.java:64)
                at com.raf.sk.core.actions.ActionManager.run(ActionManager.java:50)
                at com.raf.sk.cli.Main.main(Main.java:290)
             */
            Files.move(sourceDir.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveFile(String s, String s1) {
        s = resolvePath(s);
        String dest = Path.of(resolvePath(s1)).getParent().toString();
        File sourceFile = new File(s);
        File targetDir = new File(dest);
        if (!sourceFile.isFile()) {
            throw new IODriverException(
                    "Source node is not a file: " + s
            );
        }
        if (!targetDir.isDirectory()) {
            throw new IODriverException(
                    "Target node is not a directory: " + s1
            );
        }
        try {
            Files.move(sourceFile.toPath(), Path.of(resolvePath(s1)), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void downloadDirectory(String s, String s1) {
        s = resolvePath(s);
        // s1 je apsolutna
        File sourceDir = new File(s);
        File targetDirTmp = new File(s1);
        if (sourceDir.isDirectory() && targetDirTmp.isDirectory()) {
            File targetDir = new File(s1 + getSeparator() + sourceDir.getName());
            try {
                // #TODO ovde se ne kopira sadržaj! dodati da može sadržaj da se kopira
                Files.copy(sourceDir.toPath(), targetDir.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FileBuilder uploadFile(String s, String s1) {
        s = resolvePath(s);
        // s1 je apsolutna
        File sourceFile = new File(s1);
        File targetPath = new File(s + getSeparator() + sourceFile.getName());
        if (sourceFile.isFile()) {
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
        s = resolvePath(s);
        // s1 je apsolutna
        File sourceFile = new File(s);
        File targetFile = new File(s1 + getSeparator() + sourceFile.getName());
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
     * Vraća sistemski fajl separator.
     *
     * @return Sistemski fajl separator.
     */
    private String getSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * Pretvara path koji je dala aplikacija u apsolutni path za korisničko okruženje. Može se pozivati SAMO nakon
     * inicijalnog čitanja direktorijuma.
     *
     * @param appPath Path iz aplikacije.
     * @return Krajnji path.
     */
    private String resolvePath(String appPath) {
        if (srcPath == null)
            throw new IODriverException(
                    "Programming error: you cannot call resolvePath() before reading the config!"
            );

        String sep = getSeparator();
        if (!srcPath.endsWith(sep)) {
            srcPath = srcPath + sep;
        }

        if (appPath.startsWith(INODE_SEPARATOR))
            appPath = appPath.substring(1);

        if (sep.equals("\\")) sep = "\\\\";
        appPath = appPath.replaceAll(INODE_SEPARATOR, sep);
        return srcPath + appPath;
    }

    /**
     * Prolazi kroz direktorijum rekurzivno i pravi DirectoryBuilder.
     *
     * @param parent DirectoryBuilder koji se gradi.
     * @param file   Korenski File objekat.
     * @return Vraća korensko DirectoryBuilder stablo.
     */
    private INodeBuilder traverse(DirectoryBuilder parent, File file) {
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                parent.addChild(new FileBuilder(
                        parent,
                        f.getName(),
                        f.length()
                ));
            } else {
                DirectoryBuilder db = new DirectoryBuilder(
                        parent,
                        f.getName()
                );
                parent.addChild(db);
                traverse(db, f);
            }
        }
        return parent;
    }
}

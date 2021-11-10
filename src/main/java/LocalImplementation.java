import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.raf.sk.specification.builders.DirectoryBuilder;
import com.raf.sk.specification.builders.FileBuilder;
import com.raf.sk.specification.exceptions.IOManagerNoDriverException;
import com.raf.sk.specification.io.IODriver;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class LocalImplementation implements IODriver {

    @Override
    public void makeDirectory(String s) {
        Path path = Path.of(s);
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
        Path path = Path.of(s);
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
        Path path = Path.of(s);
        try {
            Files.deleteIfExists(path);
            System.out.println("[DIRECTORY]: " + path.getFileName() + " has been deleted!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String s) {
        Path path = Path.of(s);
        try {
            Files.deleteIfExists(path);
            System.out.println("[FILE]: " + path.getFileName() + " has been deleted!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveDirectory(String s, String s1) {
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

        return null;
    }

    @Override
    public void downloadFile(String s, String s1) {
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
        try {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(s));

            // convert JSON file to map
            Map<?, ?> map = gson.fromJson(reader, Map.class);

            // print map entries
            StringBuilder mapAsString = new StringBuilder("{");
            for (Object key : map.keySet()) {
                mapAsString.append(key + "=" + map.get(key) + ", ");
            }
            mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
            System.out.println(mapAsString.toString());
            //return mapAsString.toString();

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void writeConfig(String s, String s1) {
        File targetFile = new File(s1);
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
        Path path = Path.of(s);
        String s2 = path.getParent().toString();
        File theFile = new File(s2);
            if(theFile.isDirectory()) {
                File[] files = theFile.listFiles();
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

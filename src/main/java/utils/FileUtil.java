package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileUtil {
    public static void writeToFile(String filePath, String content) {
        try {
            Path file = Paths.get(filePath);
            Files.write(file, content.getBytes(), StandardOpenOption.CREATE);
            System.out.println("Content written to file successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String filePath = "result.txt";
        String content = "Hello, World! This is some content to write to a file.";

        writeToFile(filePath, content);
    }
}

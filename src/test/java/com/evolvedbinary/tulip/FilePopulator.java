package com.evolvedbinary.tulip;

import java.io.FileWriter;
import java.io.IOException;

public class FilePopulator {
    public static void main(String[] args) {
        String content = "\"When\"\"What\" \"Hello\"    \"Hi\"    1 2 3 4 11 2 33 44\"Evolved\"22\"Evolved Binary\" ";

        int repeatCount = 600000; // Change this to set how many times the string should repeat

        try (FileWriter writer = new FileWriter("src/test/java/com/evolvedbinary/tulip/file.txt")) {
            for (int i = 0; i < repeatCount; i++) {
                writer.write(content);
            }
            System.out.println("File written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

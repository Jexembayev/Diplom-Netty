package orhestra.createVmTab.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    public List<String> readFile(String filePath) {
        List<String> lines = new ArrayList<>();
        if (filePath != null && !filePath.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Error while reading file " + e.getMessage());
            }
        }
        return lines;
    }
}

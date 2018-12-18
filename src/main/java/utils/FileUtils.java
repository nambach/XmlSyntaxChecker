package utils;

import java.io.*;

public class FileUtils {

    public static String EMPTY_CONTENT = "";

    public static String readTextContent(String filePath) {
        StringBuilder result = new StringBuilder(EMPTY_CONTENT);
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            reader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result.toString();
        }
    }
}

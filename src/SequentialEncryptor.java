import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class SequentialEncryptor {

    public static String caeserCipher(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                // ch - 'A' gives the position of the element (0 based)
                // Then add the shift value (3), %26 then convert it to character by adding 'A' to it.
                result.append((char) ('A' + (ch - 'A' + shift) % 26));
            } else if (Character.isLowerCase(ch)) {
                result.append((char) ('a' + (ch - 'a' + shift) % 26));
            } else if (Character.isDigit(ch)) {
                result.append((char) ('0' + (ch - '0' + shift) % 10));
            } else {
                result.append(ch); // symbols, commas, etc.
            }
        }
        return result.toString();
    }


    public static void main(String[] args) {
        // Paths to my files
        String csvFile = "C:\\Users\\yazan\\Desktop\\Medical_Data.csv";
        String outputCSVFile = "C:\\Users\\yazan\\Desktop\\encrypted_data.csv";

        try {
            // Execution time
            long startExecutionTime = System.currentTimeMillis();


            List<String> lines = Files.readAllLines(Paths.get(csvFile));
            List<String> encryptedLines = new ArrayList<>();

            // Read each line of the CSV and Encrypt it
            for(String line : lines){
               encryptedLines.add(caeserCipher(line,3));
            }
            // Write it to the output file
            Files.write(Paths.get(outputCSVFile),encryptedLines);

            // End of execution time
            long endExecutionTime = System.currentTimeMillis();
            System.out.println("Execution time: " + (endExecutionTime - startExecutionTime) + " ms");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
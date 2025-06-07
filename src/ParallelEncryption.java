import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ParallelEncryption {

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

    public static void runParallel(String inputPath,String outputPath,int numOfThreads) throws IOException, InterruptedException {
        List<String> lines = Files.readAllLines(Paths.get(inputPath));
        int totalLinesInFile = lines.size();
        int sizeEachThread = totalLinesInFile / numOfThreads;

        List<String> encryptedResults = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();

        long startExecutionTime = System.currentTimeMillis();

        for(int i = 0; i < numOfThreads; i++){
            int start = i * sizeEachThread;
            int end = (i == numOfThreads - 1)? totalLinesInFile: start + sizeEachThread;
            List<String> subList = lines.subList(start,end);

            Thread thread = new Thread(() -> {
                List<String> encryptedSubList = new ArrayList<>();
                for(String line : subList){
                    encryptedSubList.add(caeserCipher(line,3));
                }
                encryptedResults.addAll(encryptedSubList);
            });
            threads.add(thread);
            thread.start();

            for (Thread th : threads) th.join(); // Wait for all threads to complete
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Encryption with " + numOfThreads + " threads took: " + (endTime - startExecutionTime) + " ms");

        Files.write(Paths.get(outputPath), encryptedResults);

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        // Paths to my files
        String csvFile = "C:\\Users\\yazan\\Desktop\\Medical_Data.csv";
        String outputCSVFile = "C:\\Users\\yazan\\Desktop\\encrypted_data.csv";

        runParallel(csvFile,outputCSVFile,4);
    }
}
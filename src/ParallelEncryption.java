import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ParallelEncryption {

    public static void writeExecutionTimesToCSV(String path, Map<Integer, Long> timesMap){
        // Ensures the file is automatically closed after writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))){
            writer.write("Threads,Time(ms)\n");
            for (Map.Entry<Integer,Long> entry : timesMap.entrySet()){
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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



    public static long runParallel(String inputPath,String outputPath,int numOfThreads) throws IOException, InterruptedException {
        List<String> lines = Files.readAllLines(Paths.get(inputPath));
        int totalLinesInFile = lines.size();
        // The chunk each thread gets to execute
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

            for (Thread th : threads) {
                thread.join();   // Wait for all threads to complete
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Encryption with " + numOfThreads + " threads took: " + (endTime - startExecutionTime) + " ms");

        Files.write(Paths.get(outputPath), encryptedResults);
        return endTime - startExecutionTime;
    }

    public static String runParallelWithMessagePassing(String inputPath, int numOfThreads) throws IOException, InterruptedException {

        List<String> lines = Files.readAllLines(Paths.get(inputPath));
        int totalLinesInFile = lines.size();
        int  sizeEachThread = totalLinesInFile / numOfThreads;

        // Queue of best result(line) for each thread
        BlockingQueue<String> resultQueue = new LinkedBlockingQueue<>();
        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < numOfThreads; i++){
            int start = i * sizeEachThread;
            int end = (i == numOfThreads - 1)? totalLinesInFile: start + sizeEachThread;
            List<String> subList = lines.subList(start,end);

            Thread thread = new Thread(() -> {
               String bestResultInThread = "";
               for(String line : subList){
                   // Find the longest line in the thread
                   if(line.length() > bestResultInThread.length()){
                       bestResultInThread = line;
                   }
               }
               try {
                   // Add the longest line the BlockingQueue
                   resultQueue.put(bestResultInThread);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
            });

            threads.add(thread);
            thread.start();
        }
        for(Thread th: threads){
            th.join();
        }

        // Find the longest line among all threads
        String bestOfAllThreads = "";
        while(!resultQueue.isEmpty()){
            String current = resultQueue.take();
            if(current.length() > bestOfAllThreads.length()){
                bestOfAllThreads = current;
            }
        }

        System.out.println("Final best result (longest line): " + bestOfAllThreads);
        return bestOfAllThreads;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        // Paths to my files
        String csvFile = "C:\\Users\\yazan\\Desktop\\Medical_Data.csv";
        String outputCSVFile = "C:\\Users\\yazan\\Desktop\\encrypted_data.csv";

        Map<Integer, Long> executionTimes = new HashMap<>();
        executionTimes.put(4,runParallel(csvFile,outputCSVFile,4));
        executionTimes.put(8,runParallel(csvFile,outputCSVFile,8));
        executionTimes.put(16,runParallel(csvFile,outputCSVFile,16));
        executionTimes.put(32,runParallel(csvFile,outputCSVFile,32));

        writeExecutionTimesToCSV("C:/Users/yazan/Desktop/execution_times.csv", executionTimes);

        // Run parallel with message passing
        runParallelWithMessagePassing(csvFile,4);
    }
}

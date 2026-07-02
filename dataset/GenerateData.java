import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateData {
    
    // Generates dataset and saves it to a file (either Binary or CSV)
    public static void generateDataset(int numPoints, String filename, boolean saveToFile, boolean useCSV) {
        System.out.println("\n--------------------------------------------------");
        System.out.printf("Starting generation of %d data points...%n", numPoints);
        
        long start = System.nanoTime();
        
        // 1. Initialize Random generator with seed 42
        Random rng = new Random(42);
        
        double[] data = new double[numPoints];
        
        // 2. Generate random doubles in range [0.0, 10000.0]
        for (int i = 0; i < numPoints; i++) {
            data[i] = rng.nextDouble() * 10000.0;
        }
        
        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        
        System.out.printf("Successfully generated %d doubles.%n", numPoints);
        System.out.printf("Time taken to generate in memory: %.4f seconds.%n", elapsedSeconds);
        
        // 3. Save to file if requested
        if (saveToFile) {
            long saveStart = System.nanoTime();
            if (useCSV) {
                // Save as CSV (plain text format)
                String csvFilename = filename.replace(".bin", ".csv");
                try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
                    for (double val : data) {
                        writer.println(val); // One double per line
                    }
                    long saveEnd = System.nanoTime();
                    double saveElapsed = (saveEnd - saveStart) / 1_000_000_000.0;
                    System.out.printf("Data saved to CSV file: %s (took %.4f seconds)%n", csvFilename, saveElapsed);
                } catch (IOException e) {
                    System.err.println("Failed to write CSV file: " + e.getMessage());
                }
            } else {
                // Save as Binary (raw double precision bytes)
                try (DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(filename)))) {
                    for (double val : data) {
                        dos.writeDouble(val);
                    }
                    long saveEnd = System.nanoTime();
                    double saveElapsed = (saveEnd - saveStart) / 1_000_000_000.0;
                    System.out.printf("Data saved to Binary file: %s (took %.4f seconds)%n", filename, saveElapsed);
                } catch (IOException e) {
                    System.err.println("Failed to write Binary file: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Synthetic Dataset Generator (Java Version) ===");
        
        int smallSize = 1_000_000;    // 1 Million
        int mediumSize = 10_000_000;  // 10 Million
        int largeSize = 100_000_000;  // 100 Million
        
        // CHANGE THESE CONFIGURATIONS IF NEEDED:
        boolean saveToFile = true;   // Set to true to actually save the files to disk
        boolean useCSV = true;       // Set to true for CSV format, false for Binary format
        
        // Run benchmarks and save datasets to your disk
        generateDataset(smallSize, "dataset_small.bin", saveToFile, useCSV);
        generateDataset(mediumSize, "dataset_medium.bin", saveToFile, useCSV);
        
        // Note: For 100 Million points, CSV file will be ~1.5 GB. 
        // We run it here, but feel free to comment it out if you lack disk space.
        generateDataset(largeSize, "dataset_large.bin", saveToFile, useCSV);
        
        System.out.println("\n================ Benchmarking Complete ================");
    }
}

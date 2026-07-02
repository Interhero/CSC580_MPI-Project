#include <iostream>
#include <vector>
#include <random>
#include <chrono>
#include <fstream>
#include <iomanip>

// Function to generate and optionally save the synthetic dataset
void generateDataset(size_t numPoints, const std::string& filename, bool saveToFile = false) {
    std::cout << "\n--------------------------------------------------" << std::endl;
    std::cout << "Starting generation of " << numPoints << " data points..." << std::endl;
    
    // Start timing the generation process
    auto start = std::chrono::high_resolution_clock::now();
    
    // 1. Initialize the 64-bit Mersenne Twister generator with a fixed seed (42) for reproducibility
    std::mt19937_64 rng(42);
    
    // 2. Define a uniform real distribution mapping to double precision floating-point numbers in [0.0, 10000.0]
    std::uniform_real_distribution<double> dist(0.0, 10000.0);
    
    // Pre-allocate memory for the dataset to avoid overhead from dynamic resizing (reallocations)
    std::vector<double> data;
    try {
        data.reserve(numPoints);
    } catch (const std::bad_alloc& e) {
        std::cerr << "Memory allocation failed for " << numPoints << " points: " << e.what() << std::endl;
        return;
    }
    
    // 3. Populate the vector with random doubles
    for (size_t i = 0; i < numPoints; ++i) {
        data.push_back(dist(rng));
    }
    
    // End timing the generation process
    auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> diff = end - start;
    
    std::cout << "Successfully generated " << numPoints << " doubles." << std::endl;
    std::cout << "Time taken to generate in memory: " << std::fixed << std::setprecision(4) 
              << diff.count() << " seconds." << std::endl;
              
    // 4. Optionally write the data to a binary file (fast and compact representation)
    if (saveToFile) {
        auto saveStart = std::chrono::high_resolution_clock::now();
        
        // Open the file in binary mode
        std::ofstream outFile(filename, std::ios::binary);
        if (outFile.is_open()) {
            // Write the buffer directly to disk
            outFile.write(reinterpret_cast<const char*>(data.data()), data.size() * sizeof(double));
            outFile.close();
            
            auto saveEnd = std::chrono::high_resolution_clock::now();
            std::chrono::duration<double> saveDiff = saveEnd - saveStart;
            std::cout << "Data saved to binary file: " << filename 
                      << " (took " << saveDiff.count() << " seconds)" << std::endl;
        } else {
            std::cerr << "Failed to open file: " << filename << " for writing." << std::endl;
        }
    }
}

int main() {
    std::cout << "=== Synthetic Dataset Generator and Benchmarking ===" << std::endl;
    
    // Define the data scales as specified in the requirements
    const size_t SMALL_SIZE  = 1000000;    // 1 Million
    const size_t MEDIUM_SIZE = 10000000;   // 10 Million
    const size_t LARGE_SIZE  = 100000000;  // 100 Million
    
    // Run benchmarks for each size
    // Note: Setting saveToFile=false to measure pure generation time, or true if you want to write to disk.
    // Writing 100 million doubles to disk will create a ~800MB binary file.
    
    generateDataset(SMALL_SIZE, "dataset_small.bin", true);
    generateDataset(MEDIUM_SIZE, "dataset_medium.bin", true);
    generateDataset(LARGE_SIZE, "dataset_large.bin", true);
    
    std::cout << "\n================ Benchmarking Complete ================" << std::endl;
    return 0;
}

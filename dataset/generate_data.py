import random
import time
import struct

def generate_dataset(num_points, filename, save_to_file=False):
    print(f"\n--------------------------------------------------")
    print(f"Starting generation of {num_points:,} data points...")
    
    start_time = time.time()
    
    # 1. Initialize random generator with seed 42 for reproducibility
    # Python uses Mersenne Twister (MT19937) internally for random
    random.seed(42)
    
    # 2. Generate random floats in range [0.0, 10000.0]
    # We use a list comprehension or generator to create the dataset
    data = [random.uniform(0.0, 10000.0) for _ in range(num_points)]
    
    end_time = time.time()
    elapsed = end_time - start_time
    
    print(f"Successfully generated {num_points:,} doubles.")
    print(f"Time taken to generate in memory: {elapsed:.4f} seconds.")
    
    # 3. Save to file if requested (as binary double-precision floats, matching C++ layout)
    if save_to_file:
        save_start = time.time()
        with open(filename, 'wb') as f:
            # Struct pack format 'd' is double (8 bytes)
            # pack_into or writing in chunks is memory efficient, but for simplicity:
            f.write(struct.pack(f'{num_points}d', *data))
        save_elapsed = time.time() - save_start
        print(f"Data saved to binary file: {filename} (took {save_elapsed:.4f} seconds)")

if __name__ == "__main__":
    print("=== Synthetic Dataset Generator (Python version) ===")
    
    SMALL_SIZE = 1000000     # 1 Million
    MEDIUM_SIZE = 10000000   # 10 Million
    
    # Note: Large size (100M) in pure Python may be slow/memory heavy. 
    # Python is an interpreted language. Let's run Small and Medium.
    generate_dataset(SMALL_SIZE, "dataset_small.bin", False)
    generate_dataset(MEDIUM_SIZE, "dataset_medium.bin", False)
    
    print("\n================ Benchmarking Complete ================")

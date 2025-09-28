import sys
import os

def do_first_pass(f):
    for line_number, line in enumerate(f, start = 1):
        print(f"LN {line_number}: {line}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Please specify a source file")
        sys.exit()

    path = sys.argv[1]
    
    if not os.path.isfile(path):
        print(f"File {path} not found")
        sys.exit()

    with open(path, 'r') as f:
        do_first_pass(f)

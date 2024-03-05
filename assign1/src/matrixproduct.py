import time
import ctypes

# Define constants
SYSTEMTIME = time.clock_t

# Load PAPI library
papi_lib = ctypes.CDLL("libpapi.so")

# Function to handle PAPI errors
def handle_error(retval):
    print(f"PAPI error {retval}: {papi_lib.PAPI_strerror(retval)}")
    exit(1)

# Function to initialize PAPI
def init_papi():
    retval = papi_lib.PAPI_library_init(papi_lib.PAPI_VER_CURRENT)
    if retval != papi_lib.PAPI_VER_CURRENT and retval < 0:
        print("PAPI library version mismatch!")
        exit(1)
    if retval < 0:
        handle_error(retval)

    print(f"PAPI Version Number: MAJOR: {papi_lib.PAPI_VERSION_MAJOR(retval)} "
          f"MINOR: {papi_lib.PAPI_VERSION_MINOR(retval)} REVISION: {papi_lib.PAPI_VERSION_REVISION(retval)}")

# Matrix multiplication function
def on_mult(m_ar, m_br):
    pha = [[1.0] * m_ar for _ in range(m_ar)]
    phb = [[float(i + 1) for _ in range(m_br)] for i in range(m_br)]
    phc = [[0.0] * m_br for _ in range(m_ar)]

    time1 = time.clock()

    for i in range(m_ar):
        for j in range(m_br):
            phc[i][j] = sum(pha[i][k] * phb[k][j] for k in range(m_ar))

    time2 = time.clock()
    print(f"Time: {time2 - time1:.3f} seconds")

    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    for i in range(1):
        for j in range(min(10, m_br)):
            print(phc[i][j], end=" ")
    print()

# Function for line x line matrix multiplication
def on_mult_line(m_ar, m_br):
    pass  # Add your code here

# Function for block x block matrix multiplication
def on_mult_block(m_ar, m_br, bk_size):
    pass  # Add your code here

# Main function
def main():
    lin, col, block_size = 0, 0, 0
    op = 1

    event_set = papi_lib.PAPI_NULL
    values = (ctypes.c_longlong * 2)()
    ret = papi_lib.PAPI_library_init(papi_lib.PAPI_VER_CURRENT)

    if ret != papi_lib.PAPI_VER_CURRENT:
        print("FAIL")

    ret = papi_lib.PAPI_create_eventset(ctypes.byref(event_set))
    if ret != papi_lib.PAPI_OK:
        print("ERROR: create eventset")

    ret = papi_lib.PAPI_add_event(event_set, papi_lib.PAPI_L1_DCM)
    if ret != papi_lib.PAPI_OK:
        print("ERROR: PAPI_L1_DCM")

    ret = papi_lib.PAPI_add_event(event_set, papi_lib.PAPI_L2_DCM)
    if ret != papi_lib.PAPI_OK:
        print("ERROR: PAPI_L2_DCM")

    while op != 0:
        print("\n1. Multiplication")
        print("2. Line Multiplication")
        print("3. Block Multiplication")
        op = int(input("Selection?: "))

        if op == 0:
            break

        lin = int(input("Dimensions: lins=cols? "))
        col = lin

        # Start counting
        ret = papi_lib.PAPI_start(event_set)
        if ret != papi_lib.PAPI_OK:
            print("ERROR: Start PAPI")

        if op == 1:
            on_mult(lin, col)
        elif op == 2:
            on_mult_line(lin, col)
        elif op == 3:
            block_size = int(input("Block Size? "))
            on_mult_block(lin, col, block_size)

        ret = papi_lib.PAPI_stop(event_set, values)
        if ret != papi_lib.PAPI_OK:
            print("ERROR: Stop PAPI")
        print(f"L1 DCM: {values[0]}")
        print(f"L2 DCM: {values[1]}")

        ret = papi_lib.PAPI_reset(event_set)
        if ret != papi_lib.PAPI_OK:
            print("FAIL reset")

    ret = papi_lib.PAPI_remove_event(event_set, papi_lib.PAPI_L1_DCM)
    if ret != papi_lib.PAPI_OK:
        print("FAIL remove event")

    ret = papi_lib.PAPI_remove_event(event_set, papi_lib.PAPI_L2_DCM)
    if ret != papi_lib.PAPI_OK:
        print("FAIL remove event")

    ret = papi_lib.PAPI_destroy_eventset(ctypes.byref(event_set))
    if ret != papi_lib.PAPI_OK:
        print("FAIL destroy")

if __name__ == "__main__":
    main()

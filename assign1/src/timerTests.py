from matrixproduct import on_mult, on_mult_line


# Main function
def main():
    for i in range(600, 3001, 400):
        print(f'on_mult: line, cols = {i}')
        on_mult(i, i)

        print(f'on_mult_line: line, cols = {i}')
        on_mult_line(i, i)


if __name__ == "__main__":
    main()

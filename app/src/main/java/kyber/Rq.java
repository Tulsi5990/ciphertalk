package kyber;

import java.util.Arrays;

public class Rq {
    private int Q;         // The modulus Q used for all operations in Rq
    private int[] coeffs;  // Coefficients of the polynomial

    // Constructor to initialize Rq from an array of coefficients and modulus Q
    public Rq(int[] coeffs, int q) {
        this.coeffs = Arrays.copyOf(coeffs, coeffs.length);  // Copy coefficients to avoid external modification
        this.Q = q;  // Set the modulus Q
        normalise();  // Ensure all coefficients are in the range [0, Q-1]
    }

    // Constructor to initialize Rq with a specified size and modulus Q
    public Rq(int size, int q) {
        this.coeffs = new int[size];  // Initialize coefficients array with zeros
        this.Q = q;  // Set the modulus Q
    }

    // Addition of two Rq objects
    public Rq add(Rq other) {
        int size = Math.max(coeffs.length, other.coeffs.length);  // Determine the size of the result
        int[] result = new int[size];  // Initialize result array with the required size

        // Add coefficients of the current polynomial
        for (int i = 0; i < coeffs.length; i++) {
            result[i] += coeffs[i];
        }

        // Add coefficients of the other polynomial
        for (int i = 0; i < other.coeffs.length; i++) {
            result[i] += other.coeffs[i];
        }

        return new Rq(result, Q);  // Create a new Rq object with the result
    }

    // Subtraction of two Rq objects
    public Rq subtract(Rq other) {
        int size = Math.max(coeffs.length, other.coeffs.length);  // Determine the size of the result
        int[] result = new int[size];  // Initialize result array with the required size

        // Add coefficients of the current polynomial
        for (int i = 0; i < coeffs.length; i++) {
            result[i] += coeffs[i];
        }

        // Subtract coefficients of the other polynomial
        for (int i = 0; i < other.coeffs.length; i++) {
            result[i] -= other.coeffs[i];
        }

        return new Rq(result, Q);  // Create a new Rq object with the result
    }

    // Convert an Rq object to a byte array
    public byte[] toBytes() {
        byte[] byteArray = new byte[coeffs.length * Integer.BYTES];  // Each int is 4 bytes
        for (int i = 0; i < coeffs.length; i++) {
            System.arraycopy(intToBytes(coeffs[i]), 0, byteArray, i * Integer.BYTES, Integer.BYTES);
        }
        return byteArray;  // Return the byte array representation
    }

    // Convert a byte array to an Rq object
    public static Rq fromBytes(byte[] data, int q) {
        int byteSize = Integer.BYTES;  // Size of an int in bytes
        int length = data.length / byteSize;  // Determine the number of integers in the byte array
        int[] coefficients = new int[length];  // Initialize array for coefficients

        for (int i = 0; i < length; i++) {
            byte[] intBytes = Arrays.copyOfRange(data, i * byteSize, (i + 1) * byteSize);
            coefficients[i] = bytesToInt(intBytes);  // Convert each 4-byte segment to an integer
        }

        return new Rq(coefficients, q);  // Create a new Rq object with the extracted coefficients
    }

    // Multiplication of two Rq objects
    public Rq multiply(Rq other) {
        int[] result = new int[coeffs.length + other.coeffs.length - 1];  // Result size = convolution size

        // Multiply coefficients using polynomial multiplication
        for (int i = 0; i < coeffs.length; i++) {
            for (int j = 0; j < other.coeffs.length; j++) {
                result[i + j] = (result[i + j] + (coeffs[i] * other.coeffs[j])) % Q;
            }
        }

        // Reduce the result modulo x^n + 1 (circular reduction)
        int[] finalResult = new int[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            finalResult[i] = result[i];
            if (i + coeffs.length < result.length) {
                finalResult[i] = (finalResult[i] - result[i + coeffs.length]) % Q;
            }
        }

        return new Rq(finalResult, Q);  // Create a new Rq object with the reduced coefficients
    }

    // Normalization of coefficients modulo Q
    public void normalise() {
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = ((coeffs[i] % Q) + Q) % Q;  // Ensure coefficients are in the range [0, Q-1]
        }
    }

    // Helper method to convert an int to a byte array
    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),  // Most significant byte
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value          // Least significant byte
        };
    }

    // Helper method to convert a byte array back to int
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |  // Combine 4 bytes into an int
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    // Getter for coefficients
    public int[] getCoeffs() {
        return Arrays.copyOf(coeffs, coeffs.length);  // Return a copy to ensure immutability
    }

    // Setter for coefficients
    public void setCoeffs(int[] coeffs) {
        this.coeffs = Arrays.copyOf(coeffs, coeffs.length);  // Copy coefficients to avoid external modification
        normalise();  // Normalize coefficients to ensure they are modulo Q
    }
}

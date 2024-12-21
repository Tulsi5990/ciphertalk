package kyber;

import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class CRYSTALS_KYBER {
    private static int n;  // Polynomial degree
    private static int k;  // Matrix dimension
    private static int Q;  // Modulus
    private final Random random;

    public CRYSTALS_KYBER(int N, int K, int q) {
        this.n = N;
        this.k = K;
        this.Q = q;
        this.random = new Random();
    }

    protected Rq[][] generateMatrix() {
        Rq[][] A = new Rq[k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int[] coeffs = new int[n];
                for (int l = 0; l < n; l++) {
                    coeffs[l] = random.nextInt(Q);
                }
                A[i][j] = new Rq(coeffs, Q);
            }
        }
        return A;
    }

    protected Rq[] generateRandomVector(int size, int bound) {
        Rq[] vector = new Rq[size];
        for (int i = 0; i < size; i++) {
            int[] coeffs = new int[n];
            for (int j = 0; j < n; j++) {
                coeffs[j] = random.nextInt(bound);
            }
            vector[i] = new Rq(coeffs, Q);
        }
        return vector;
    }

    public KeyPair generateKeyPair() {
        Rq[][] A = generateMatrix();
        Rq[] s = generateRandomVector(k, Q);  // Secret key
        Rq[] e = generateRandomVector(k, 4);  // Error terms

        Rq[] t = new Rq[k];
        for (int i = 0; i < k; i++) {
            t[i] = new Rq(n, Q);
            for (int j = 0; j < k; j++) {
                t[i] = t[i].add(A[i][j].multiply(s[j]));
            }
            t[i] = t[i].add(e[i]);
        }

        return new KeyPair(t, s, A);
    }

    public EncapsulationResult encapsulate(Rq[] publicKey, Rq[][] A, byte[] keyAndIV) {
        Rq[] r = generateRandomVector(k, Q);  // Random vector
        Rq[] e1 = generateRandomVector(k, 3);  // Error vectors
        Rq e2 = new Rq(new int[n], Q);         // Error term for v

        Rq[] u = new Rq[k];
        for (int i = 0; i < k; i++) {
            u[i] = e1[i];
            for (int j = 0; j < k; j++) {
                u[i] = u[i].add(A[j][i].multiply(r[j]));
            }
        }

        Rq v = e2;
        for (int i = 0; i < k; i++) {
            v = v.add(publicKey[i].multiply(r[i]));
        }

        // Optionally, modify or combine keyAndIV here if needed.
        byte[] sharedSecret = generateSharedSecret(u, v);
        Log.d("Encryption", "v: " + v.toBytes().toString());

        return new EncapsulationResult(u, v, sharedSecret);
    }


    public byte[] decapsulate(Rq[] secretKey, Rq[] u, Rq v) {
        Rq m = v;
        for (int i = 0; i < k; i++) {
            m = m.subtract(secretKey[i].multiply(u[i]));
        }
        byte[] mBytes= m.toBytes();
        Log.d("Decryption", " m: " + mBytes.toString());
        return generateSharedSecret(u, m);
    }

    private byte[] generateSharedSecret(Rq[] u, Rq v) {
        byte[] secret = new byte[48];  // Combined size for session key and IV

        // Extract session key (first 32 bytes from v)
        int[] coeffs = v.getCoeffs();
        for (int i = 0; i < 32; i++) {
            secret[i] = (byte) (coeffs[i] & 0xFF);
        }

        // Generate session IV (last 16 bytes, or from some other source)
        // You can either extract it from 'u', 'v', or generate it randomly.
        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) (coeffs[i + 32] & 0xFF);  // Optionally, use the next 16 coefficients
        }

        // Combine session key and IV
        System.arraycopy(iv, 0, secret, 32, 16);  // Append IV to the session key

        return secret;  // Now returns 48 bytes: 32 for the session key and 16 for the IV
    }


    public static byte[] convertRqArrayToBytes(Rq[] rqArray) {
        int rqSize = rqArray[0].toBytes().length;
        byte[] result = new byte[rqSize * rqArray.length];
        int position = 0;

        for (Rq rq : rqArray) {
            byte[] rqBytes = rq.toBytes();
            System.arraycopy(rqBytes, 0, result, position, rqSize);
            position += rqSize;
        }
        return result;
    }

    // Convert byte[] to Rq[] (for encapsulated u)
    public Rq[] convertBytesToRqArray(byte[] data) {
        int rqSize = n * Integer.BYTES;  // Adjust based on each Rq's byte representation
        int numRq = data.length / rqSize;
        Rq[] rqArray = new Rq[numRq];

        for (int i = 0; i < numRq; i++) {
            byte[] rqData = Arrays.copyOfRange(data, i * rqSize, (i + 1) * rqSize);
            rqArray[i] = Rq.fromBytes(rqData, Q);
        }
        return rqArray;
    }

    // Convert byte[] to a single Rq (for encapsulated v)
    public Rq convertBytesToRq(byte[] data) {
        return Rq.fromBytes(data, Q);
    }
    public static class KeyPair {
        public final Rq[] publicKey;
        public final Rq[] secretKey;
        public final Rq[][] A;

        public KeyPair(Rq[] publicKey, Rq[] secretKey, Rq[][] A) {
            this.publicKey = publicKey;
            this.secretKey = secretKey;
            this.A = A;
        }
    }

    public static class EncapsulationResult {
        public final Rq[] u;
        public final Rq v;
        public final byte[] sharedSecret;

        public EncapsulationResult(Rq[] u, Rq v, byte[] sharedSecret) {
            this.u = u;
            this.v = v;
            this.sharedSecret = sharedSecret;
        }

        // Convert Rq[] (u) to byte[]
        public byte[] getU() {
            return convertRqArrayToBytes(u);
        }

        // Convert single Rq (v) to byte[]
        public byte[] getV() {
            return v.toBytes();
        }

        // Helper method to convert Rq[] to byte[]



    }
}

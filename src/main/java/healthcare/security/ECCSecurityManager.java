package healthcare.security;

import healthcare.utils.Constants;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

/**
 * ECCSecurityManager.java
 * Facade that orchestrates ECC key management, encryption, and decryption
 * for all healthcare data transmissions in the PPEC framework.
 *
 * Lifecycle:
 *   1. Call initialize() once at simulation start.
 *   2. Call secureData(byte[]) to encrypt outgoing tuples.
 *   3. Call recoverData(byte[]) to decrypt incoming tuples.
 */
public class ECCSecurityManager {

    private static final Logger LOG = Logger.getLogger(ECCSecurityManager.class.getName());

    private final ECCKeyGenerator keyGenerator;
    private final ECCEncryptor    encryptor;
    private final ECCDecryptor    decryptor;

    private KeyPair    keyPair;
    private PublicKey  publicKey;
    private PrivateKey privateKey;

    // Simulation counters
    private long encryptionCount = 0;
    private long decryptionCount = 0;
    private long totalEncryptionLatencyNs = 0;
    private long totalDecryptionLatencyNs = 0;

    public ECCSecurityManager() {
        this.keyGenerator = new ECCKeyGenerator();
        this.encryptor    = new ECCEncryptor();
        this.decryptor    = new ECCDecryptor();
    }

    /**
     * Initialize ECC keys. Must be called before any encrypt/decrypt operations.
     */
    public void initialize() {
        try {
            this.keyPair    = keyGenerator.generateKeyPair();
            this.publicKey  = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
            LOG.info("[ECCSecurityManager] ECC key pair generated. Curve: " + Constants.ECC_CURVE);
        } catch (Exception e) {
            throw new RuntimeException("ECC initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt healthcare data using the ECC public key.
     *
     * @param plaintext raw sensor payload bytes
     * @return encrypted ciphertext bytes
     */
    public byte[] secureData(byte[] plaintext) {
        try {
            long start = System.nanoTime();
            byte[] ciphertext = encryptor.encrypt(plaintext, publicKey);
            totalEncryptionLatencyNs += (System.nanoTime() - start);
            encryptionCount++;
            return ciphertext;
        } catch (Exception e) {
            LOG.warning("[ECCSecurityManager] Encryption failed: " + e.getMessage());
            return plaintext; // fallback: pass through unencrypted
        }
    }

    /**
     * Decrypt ciphertext using the ECC private key.
     *
     * @param ciphertext encrypted payload
     * @return original plaintext bytes
     */
    public byte[] recoverData(byte[] ciphertext) {
        try {
            long start = System.nanoTime();
            byte[] plaintext = decryptor.decrypt(ciphertext, privateKey);
            totalDecryptionLatencyNs += (System.nanoTime() - start);
            decryptionCount++;
            return plaintext;
        } catch (Exception e) {
            LOG.warning("[ECCSecurityManager] Decryption failed: " + e.getMessage());
            return ciphertext;
        }
    }

    // ── Simulation-level helpers ──────────────────────────────────────────────

    /**
     * Simulate ECC overhead on a tuple payload size (bytes).
     * Returns the encrypted size (plaintext + overhead).
     */
    public long simulateEncryptedSize(long plaintextBytes) {
        return plaintextBytes + encryptor.estimateOverheadBytes();
    }

    /**
     * Average ECC encryption latency in microseconds (real JVM measurement).
     */
    public double avgEncryptionLatencyUs() {
        if (encryptionCount == 0) return 0.0;
        return (totalEncryptionLatencyNs / (double) encryptionCount) / 1_000.0;
    }

    /**
     * Average ECC decryption latency in microseconds.
     */
    public double avgDecryptionLatencyUs() {
        if (decryptionCount == 0) return 0.0;
        return (totalDecryptionLatencyNs / (double) decryptionCount) / 1_000.0;
    }

    public long getEncryptionCount() { return encryptionCount; }
    public long getDecryptionCount() { return decryptionCount; }
    public PublicKey  getPublicKey()  { return publicKey;  }
    public PrivateKey getPrivateKey() { return privateKey; }

    /**
     * Print ECC performance summary.
     */
    public void printSummary() {
        System.out.println("─── ECC Security Summary ───────────────────────────────");
        System.out.printf("  Encryptions         : %d%n", encryptionCount);
        System.out.printf("  Decryptions         : %d%n", decryptionCount);
        System.out.printf("  Avg Encrypt Latency : %.2f µs%n", avgEncryptionLatencyUs());
        System.out.printf("  Avg Decrypt Latency : %.2f µs%n", avgDecryptionLatencyUs());
        System.out.println("────────────────────────────────────────────────────────");
    }
}

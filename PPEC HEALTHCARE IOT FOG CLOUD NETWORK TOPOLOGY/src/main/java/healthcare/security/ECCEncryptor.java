package healthcare.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import healthcare.utils.Constants;

import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.Security;

/**
 * ECCEncryptor.java
 * Encrypts healthcare payload bytes using ECIES (Elliptic Curve Integrated
 * Encryption Scheme) with BouncyCastle.
 */
public class ECCEncryptor {

    static {
        if (Security.getProvider(Constants.ECC_PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypt plaintext bytes with the recipient's ECC public key.
     *
     * @param data      raw healthcare payload
     * @param publicKey recipient ECC public key
     * @return encrypted ciphertext bytes
     */
    public byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(Constants.ECC_ALGORITHM, Constants.ECC_PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Estimate the overhead added by ECC encryption (bytes).
     * ECIES on secp256r1 adds ~97 bytes overhead on average.
     */
    public int estimateOverheadBytes() {
        return 97;
    }
}

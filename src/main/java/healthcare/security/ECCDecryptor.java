package healthcare.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import healthcare.utils.Constants;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.Security;

/**
 * ECCDecryptor.java
 * Decrypts ECIES-encrypted ciphertext using the holder's ECC private key.
 */
public class ECCDecryptor {

    static {
        if (Security.getProvider(Constants.ECC_PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Decrypt ciphertext bytes with the ECC private key.
     *
     * @param ciphertext encrypted payload
     * @param privateKey holder's ECC private key
     * @return original plaintext bytes
     */
    public byte[] decrypt(byte[] ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(Constants.ECC_ALGORITHM, Constants.ECC_PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }
}

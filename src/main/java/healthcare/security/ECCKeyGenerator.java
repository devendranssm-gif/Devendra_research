package healthcare.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import healthcare.utils.Constants;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * ECCKeyGenerator.java
 * Generates ECC key pairs using BouncyCastle on the secp256r1 curve.
 */
public class ECCKeyGenerator {

    static {
        if (Security.getProvider(Constants.ECC_PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generate a fresh ECC key pair on the configured curve.
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", Constants.ECC_PROVIDER);
        ECGenParameterSpec spec = new ECGenParameterSpec(Constants.ECC_CURVE);
        kpg.initialize(spec, new SecureRandom());
        return kpg.generateKeyPair();
    }

    public PublicKey  getPublicKey(KeyPair kp)  { return kp.getPublic();  }
    public PrivateKey getPrivateKey(KeyPair kp) { return kp.getPrivate(); }
}

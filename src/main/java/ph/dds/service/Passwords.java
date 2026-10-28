package ph.dds.service;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
    private Passwords() {}
    public static String hash(char[] password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return "180000:" + Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(derive(password,salt,180000));
    }
    public static boolean verify(char[] password, String stored) {
        try {
            String[] p=stored.split(":");
            return MessageDigest.isEqual(Base64.getDecoder().decode(p[2]),
                    derive(password,Base64.getDecoder().decode(p[1]),Integer.parseInt(p[0])));
        } catch (RuntimeException ex) { return false; }
    }
    private static byte[] derive(char[] p,byte[] salt,int rounds) {
        PBEKeySpec spec=new PBEKeySpec(p,salt,rounds,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException ex) { throw new IllegalStateException(ex); }
        finally { spec.clearPassword(); }
    }
}

package fi.metropolia.audiostoryutility.security;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class Encrypter {

    private String passEncrypt = "e54FS6Xzq";

    public String encrypt(String message){

        String encryptedMsg = null;

        try {
             encryptedMsg = AESCrypt.encrypt(passEncrypt, message);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return encryptedMsg;
    }

    public String decrypt(String encryptedMessage){
        String decrypted = null;

        try {
            decrypted = AESCrypt.decrypt(passEncrypt, encryptedMessage);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return decrypted;
    }

}

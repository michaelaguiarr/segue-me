package com.segue.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConverteParaMD5 {
	
    /** Converte senha para MD5
     * 
     * @param senhaTexto
     * @return
     */
    public static String toMD5(String senha) {
        String hashMD5;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            BigInteger hash = new BigInteger(1, md.digest(senha.getBytes()));
            hashMD5 = hash.toString(16);
            return hashMD5;
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            return null;
        }
    }

}

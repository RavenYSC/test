package com.raven.valorantcoach;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypt the BYO API key with an Android Keystore-backed AES key. Never log it. */
public final class ApiSecrets {
    private static final String ALIAS="ascend_henrik_api_v1";
    private static final String PREF="api_secrets";
    private static SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
        if(!ks.containsAlias(ALIAS)){
            KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
            g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();
        }
        return (SecretKey)ks.getKey(ALIAS,null);
    }
    public static void save(Context c,String value) throws Exception {
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());
        byte[] encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        SharedPreferences.Editor editor=c.getSharedPreferences(PREF,0).edit();
        editor.putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP));
        editor.putString("cipher",Base64.encodeToString(encrypted,Base64.NO_WRAP));editor.apply();
    }
    public static String load(Context c) throws Exception {
        SharedPreferences p=c.getSharedPreferences(PREF,0);String iv=p.getString("iv",null),ct=p.getString("cipher",null);
        if(iv==null||ct==null)return "";
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)));
        return new String(cipher.doFinal(Base64.decode(ct,Base64.NO_WRAP)),StandardCharsets.UTF_8);
    }
    public static void clear(Context c){c.getSharedPreferences(PREF,0).edit().clear().apply();}
}

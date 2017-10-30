package com.bolyartech.forge.skeleton.dagger.basic.misc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bolyartech.forge.skeleton.dagger.basic.R;

import org.acra.security.KeyStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class AcraKeyStoreFactory implements KeyStoreFactory {

    private static KeyStore createKeystore(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.forge_skeleton);
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("BKS");
            ks.load(is, context.getString(R.string.bks_keystore_password).toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new IllegalStateException("Cannot create the keystore");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }

        return ks;
    }


    @Nullable
    @Override
    public KeyStore create(@NonNull Context context) {
        return createKeystore(context);
    }
}

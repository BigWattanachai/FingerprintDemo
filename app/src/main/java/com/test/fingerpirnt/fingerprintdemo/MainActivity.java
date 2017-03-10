package com.test.fingerpirnt.fingerprintdemo;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_NAME = "key_name";
    private KeyStore mKeyStore;
    private Cipher cipher;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintManager mFingerprintManager;
    private FingerprintHelper mFingerprintHelper;

    TextView fingerPrintStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fingerPrintStatus = (TextView) findViewById(R.id.fingerPrintStatus);

        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        mFingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fingerprint authentication permission not enabled",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (mFingerprintManager.isHardwareDetected()) {
            if (!mKeyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "Lock screen security not enabled in Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "Register at least one fingerprint in Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            generateKey();
            if (initCipher()) {
                mCryptoObject = new FingerprintManager.CryptoObject(cipher);
                mFingerprintHelper = new FingerprintHelper(this);
                mFingerprintHelper.setOnAuthenticationListener(new FingerprintHelper
                        .OnAuthenticationCallBack() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        fingerPrintStatus.setText("Succeeded");
                        startActivity(new Intent(MainActivity.this, LoginSuccessActivity.class));
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        fingerPrintStatus.setText("Failed");
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFingerprintHelper != null) {
            mFingerprintHelper.startAuth(mFingerprintManager, mCryptoObject);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void generateKey() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator mKeyGenerator;
        try {
            mKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            mKeyStore.load(null);
            mKeyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}

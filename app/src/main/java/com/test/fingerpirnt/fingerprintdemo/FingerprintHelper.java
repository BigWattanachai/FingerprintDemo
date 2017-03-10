package com.test.fingerpirnt.fingerprintdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {
    private Context context;
    private OnAuthenticationCallBack mListener;

    FingerprintHelper(Context context) {
        this.context = context;
    }

    void setOnAuthenticationListener(OnAuthenticationCallBack listener) {
        mListener = listener;
    }

    void startAuth(FingerprintManager manager,
                   FingerprintManager.CryptoObject cryptoObject) {

        CancellationSignal cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

//    void stopListening() {
//        if (cancellationSignal != null) {
//            cancellationSignal.cancel();
//            cancellationSignal = null;
//        }
//    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        Log.e("FingerprintHelper", "onAuthenticationError:" + errString);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        Toast.makeText(context,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        mListener.onAuthenticationSucceeded();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        mListener.onAuthenticationFailed();
    }

    interface OnAuthenticationCallBack {
        void onAuthenticationSucceeded();

        void onAuthenticationFailed();
    }
}

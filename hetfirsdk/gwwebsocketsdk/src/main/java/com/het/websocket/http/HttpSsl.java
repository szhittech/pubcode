//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.LoadStoreParameter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HttpSsl {
    public HttpSsl() {
    }

    public static SSLSocketFactory getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String password) {
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            Object trustManager;
            if (trustManagers != null) {
                trustManager = new HttpSsl.MyTrustManager(chooseTrustManager(trustManagers));
            } else {
                trustManager = new HttpSsl.UnSafeTrustManager();
            }

            sslContext.init(keyManagers, new TrustManager[]{(TrustManager)trustManager}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException var7) {
            throw new AssertionError(var7);
        } catch (KeyManagementException var8) {
            throw new AssertionError(var8);
        } catch (KeyStoreException var9) {
            throw new AssertionError(var9);
        }
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates != null && certificates.length > 0) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load((LoadStoreParameter)null);
                int index = 0;
                InputStream[] var4 = certificates;
                int var5 = certificates.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    InputStream certificate = var4[var6];
                    String certificateAlias = Integer.toString(index++);
                    keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                    try {
                        if (certificate != null) {
                            certificate.close();
                        }
                    } catch (IOException var10) {
                        ;
                    }
                }

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                return trustManagers;
            } catch (NoSuchAlgorithmException var11) {
                var11.printStackTrace();
            } catch (CertificateException var12) {
                var12.printStackTrace();
            } catch (KeyStoreException var13) {
                var13.printStackTrace();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

            return null;
        } else {
            return null;
        }
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile != null && password != null) {
                KeyStore clientKeyStore = KeyStore.getInstance("BKS");
                clientKeyStore.load(bksFile, password.toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(clientKeyStore, password.toCharArray());
                return keyManagerFactory.getKeyManagers();
            }

            return null;
        } catch (KeyStoreException var4) {
            var4.printStackTrace();
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
        } catch (UnrecoverableKeyException var6) {
            var6.printStackTrace();
        } catch (CertificateException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return null;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        TrustManager[] var1 = trustManagers;
        int var2 = trustManagers.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            TrustManager trustManager = var1[var3];
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager)trustManager;
            }
        }

        return null;
    }

    private static class MyTrustManager implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var4.init((KeyStore)null);
            this.defaultTrustManager = HttpSsl.chooseTrustManager(var4.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                this.defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException var4) {
                this.localTrustManager.checkServerTrusted(chain, authType);
            }

        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class UnSafeTrustManager implements X509TrustManager {
        private UnSafeTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class UnSafeHostnameVerifier implements HostnameVerifier {
        public UnSafeHostnameVerifier() {
        }

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class UnSafeHostnameVerifier1 implements HostnameVerifier {
        private String host;

        public UnSafeHostnameVerifier1(String host) {
            this.host = host;
        }

        public boolean verify(String hostname, SSLSession session) {
            return this.host != null && !"".equals(this.host) && this.host.contains(hostname);
        }
    }
}

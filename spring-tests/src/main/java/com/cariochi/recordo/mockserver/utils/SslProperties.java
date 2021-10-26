package com.cariochi.recordo.mockserver.utils;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.SecureRandom;

@Getter
public class SslProperties {

    private static final String KEYSTORE_FILE = "../recordo.jks";
    private static final String PASSWORD = "recordo-mock-server";

    private final SSLSocketFactory socketFactory;
    private final X509TrustManager trustManager;
    private final HostnameVerifier hostnameVerifier;
    private final SSLContext sslContext;

    public SslProperties() {
        final KeyStore keyStore = readKeyStore();
        final KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keyStore);
        final TrustManagerFactory trustManagerFactory = getTrustManagerFactory(keyStore);
        sslContext = getSslContext(trustManagerFactory, keyManagerFactory);
        socketFactory = sslContext.getSocketFactory();
        trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
        hostnameVerifier = (host, sslSession) -> true;
    }

    @SneakyThrows
    private SSLContext getSslContext(TrustManagerFactory trustManagerFactory, KeyManagerFactory keyManagerFactory) {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    @SneakyThrows
    private KeyManagerFactory getKeyManagerFactory(KeyStore keyStore) {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, PASSWORD.toCharArray());
        return keyManagerFactory;
    }

    @SneakyThrows
    private TrustManagerFactory getTrustManagerFactory(KeyStore keyStore) {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }

    @SneakyThrows
    private KeyStore readKeyStore() {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // get user password and file input stream
        char[] password = PASSWORD.toCharArray();

        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(KEYSTORE_FILE);
            ks.load(fis, password);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }

}

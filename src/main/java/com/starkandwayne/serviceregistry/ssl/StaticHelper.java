package com.starkandwayne.serviceregistry.ssl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

class StaticHelper {
   X509ExtendedTrustManager systemTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore)null);
      TrustManager[] var2 = trustManagerFactory.getTrustManagers();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         TrustManager trustManager = var2[var4];
         if (trustManager instanceof X509ExtendedTrustManager) {
            return (X509ExtendedTrustManager)trustManager;
         }
      }

      throw new IllegalStateException("Unable to locate system X509 TrustManager");
   }

   TrustManagerFactory jvmDefaultTrustManagerFactory() throws NoSuchProviderException, NoSuchAlgorithmException {
      return TrustManagerFactory.getInstance("PKIX", "SunJSSE");
   }

   KeyStore emptyKeyStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load((KeyStore.LoadStoreParameter)null);
      return keyStore;
   }

   void overrideDefaultSslContext(TrustManager[] trustManagers) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(systemKeyManagers(), trustManagers, (SecureRandom)null);
      SSLContext.setDefault(sslContext);
   }

   SSLContext newTLSContext() throws NoSuchAlgorithmException {
      return SSLContext.getInstance("TLS");
   }

   CertificateCollector newCertificateCollector(X509ExtendedTrustManager delegateTrustManager) throws Exception {
      return new CertificateCollectingX509ExtendedTrustManager(this, delegateTrustManager, Executors.newSingleThreadExecutor((r) -> {
         return new Thread(r, "cert collector");
      }));
   }

   void configureEurekaReplicationClientToUseDefaultSslContext() {
      System.setProperty("com.netflix.eureka.shouldSSLConnectionsUseSystemSocketFactory", "true");
   }

   private static KeyManager[] systemKeyManagers() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init((KeyStore)null, (char[])null);
      return keyManagerFactory.getKeyManagers();
   }
}

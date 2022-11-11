package com.starkandwayne.serviceregistry.ssl;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DynamicX509ExtendedTrustManager extends DelegatingX509ExtendedTrustManager {
   private static final Logger LOG = LoggerFactory.getLogger(DynamicX509ExtendedTrustManager.class);
   private static final Object MONITOR = new Object();
   private final KeyStore keyStore;
   private List trustedCertificates = new ArrayList();
   private TrustManagerFactory delegateTrustManagerFactory;

   DynamicX509ExtendedTrustManager(StaticHelper staticHelper) throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
      super();
      this.delegateTrustManagerFactory = staticHelper.jvmDefaultTrustManagerFactory();
      this.keyStore = staticHelper.emptyKeyStore();
      this.reinitialiseDelegateTrustManager();
   }

   void addCertificate(X509Certificate[] chain) throws KeyStoreException {
      synchronized(MONITOR) {
         int index = this.trustedCertificates.size();
         X509Certificate[] var4 = chain;
         int var5 = chain.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            X509Certificate cert = var4[var6];
            LOG.info("Trusting " + cert.getSubjectX500Principal().getName());
            this.keyStore.setCertificateEntry(Integer.toString(index++), cert);
            this.trustedCertificates.add(cert);
         }

         this.reinitialiseDelegateTrustManager();
      }
   }

   private void reinitialiseDelegateTrustManager() throws KeyStoreException {
      this.delegateTrustManagerFactory.init(this.keyStore);
      TrustManager[] trustManagers = this.delegateTrustManagerFactory.getTrustManagers();
      TrustManager[] var2 = trustManagers;
      int var3 = trustManagers.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         TrustManager trustManager = var2[var4];
         if (trustManager instanceof X509ExtendedTrustManager) {
            this.resetTrustManagers(new X509ExtendedTrustManager[]{(X509ExtendedTrustManager)trustManager});
            return;
         }
      }

      throw new IllegalStateException("Failed to initialise delegate TrustManager");
   }
}

package com.starkandwayne.serviceregistry.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class DynamicCertificateTruster {
   private static final Object MONITOR = new Object();
   private static DynamicCertificateTruster INSTANCE;
   private final StaticHelper staticHelper;
   private final DelegatingX509ExtendedTrustManager delegatingTrustManager;
   private final DynamicX509ExtendedTrustManager dynamicTrustManager;

   public static DynamicCertificateTruster instantiateAndInstall() {
      synchronized(MONITOR) {
         if (INSTANCE == null) {
            try {
               StaticHelper staticHelper = new StaticHelper();
               INSTANCE = new DynamicCertificateTruster(staticHelper, new DynamicX509ExtendedTrustManager(staticHelper), new DelegatingX509ExtendedTrustManager(new X509ExtendedTrustManager[0]));
            } catch (Exception var3) {
               throw new IllegalStateException("Unable to instantiate", var3);
            }
         }

         return INSTANCE;
      }
   }

   DynamicCertificateTruster(StaticHelper staticHelper, DynamicX509ExtendedTrustManager dynamicTrustManager, DelegatingX509ExtendedTrustManager delegatingTrustManager) {
      this.staticHelper = staticHelper;
      this.dynamicTrustManager = dynamicTrustManager;
      this.delegatingTrustManager = delegatingTrustManager;

      try {
         delegatingTrustManager.addDelegateTrustManager(staticHelper.systemTrustManager());
         delegatingTrustManager.addDelegateTrustManager(dynamicTrustManager);
         TrustManager[] trustManagers = new TrustManager[]{delegatingTrustManager};
         staticHelper.overrideDefaultSslContext(trustManagers);
         staticHelper.configureEurekaReplicationClientToUseDefaultSslContext();
      } catch (Exception var5) {
         throw new RuntimeException("Failed to install dynamic certificate truster", var5);
      }
   }

   public void trustCertificate(String host, int port) throws Exception {
      CertificateCollector certCollector = this.staticHelper.newCertificateCollector(this.delegatingTrustManager);
      X509Certificate[] untrustedCertificates = certCollector.getUntrustedCertificates(host, port, 5);
      if (untrustedCertificates != null) {
         this.dynamicTrustManager.addCertificate(untrustedCertificates);
      }
   }
}

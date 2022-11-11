package com.starkandwayne.serviceregistry.ssl;


import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CertificateCollectingX509ExtendedTrustManager extends DelegatingX509ExtendedTrustManager implements CertificateCollector {
   private static final Logger LOG = LoggerFactory.getLogger(CertificateCollectingX509ExtendedTrustManager.class);
   private final ExecutorService executor;
   private final SSLContext sslContext;
   private volatile X509Certificate[] collectedCerts;
   private volatile boolean collectedCertsTrustedByDelegate;

   CertificateCollectingX509ExtendedTrustManager(StaticHelper staticHelper, X509ExtendedTrustManager delegate, ExecutorService executor) throws Exception {
      super(delegate);
      this.executor = executor;
      this.sslContext = staticHelper.newTLSContext();
      this.sslContext.init((KeyManager[])null, new TrustManager[]{this}, (SecureRandom)null);
   }

   public X509Certificate[] getUntrustedCertificates(String host, int port, int timeoutSeconds) throws Exception {
      if (this.collectedCerts != null) {
         throw new IllegalStateException("A certificate chain has already been collected");
      } else {
         this.collectCertificates(host, port, timeoutSeconds);
         if (this.collectedCerts == null) {
            throw new CertificateException("Could not obtain server certificate chain");
         } else if (this.collectedCertsTrustedByDelegate) {
            LOG.debug("Certificates served by {}:{} are already trusted", host, port);
            return null;
         } else {
            return this.collectedCerts;
         }
      }
   }

   private void collectCertificates(String host, int port, int timeoutSeconds) throws Exception {
      SSLSocketFactory factory = this.sslContext.getSocketFactory();
      Future task = this.executor.submit(() -> {
         try {
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            socket.startHandshake();
            socket.close();
         } catch (Exception var4) {
            LOG.debug("Suppressing certificate collection exception", var4);
         }

      });

      try {
         task.get((long)timeoutSeconds, TimeUnit.SECONDS);
      } catch (TimeoutException var7) {
         task.cancel(true);
         throw var7;
      }
   }

   void check(DelegatingX509ExtendedTrustManager.Consumer consumer, X509Certificate[] chain) {
      if (this.collectedCerts != null) {
         throw new IllegalStateException("A certificate chain has already been collected.");
      } else {
         this.collectedCerts = chain;

         try {
            super.check(consumer, chain);
            this.collectedCertsTrustedByDelegate = true;
         } catch (CertificateException var4) {
            this.collectedCertsTrustedByDelegate = false;
         }

      }
   }
}

package com.starkandwayne.serviceregistry.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DelegatingX509ExtendedTrustManager extends X509ExtendedTrustManager {
   private static final Logger LOG = LoggerFactory.getLogger(DelegatingX509ExtendedTrustManager.class);
   private volatile List delegates = new CopyOnWriteArrayList();

   DelegatingX509ExtendedTrustManager(X509ExtendedTrustManager... delegates) {
      this.delegates.addAll(Arrays.asList(delegates));
   }

   void addDelegateTrustManager(X509ExtendedTrustManager trustManager) {
      this.delegates.add(trustManager);
   }

   void resetTrustManagers(X509ExtendedTrustManager... delegates) {
      this.delegates = new CopyOnWriteArrayList(Arrays.asList(delegates));
   }

   public final void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkClientTrusted(chain, authType);
      }, chain);
   }

   public final void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkServerTrusted(chain, authType);
      }, chain);
   }

   public final void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkClientTrusted(chain, authType, socket);
      }, chain);
   }

   public final void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkServerTrusted(chain, authType, socket);
      }, chain);
   }

   public final void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkClientTrusted(chain, authType, engine);
      }, chain);
   }

   public final void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
      this.check((delegate) -> {
         delegate.checkServerTrusted(chain, authType, engine);
      }, chain);
   }

   /*

   public X509Certificate[] getAcceptedIssuers() {
      return (X509Certificate[])this.delegates.stream().map(X509TrustManager::getAcceptedIssuers).flatMap(Arrays::stream).distinct().toArray((x$0) -> {
         return new X509Certificate[x$0];
      });
   } */

   void check(Consumer consumer, X509Certificate[] chain) throws CertificateException {
      CertificateException exception = null;
      Iterator var4 = this.delegates.iterator();

      while(var4.hasNext()) {
         X509ExtendedTrustManager trustManager = (X509ExtendedTrustManager)var4.next();

         try {
            LOG.debug(this.getClass().getSimpleName() + " delegating to " + trustManager);
            consumer.apply(trustManager);
            return;
         } catch (CertificateException var7) {
            exception = var7;
         }
      }

      if (exception != null) {
         throw exception;
      }
   }

   interface Consumer {
      void apply(X509ExtendedTrustManager trustManager) throws CertificateException;
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      // TODO Auto-generated method stub
      return null;
   }
}

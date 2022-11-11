package com.starkandwayne.serviceregistry.ssl;

import java.security.cert.X509Certificate;

interface CertificateCollector {
   X509Certificate[] getUntrustedCertificates(String host, int port, int timeoutSeconds) throws Exception;
}

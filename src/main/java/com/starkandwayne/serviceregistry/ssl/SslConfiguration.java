package com.starkandwayne.serviceregistry.ssl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SslConfiguration {
   @Bean
   DynamicCertificateTruster dynamicCertificateTruster() {
      return DynamicCertificateTruster.instantiateAndInstall();
   }
}

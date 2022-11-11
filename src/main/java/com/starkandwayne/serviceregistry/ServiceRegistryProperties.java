package com.starkandwayne.serviceregistry;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties("scs.service-registry")
public class ServiceRegistryProperties {
   private String appId;
   private String serviceInstanceId;
   private List peerInfos = Collections.emptyList();
   private URI uri;
   private URI authorizationUri;
   private URI issuerUri;
   private URI jwkSetUri;
   private int count;

   @Bean
   public String getAppId() {
      return this.appId;
   }

   @Bean
   public void setAppId(String appId) {
      this.appId = appId;
   }

   @Bean
   public String getServiceInstanceId() {
      return this.serviceInstanceId;
   }

   @Bean
   public void setServiceInstanceId(String serviceInstanceId) {
      this.serviceInstanceId = serviceInstanceId;
   }

   @Bean
   public List getPeerInfos() {
      return this.peerInfos;
   }

   @Bean
   public void setPeerInfos(List peerInfos) {
      this.peerInfos = peerInfos;
   }

   @Bean
   public URI getUri() {
      return this.uri;
   }

   @Bean
   public void setUri(URI uri) {
      this.uri = uri;
   }

   @Bean
   public URI getAuthorizationUri() {
      return this.authorizationUri;
   }

   @Bean
   public void setAuthorizationUri(URI authorizationUri) {
      this.authorizationUri = authorizationUri;
   }

   @Bean
   public URI getIssuerUri() {
      return this.issuerUri;
   }

   @Bean
   public void setIssuerUri(URI issuerUri) {
      this.issuerUri = issuerUri;
   }
   @Bean
   public URI getJwkSetUri() {
      return this.jwkSetUri;
   }
   @Bean
   public void setJwkSetUri(URI jwkSetUri) {
      this.jwkSetUri = jwkSetUri;
   }
   @Bean
   public int getCount() {
      return this.count;
   }
   @Bean
   public void setCount(int count) {
      this.count = count;
   }
   @Bean
   public String toString() {
      return "ServiceRegistryProperties{appId='" + this.appId + '\'' + ", serviceInstanceId='" + this.serviceInstanceId + '\'' + ", peerInfos=" + this.peerInfos + ", uri='" + this.uri + '\'' + ", uaaUri='" + this.authorizationUri + '\'' + ", issuerUri='" + this.issuerUri + '\'' + ", jwkSetUri='" + this.jwkSetUri + '\'' + ", count='" + this.count + '\'' + '}';
   }
}

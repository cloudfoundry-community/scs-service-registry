package com.starkandwayne.serviceregistry.management;

import com.starkandwayne.serviceregistry.ServiceRegistryProperties;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
class PeerInfoContributor implements InfoContributor {
   private final ServiceRegistryProperties serviceRegistryProperties;

   public PeerInfoContributor(ServiceRegistryProperties serviceRegistryProperties) {
      this.serviceRegistryProperties = serviceRegistryProperties;
   }

   public void contribute(Info.Builder builder) {
      builder.withDetail("peers", this.serviceRegistryProperties.getPeerInfos());
   }
}

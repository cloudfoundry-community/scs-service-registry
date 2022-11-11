package com.starkandwayne.serviceregistry.auth;

import com.starkandwayne.serviceregistry.ServiceRegistryProperties;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
class PeerAuthorityChecker {
   private final Set peerAuthorities;

   PeerAuthorityChecker(ServiceRegistryProperties serviceRegistryProperties) {
      this.peerAuthorities = (Set)serviceRegistryProperties.getPeerInfos().stream().map((info) -> {
         return "SCOPE_service_registry_" + ((ServiceRegistryProperties) info).getServiceInstanceId() + ".peer";
      }).collect(Collectors.toSet());
      this.peerAuthorities.add("SCOPE_service_registry_" + serviceRegistryProperties.getServiceInstanceId() + ".peer");
   }

   public boolean check(Authentication authentication) {
      Set grantedAuthorities = (Set)authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
      grantedAuthorities.retainAll(this.peerAuthorities);
      return !grantedAuthorities.isEmpty();
   }
}

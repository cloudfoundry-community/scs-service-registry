package com.starkandwayne.serviceregistry.auth;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.registry.InstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class RegistryResourceChecker {
   private static final Logger LOGGER = LoggerFactory.getLogger(RegistryResourceChecker.class);
   private final InstanceRegistry instanceRegistry;

   public RegistryResourceChecker(InstanceRegistry instanceRegistry) {
      this.instanceRegistry = instanceRegistry;
   }

   public boolean updateCheck(Authentication authentication, String appId, String instanceId) {
      LOGGER.info("inside updateCheck, appId {} and instanceId {}", appId, instanceId);
      InstanceInfo existingInstance = this.instanceRegistry.getInstanceByAppAndId(appId, instanceId);
      if (existingInstance == null) {
         LOGGER.debug("Updating non-existent instance info is allowed: appName: {}, instanceId: {}, principal: {}", new Object[]{appId, instanceId, authentication.getPrincipal()});
         return true;
      } else {
         String requiredAuthorities = (String)existingInstance.getMetadata().get("required_authorities");
         return authentication.getAuthorities().stream().allMatch((authority) -> {
            return requiredAuthorities.contains(authority.getAuthority());
         });
      }
   }
}

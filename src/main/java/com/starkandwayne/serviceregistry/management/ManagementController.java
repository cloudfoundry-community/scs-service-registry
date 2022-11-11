package com.starkandwayne.serviceregistry.management;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ManagementController {
   private ManagementService managementService;

   ManagementController(ManagementService managementService) {
      this.managementService = managementService;
   }

   @GetMapping({"/cli/instances/{serviceInstanceId}/parameters"})
   Map getServiceInstanceConfiguration(@PathVariable String serviceInstanceId, @RequestHeader("Authorization") String token) {
      return this.managementService.getServiceInstanceConfiguration(serviceInstanceId, token);
   }
}

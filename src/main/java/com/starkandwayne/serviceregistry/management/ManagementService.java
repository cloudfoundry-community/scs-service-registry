package com.starkandwayne.serviceregistry.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class ManagementService {
   private static final Logger log = LoggerFactory.getLogger(ManagementService.class);
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private CloudFoundryService cloudFoundryService;

   public ManagementService(CloudFoundryService cloudFoundryService) {
      this.cloudFoundryService = cloudFoundryService;
   }

   Map getServiceInstanceConfiguration(String serviceInstanceId, String token) {
      Map serviceInstanceConfiguration = new HashMap();
      JsonNode applicationResources = this.cloudFoundryService.getApplicationResources(token, serviceInstanceId);
      int count = applicationResources.get("instances").asInt();
      serviceInstanceConfiguration.put("count", count);
      if (!applicationResources.at("/environment_json").has("SPRING_APPLICATION_JSON")) {
         return serviceInstanceConfiguration;
      } else {
         try {
            JsonNode springApplicationNode = (JsonNode)OBJECT_MAPPER.readValue(applicationResources.at("/environment_json/SPRING_APPLICATION_JSON").asText(), JsonNode.class);
            if (springApplicationNode.has("peers")) {
               List peers = new ArrayList();
               JsonNode peersNode = (JsonNode)OBJECT_MAPPER.readValue(springApplicationNode.get("peers").asText(), JsonNode.class);
               peersNode.elements().forEachRemaining((node) -> {
                  peers.add(new Peer(node.get("uri").asText()));
               });
               if (!peers.isEmpty()) {
                  serviceInstanceConfiguration.put("peers", peers);
               }
            }

            return serviceInstanceConfiguration;
         } catch (IOException var9) {
            log.error("Unable to read peer information for service " + serviceInstanceId, var9);
            throw new RuntimeException("Unable to read peer information for service " + serviceInstanceId);
         }
      }
   }

   static class Peer {
      private final String uri;

      public Peer(final String uri) {
         this.uri = uri;
      }

      public String getUri() {
         return this.uri;
      }

      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Peer)) {
            return false;
         } else {
            Peer other = (Peer)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$uri = this.getUri();
               Object other$uri = other.getUri();
               if (this$uri == null) {
                  if (other$uri != null) {
                     return false;
                  }
               } else if (!this$uri.equals(other$uri)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(final Object other) {
         return other instanceof Peer;
      }

      public int hashCode() {
         int PRIME = 1;
         int result = 1;
         Object $uri = this.getUri();
         result = result * 59 + ($uri == null ? 43 : $uri.hashCode());
         return result;
      }

      public String toString() {
         return "ManagementService.Peer(uri=" + this.getUri() + ")";
      }
   }
}

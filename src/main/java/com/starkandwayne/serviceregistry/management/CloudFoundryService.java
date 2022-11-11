package com.starkandwayne.serviceregistry.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

@Service
class CloudFoundryService {
   private static final Logger log = LoggerFactory.getLogger(CloudFoundryService.class);
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private String cfApi;
   private RestTemplate restTemplate;

   CloudFoundryService(RestTemplate restTemplate, @Value("${vcap.application.cf_api:}") String cfApi) {
      this.restTemplate = restTemplate;
      this.cfApi = cfApi;
   }

   JsonNode getApplicationResources(String token, String serviceInstanceId) {
      Assert.notNull(this.cfApi, "vcap.application.cf_api is not set");
      String appsUrl = this.cfApi + this.getAppsUrl(token, this.cfApi, serviceInstanceId);
      HttpEntity httpEntity = this.createEntityWithAuthorization(token);
      String body = (String)this.restTemplate.exchange(appsUrl, HttpMethod.GET, httpEntity, String.class, new Object[0]).getBody();

      try {
         return ((JsonNode)OBJECT_MAPPER.readValue(body, JsonNode.class)).findValue("resources").get(0).get("entity");
      } catch (IOException var7) {
         log.error("Unable to read application {}", var7.getMessage());
         throw new RuntimeException("Unable to read application");
      }
   }

   private String getAppsUrl(String token, String cfApi, String serviceInstanceId) {
      HttpEntity httpEntity = this.createEntityWithAuthorization(token);
      String body = (String)this.restTemplate.exchange(cfApi + "/v2/spaces?q=name:" + serviceInstanceId, HttpMethod.GET, httpEntity, String.class, new Object[0]).getBody();

      try {
         return ((JsonNode)OBJECT_MAPPER.readValue(body, JsonNode.class)).findValue("resources").get(0).get("entity").get("apps_url").asText();
      } catch (Exception var7) {
         log.error("Unable to read spaces {}", var7.getMessage());
         throw new RuntimeException("Unable to read spaces");
      }
   }

   private HttpEntity createEntityWithAuthorization(String token) {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add("Authorization", token);
      return new HttpEntity(httpHeaders);
   }
}

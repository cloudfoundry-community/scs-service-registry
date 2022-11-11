package com.starkandwayne.serviceregistry.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

public class UaaOidcUserService implements OAuth2UserService {
   private final Logger log = LoggerFactory.getLogger(UaaOidcUserService.class);
   private final OidcUserService userService;
   private final String usersApiUrl;
   private final RestTemplate restTemplate;
   private final ObjectMapper objectMapper;

   public UaaOidcUserService(OidcUserService userService, String authorizationUri, RestTemplate restTemplate, ObjectMapper objectMapper) {
      this.userService = userService;
      this.usersApiUrl = this.getUsersEndpoint(authorizationUri);
      this.restTemplate = restTemplate;
      this.objectMapper = objectMapper;
   }

   public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
      OidcUser oidcUser = this.userService.loadUser(userRequest);
      Map claims = userRequest.getIdToken().getClaims();
      if (!claims.containsKey("sub")) {
         this.log.warn("No sub claim found, can't fetch user groups from UAA");
         return oidcUser;
      } else {
         String userId = claims.get("sub").toString();
         String authHeader = "Bearer " + userRequest.getAccessToken().getTokenValue();
         RequestEntity uaaUserRequest = RequestEntity.get(URI.create(this.usersApiUrl + userId)).header("Authorization", new String[]{authHeader}).build();
         ResponseEntity uaaUserResponse = this.restTemplate.exchange(uaaUserRequest, String.class);
         if (uaaUserResponse.getStatusCode() != HttpStatus.OK) {
            this.log.warn("Failed to fetch user details from UAA: " + uaaUserResponse.getStatusCode());
            return oidcUser;
         } else {
            try {
               Set authorities = new HashSet(oidcUser.getAuthorities());
               JsonNode groups = this.objectMapper.readTree((String)uaaUserResponse.getBody()).get("groups");
               Iterator var10 = groups.iterator();

               while(var10.hasNext()) {
                  JsonNode group = (JsonNode)var10.next();
                  String role = "ROLE_" + group.get("display").asText().trim();
                  SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                  authorities.add(authority);
                  this.log.debug("Added {} authority to user with id {}", role, userId);
               }

               this.log.info("Registered {} authorities for user {}", authorities.size(), userId);
               return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            } catch (IOException var14) {
               this.log.error("Failed to parse user info from UAA", var14);
               return oidcUser;
            }
         }
      }
   }

   private String getUsersEndpoint(String authorizationUri) {
      return authorizationUri.endsWith("/") ? authorizationUri + "Users/" : authorizationUri + "/Users/";
   }

   @Override
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      // TODO Auto-generated method stub
      return null;
   }
}

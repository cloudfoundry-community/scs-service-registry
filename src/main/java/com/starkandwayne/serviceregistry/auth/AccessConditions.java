package com.starkandwayne.serviceregistry.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AccessConditions implements AccessBuilder {
   private List accesses = new ArrayList();
   private Conjunction conjunction = new Conjunction(this);

   public static AccessBuilder instance() {
      return new AccessConditions();
   }

   public Conjunction hasAdminScope() {
      this.accesses.add("hasAuthority('SCOPE_cloud_controller.admin')");
      return this.conjunction;
   }

   public Conjunction hasAdminRole() {
      this.accesses.add("hasRole('cloud_controller.admin')");
      return this.conjunction;
   }

   public Conjunction isSpaceDeveloper() {
      this.accesses.add("@spaceDeveloperChecker.check(authentication)");
      return this.conjunction;
   }

   public Conjunction hasAuthority(String authority) {
      this.accesses.add("hasAuthority('" + authority + "')");
      return this.conjunction;
   }

   public Conjunction access(String accessString) {
      this.accesses.add(accessString);
      return this.conjunction;
   }

   public Conjunction nested(String nestedAccessString) {
      this.accesses.add("(" + nestedAccessString + ")");
      return this.conjunction;
   }

   public Conjunction nested(AccessBuilder nestedBuilder) {
      this.accesses.add("(" + nestedBuilder.build() + ")");
      return this.conjunction;
   }

   public String build() {
      return (String)this.accesses.stream().collect(Collectors.joining(" "));
   }

   public static class Conjunction implements AccessBuilder {
      private AccessConditions accessBuilder;

      public Conjunction(AccessConditions accessBuilder) {
         this.accessBuilder = accessBuilder;
      }

      public AccessConditions or() {
         this.accessBuilder.accesses.add("||");
         return this.accessBuilder;
      }

      public AccessConditions and() {
         this.accessBuilder.accesses.add("&&");
         return this.accessBuilder;
      }

      public String build() {
         return this.accessBuilder.build();
      }
   }
}

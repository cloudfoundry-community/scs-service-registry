package com.starkandwayne.serviceregistry.auth;

interface AccessBuilder {
   String build();

   static AccessConditions instance() {
      return new AccessConditions();
   }
}

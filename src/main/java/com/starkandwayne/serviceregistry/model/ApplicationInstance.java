package com.starkandwayne.serviceregistry.model;

public class ApplicationInstance {
   private final String id;
   private final String url;

   public ApplicationInstance(String id, String url) {
      this.id = id;
      this.url = url;
   }

   public String getId() {
      return this.id;
   }

   public String getUrl() {
      return this.url;
   }

   public boolean isHref() {
      return this.url.startsWith("http");
   }
}

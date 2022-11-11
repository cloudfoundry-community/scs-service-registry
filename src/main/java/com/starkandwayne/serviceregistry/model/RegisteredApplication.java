package com.starkandwayne.serviceregistry.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisteredApplication {
   private final String name;
   private Map zoneCounts = new HashMap();
   private Map instancesByStatus = new HashMap();

   public RegisteredApplication(String appName) {
      this.name = appName;
   }

   public String getName() {
      return this.name;
   }

   public Map getZones() {
      return this.zoneCounts;
   }

   public Map getInstances() {
      return this.instancesByStatus;
   }

   public void incrementZoneCount(String zoneName) {
      Integer count = (Integer)this.zoneCounts.get(zoneName);
      if (count != null) {
         this.zoneCounts.put(zoneName, count + 1);
      } else {
         this.zoneCounts.put(zoneName, 1);
      }

   }

   public void addInstanceWithStatus(String status, ApplicationInstance instance) {
      List instances = (List)this.instancesByStatus.get(status);
      if (instances == null) {
         instances = new ArrayList();
         this.instancesByStatus.put(status, instances);
      }

      ((List)instances).add(instance);
   }
}

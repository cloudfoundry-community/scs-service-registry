package com.starkandwayne.serviceregistry.model;


import com.netflix.discovery.shared.Pair;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class History {
   private List registered = new ArrayList();
   private List canceled = new ArrayList();

   public List getRegisteredInstances() {
      return this.registered;
   }

   public void addRegisteredInstances(List instances) {
      Iterator var2 = instances.iterator();

      while(var2.hasNext()) {
         Pair instance = (Pair)var2.next();
         this.registered.add(new Instance((Long)instance.first(), (String)instance.second()));
      }

   }

   public List getCanceledInstances() {
      return this.canceled;
   }

   public void addCanceledInstances(List instances) {
      Iterator var2 = instances.iterator();

      while(var2.hasNext()) {
         Pair instance = (Pair)var2.next();
         this.canceled.add(new Instance((Long)instance.first(), (String)instance.second()));
      }

   }

   class Instance {
      private final Date timestamp;
      private final String id;

      public Instance(long timestamp, String id) {
         this.timestamp = new Date(timestamp);
         this.id = id;
      }

      public Date getTimestamp() {
         return this.timestamp;
      }

      public String getId() {
         return this.id;
      }
   }
}

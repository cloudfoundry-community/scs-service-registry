package com.starkandwayne.serviceregistry.model;


import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public class RegistryInfo {
   private final boolean leaseExpirationEnabled;
   private final int numOfRenewsPerMinThreshold;
   private final long numOfRenewsInLastMin;
   private final boolean selfPreservationModeEnabled;
   private final boolean belowRenewThreshold;

   public RegistryInfo(PeerAwareInstanceRegistry registry) {
      this.leaseExpirationEnabled = registry.isLeaseExpirationEnabled();
      this.numOfRenewsPerMinThreshold = registry.getNumOfRenewsPerMinThreshold();
      this.numOfRenewsInLastMin = registry.getNumOfRenewsInLastMin();
      this.selfPreservationModeEnabled = registry.isSelfPreservationModeEnabled();
      this.belowRenewThreshold = registry.isBelowRenewThresold() == 1;
   }

   public boolean isLeaseExpirationEnabled() {
      return this.leaseExpirationEnabled;
   }

   public int getNumOfRenewsPerMinThreshold() {
      return this.numOfRenewsPerMinThreshold;
   }

   public long getNumOfRenewsInLastMin() {
      return this.numOfRenewsInLastMin;
   }

   public boolean isSelfPreservationModeEnabled() {
      return this.selfPreservationModeEnabled;
   }

   public boolean isBelowRenewThreshold() {
      return this.belowRenewThreshold;
   }
}

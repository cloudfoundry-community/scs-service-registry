package com.starkandwayne.serviceregistry.model;


import java.util.Map;

public class ServerStatus {
   private Map generalStats;
   private Map applicationStats;
   private String upTime;
   private RegistryInfo registryInfo;
   private InstanceStatus instanceStatus;
   private HaProperties haProperties;
   private String version;

   public void setGeneralStats(Map generalStats) {
      this.generalStats = generalStats;
   }

   public Map getGeneralStats() {
      return this.generalStats;
   }

   public void setApplicationStats(Map applicationStats) {
      this.applicationStats = applicationStats;
   }

   public Map getApplicationStats() {
      return this.applicationStats;
   }

   public void setUpTime(String upTime) {
      this.upTime = upTime;
   }

   public String getUpTime() {
      return this.upTime;
   }

   public void setRegistryInfo(RegistryInfo registryInfo) {
      this.registryInfo = registryInfo;
   }

   public RegistryInfo getRegistryInfo() {
      return this.registryInfo;
   }

   public void setInstanceStatus(InstanceStatus instanceStatus) {
      this.instanceStatus = instanceStatus;
   }

   public InstanceStatus getInstanceStatus() {
      return this.instanceStatus;
   }

   public void setHaProperties(HaProperties haProperties) {
      this.haProperties = haProperties;
   }

   public HaProperties getHaProperties() {
      return this.haProperties;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getVersion() {
      return this.version;
   }
}

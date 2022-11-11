package com.starkandwayne.serviceregistry.model;


import com.netflix.appinfo.InstanceInfo;

public class InstanceStatus {
   private final String IpAddress;
   private final String status;

   public InstanceStatus(InstanceInfo instanceInfo) {
      this.IpAddress = instanceInfo.getIPAddr();
      this.status = instanceInfo.getStatus().name();
   }

   public String getIpAddress() {
      return this.IpAddress;
   }

   public String getStatus() {
      return this.status;
   }
}

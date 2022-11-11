package com.starkandwayne.serviceregistry.web;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.resources.StatusResource;
import com.netflix.eureka.util.StatusInfo;
import com.starkandwayne.serviceregistry.ServiceRegistryProperties;
import com.starkandwayne.serviceregistry.model.ApplicationInstance;
import com.starkandwayne.serviceregistry.model.HaProperties;
import com.starkandwayne.serviceregistry.model.History;
import com.starkandwayne.serviceregistry.model.InstanceStatus;
import com.starkandwayne.serviceregistry.model.RegisteredApplication;
import com.starkandwayne.serviceregistry.model.RegisteredApplications;
import com.starkandwayne.serviceregistry.model.RegistryInfo;
import com.starkandwayne.serviceregistry.model.ServerStatus;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/dashboard"})
public class EurekaController {
   private static final Logger log = LoggerFactory.getLogger(EurekaController.class);
   private final PeerAwareInstanceRegistry registry;
   private final ServiceRegistryProperties serviceRegistryProperties;
   @Value("${VERSION:unknown}")
   private String version;

   @RequestMapping({"/apps"})
   public List getApplications() {
      return this.populateApps();
   }

   @RequestMapping({"/status"})
   public ServerStatus getStatus() {
      return this.populateStatus();
   }

   @RequestMapping({"/appstatus"})
   @Cacheable(
      cacheNames = {"appStatusCache"},
      cacheManager = "appStatusCache"
   )
   public Map getApplicationsAndStatus() {
      Map result = new HashMap();
      result.put("applications", this.populateApps());
      result.put("serverStatus", this.populateStatus());
      result.put("peerInfos", this.serviceRegistryProperties.getPeerInfos());
      return result;
   }

   @RequestMapping({"/history"})
   public History getHistory() {
      return this.populateHistory();
   }

   @RequestMapping(
      value = {"/purge"},
      method = {RequestMethod.POST}
   )
   public void purge() {
      this.purgeDownInstances();
   }

   @RequestMapping({"/userinfo"})
   public Principal userInfo(Principal user) {
      return user;
   }

   @Scheduled(
      fixedRate = 1000L
   )
   @CacheEvict(
      cacheNames = {"appStatusCache"},
      cacheManager = "appStatusCache"
   )
   public void evict() {
   }

   private List populateApps() {
      List sortedApplications = this.registry.getSortedApplications();
      RegisteredApplications applications = new RegisteredApplications(sortedApplications.size());
      Iterator var3 = sortedApplications.iterator();

      while(var3.hasNext()) {
         Application app = (Application)var3.next();
         RegisteredApplication regApp = new RegisteredApplication(app.getName());
         applications.add(regApp);
         Iterator var6 = app.getInstances().iterator();

         while(var6.hasNext()) {
            InstanceInfo info = (InstanceInfo)var6.next();
            String zone = "default";
            regApp.incrementZoneCount(zone);
            ApplicationInstance appInstance = new ApplicationInstance(info.getId(), info.getStatusPageUrl());
            InstanceInfo.InstanceStatus status = info.getStatus();
            regApp.addInstanceWithStatus(status.name(), appInstance);
         }
      }

      return applications;
   }

   private ServerStatus populateStatus() {
      ServerStatus status = new ServerStatus();
      status.setUpTime(StatusInfo.getUpTime());
      StatusInfo statusInfo = (new StatusResource()).getStatusInfo();
      status.setGeneralStats(statusInfo.getGeneralStats());
      status.setApplicationStats(statusInfo.getApplicationStats());
      status.setRegistryInfo(this.populateRegistryInfo());
      status.setInstanceStatus(this.populateInstanceStatus());
      HaProperties haProperties = new HaProperties();
      haProperties.setPeers(this.serviceRegistryProperties.getPeerInfos());
      haProperties.setUri(URI.create("https://" + this.serviceRegistryProperties.getUri()));
      haProperties.setNodes(this.serviceRegistryProperties.getCount());
      status.setHaProperties(haProperties);
      status.setVersion(this.version);
      return status;
   }

   private RegistryInfo populateRegistryInfo() {
      return new RegistryInfo(this.registry);
   }

   private InstanceStatus populateInstanceStatus() {
      return new InstanceStatus((new StatusResource()).getStatusInfo().getInstanceInfo());
   }

   private History populateHistory() {
      History history = new History();
      history.addRegisteredInstances(this.registry.getLastNRegisteredInstances());
      history.addCanceledInstances(this.registry.getLastNCanceledInstances());
      return history;
   }

   private void purgeDownInstances() {
      List applications = this.registry.getApplications().getRegisteredApplications();
      Iterator var2 = applications.iterator();

      while(var2.hasNext()) {
         Application app = (Application)var2.next();
         Iterator var4 = app.getInstances().iterator();

         while(var4.hasNext()) {
            InstanceInfo info = (InstanceInfo)var4.next();
            InstanceInfo.InstanceStatus status = info.getStatus();
            if (status == com.netflix.appinfo.InstanceInfo.InstanceStatus.DOWN) {
               log.info(String.format("Purging instance %s:%s", info.getAppName(), info.getId()));
               this.registry.cancel(info.getAppName(), info.getId(), false);
            }
         }
      }

   }

   @Autowired
   public EurekaController(final PeerAwareInstanceRegistry registry, final ServiceRegistryProperties serviceRegistryProperties) {
      this.registry = registry;
      this.serviceRegistryProperties = serviceRegistryProperties;
   }
}

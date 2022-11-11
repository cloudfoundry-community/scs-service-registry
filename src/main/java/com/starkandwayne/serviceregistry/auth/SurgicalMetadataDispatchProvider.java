package com.starkandwayne.serviceregistry.auth;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.resources.ApplicationResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.server.impl.model.method.dispatch.EntityParamDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class SurgicalMetadataDispatchProvider extends EntityParamDispatchProvider {
   private static final Logger LOGGER = LoggerFactory.getLogger(SurgicalMetadataDispatchProvider.class);
   static final String REQUIRED_AUTHORITIES_KEY = "required_authorities";

   public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
      return this.create(abstractResourceMethod, (method, resource, parameters) -> {
         if (this.isAddingInstance(method, resource)) {
            InstanceInfo instanceInfo = (InstanceInfo)this.findInParameters(InstanceInfo.class, parameters);
            this.setMetadata(instanceInfo, "required_authorities", this.getAuthorities());
         }

         return method.invoke(resource, parameters);
      });
   }

   private String getAuthorities() {
      return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
   }

   private boolean isAddingInstance(Method method, Object resource) {
      if (!ApplicationResource.class.isAssignableFrom(resource.getClass())) {
         return false;
      } else {
         return "addInstance".equals(method.getName());
      }
   }

   private void setMetadata(InstanceInfo instanceInfo, String key, String value) {
      Map metadataMap = instanceInfo.getMetadata();
      if (Collections.emptyMap().getClass().equals(metadataMap.getClass())) {
         metadataMap = new ConcurrentHashMap();
         InstanceInfo.Builder builder = new InstanceInfo.Builder(instanceInfo);
         builder.setMetadata((Map)metadataMap);
      }

      ((Map)metadataMap).put(key, value);
   }

   private Object findInParameters(Class type, Object[] parameters) {
      Object[] var3 = parameters;
      int var4 = parameters.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Object param = var3[var5];
         if (type.isAssignableFrom(param.getClass())) {
            return param;
         }
      }

      return null;
   }
}

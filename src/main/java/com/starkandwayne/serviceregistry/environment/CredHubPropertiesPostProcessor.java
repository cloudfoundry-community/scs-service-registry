package com.starkandwayne.serviceregistry.environment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.credhub.configuration.CredHubTemplateFactory;
import org.springframework.credhub.core.CredHubException;
import org.springframework.credhub.core.CredHubOperations;
import org.springframework.credhub.core.CredHubProperties;
import org.springframework.credhub.support.ClientOptions;
import org.springframework.credhub.support.SimpleCredentialName;
import org.springframework.credhub.support.json.JsonCredential;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

class CredHubPropertiesPostProcessor implements EnvironmentPostProcessor, ApplicationListener {
   private static final DeferredLog log = new DeferredLog();

   public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
      String runtimeCredHubUrl = environment.getProperty("credentials.credhub-url");
      if (StringUtils.isEmpty(runtimeCredHubUrl)) {
         log.warn("credentials.credhub-url is not set, skipping CredHub properties loading");
      } else {
         String credhubRef = environment.getProperty("credentials.credhub-ref");
         if (StringUtils.isEmpty(credhubRef)) {
            log.warn("credentials.credhub-ref is not set, skipping CredHub properties loading");
         } else {
            CredHubOperations credHubOperations = this.buildMtlsCredHubOperations(runtimeCredHubUrl);
            log.info(String.format("Loading properties from CredHub %s, using credentials-ref %s", runtimeCredHubUrl, credhubRef));
            credhubRef = credhubRef.replace("((", "").replace("))", "");

            JsonCredential value;
            try {
               value = (JsonCredential)credHubOperations.credentials().getByName(new SimpleCredentialName(new String[]{credhubRef}), JsonCredential.class).getValue();
            } catch (CredHubException var8) {
               if (var8.getStatusCode() == HttpStatus.NOT_FOUND) {
                  System.err.println(String.format("%s credential not found in CredHub", credhubRef));
                  return;
               }

               throw var8;
            } catch (ResourceAccessException var9) {
               log.error("Can't get credentials from CredHub");
               var9.printStackTrace(System.err);
               return;
            }

            MapPropertySource credhubPropertySource = new MapPropertySource("credhub", value);
            environment.getPropertySources().addLast(credhubPropertySource);
            log.info(String.format("Registered %s properties from %s", value.size(), credhubRef));
         }
      }
   }

   private CredHubOperations buildMtlsCredHubOperations(String runtimeCredHubUrl) {
      CredHubProperties credHubProperties = new CredHubProperties();
      credHubProperties.setUrl(runtimeCredHubUrl);
      CredHubTemplateFactory credHubTemplateFactory = new CredHubTemplateFactory();
      return credHubTemplateFactory.credHubTemplate(credHubProperties, new ClientOptions());
   }

   public void onApplicationEvent(ApplicationEvent event) {
      log.replayTo(CredHubPropertiesPostProcessor.class);
   }
}

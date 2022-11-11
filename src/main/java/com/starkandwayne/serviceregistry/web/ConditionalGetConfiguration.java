package com.starkandwayne.serviceregistry.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class ConditionalGetConfiguration {
   @Bean
   public FilterRegistrationBean etagFilterRegistration() {
      FilterRegistrationBean filterReg = new FilterRegistrationBean();
      filterReg.setFilter(this.etagFilter());
      filterReg.addUrlPatterns(new String[]{"/dashboard/appstatus"});
      filterReg.setName("etagFilter");
      return filterReg;
   }

   @Bean
   public ShallowEtagHeaderFilter etagFilter() {
      return new ShallowEtagHeaderFilter();
   }
}

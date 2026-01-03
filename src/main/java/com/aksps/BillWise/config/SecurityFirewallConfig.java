package com.aksps.BillWise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.DefaultHttpFirewall;

@Configuration
public class SecurityFirewallConfig {

    @Bean
    public HttpFirewall allowEncodedCharsHttpFirewall() {
        // DefaultHttpFirewall is more permissive than StrictHttpFirewall and avoids
        // rejecting encoded characters such as "%0A". Use this to prevent
        // RequestRejectedException caused by encoded newlines.
        return new DefaultHttpFirewall();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall firewall) {
        return web -> web.httpFirewall(firewall);
    }
}

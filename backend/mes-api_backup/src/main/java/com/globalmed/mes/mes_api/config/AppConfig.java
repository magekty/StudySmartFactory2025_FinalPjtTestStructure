// src/main/java/com/globalmed/mes/mes_api/config/AppConfig.java
package com.globalmed.mes.mes_api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ ShadowProps.class })
public class AppConfig {}
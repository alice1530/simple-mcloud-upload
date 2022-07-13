package com.alice1530.mcloud.config;

import com.alice1530.mcloud.client.MCloudClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MCloudProperties.class)
public class MCloudAutoConfig {

    @Autowired
    private MCloudProperties MCloudProperties;

    @Bean
    public MCloudClient mCloudClient(){
        return new MCloudClient(MCloudProperties);
    }



}

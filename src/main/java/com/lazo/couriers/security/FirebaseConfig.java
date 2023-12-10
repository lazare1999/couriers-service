package com.lazo.couriers.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;



/**
 * Created by Lazo on 2022-05-24
 */

@Configuration
public class FirebaseConfig {


    @Bean
    public DatabaseReference firebaseDatabase() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Value("${co.module.firebase.database.url}")
    private String databaseUrl;

    @Value("${co.module.firebase.config.path}")
    private String configPath;

    @PostConstruct
    public void init() {

        InputStream inputStream = FirebaseConfig.class.getClassLoader().getResourceAsStream(configPath);

        FirebaseOptions options;
        try {
            if (inputStream != null) {
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

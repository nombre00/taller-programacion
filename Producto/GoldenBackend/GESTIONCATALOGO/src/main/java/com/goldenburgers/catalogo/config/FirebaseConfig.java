package com.goldenburgers.catalogo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path}")
    private Resource firebaseCredentialsPath;

    @Value("${firebase.storage.bucket:goldenburgers-60680.firebasestorage.app}")
    private String storageBucket;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = firebaseCredentialsPath.getInputStream();
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK inicializado exitosamente con bucket: {}", storageBucket);
            } else {
                logger.info("Firebase Admin SDK ya estaba inicializado");
            }
        } catch (IOException e) {
            logger.error("Error al inicializar Firebase Admin SDK: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar Firebase Admin SDK", e);
        }
    }

    @Bean
    public FirebaseApp firebaseApp() {
        return FirebaseApp.getInstance();
    }

    @Bean
    public Storage storage() throws IOException {
        InputStream serviceAccount = firebaseCredentialsPath.getInputStream();
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("goldenburgers-60680")
                .build()
                .getService();
    }
}
package com.goldenburgers.apigateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuración de Firebase Admin SDK
 *
 * IMPORTANTE: Para usar esta configuración necesitas:
 * 1. Descargar la clave privada de Firebase Admin SDK desde la consola de Firebase
 * 2. Colocar el archivo JSON en src/main/resources/firebase-credentials.json
 * O
 * 3. Configurar la variable de entorno FIREBASE_CREDENTIALS_PATH con la ruta al archivo
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path}")
    private Resource firebaseCredentialsPath;

    /**
     * Inicializa Firebase App al arrancar la aplicación
     */
    @PostConstruct
    public void initialize() {
        try {
            // Verifica si Firebase ya fue inicializado
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = firebaseCredentialsPath.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);

                logger.info("Firebase Admin SDK inicializado exitosamente");
            } else {
                logger.info("Firebase Admin SDK ya estaba inicializado");
            }
        } catch (IOException e) {
            logger.error("Error al inicializar Firebase Admin SDK: {}", e.getMessage());
            logger.error("IMPORTANTE: Debes colocar el archivo de credenciales de Firebase en:");
            logger.error("  - src/main/resources/firebase-credentials.json");
            logger.error("  O configurar la variable de entorno FIREBASE_CREDENTIALS_PATH");
            throw new RuntimeException("No se pudo inicializar Firebase Admin SDK", e);
        }
    }

    /**
     * Bean de FirebaseApp para inyección de dependencias
     */
    @Bean
    public FirebaseApp firebaseApp() {
        return FirebaseApp.getInstance();
    }
}

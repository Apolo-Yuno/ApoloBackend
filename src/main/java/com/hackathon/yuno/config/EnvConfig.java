package com.hackathon.yuno.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Carga variables de entorno desde archivo .env
 * Se ejecuta automáticamente al iniciar la aplicación
 */
@Configuration
@Slf4j
public class EnvConfig {

    public EnvConfig() {
        log.info("🔧 Cargando variables de entorno desde .env...");
        
        // Cargar variables del archivo .env (si existe)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        // Asignar cada variable de entorno como propiedad del sistema
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            log.debug("✓ Variable cargada: {}", entry.getKey());
        });
        
        log.info("✅ Variables de entorno cargadas correctamente");
    }
}

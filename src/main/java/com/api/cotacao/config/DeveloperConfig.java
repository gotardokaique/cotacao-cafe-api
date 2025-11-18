package com.api.cotacao.config;

import com.api.cotacao.dev.DevSqlLogger;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeveloperConfig {

    private static final Logger log = LoggerFactory.getLogger(DeveloperConfig.class);

    private final Environment env;

    public DeveloperConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        String raw = env.getProperty("app.developer");
        log.info("app.developer from Environment = '{}'", raw);

        boolean dev = Boolean.parseBoolean(raw);
        DevSqlLogger.setDeveloperMode(dev);
        
        String modo = "desativado";
        
        if (dev) {
        	modo = "ativado.";
        }
        
        log.info("Modo de desenvolvimento "+ modo, dev);
    }
}

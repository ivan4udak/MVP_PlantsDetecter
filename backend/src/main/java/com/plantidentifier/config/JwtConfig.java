// config/JwtConfig.java
package com.plantidentifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Типобезопасное чтение JWT настроек из application.properties.
 *
 * @ConfigurationProperties(prefix = "jwt") — читает все
 * свойства начинающиеся с "jwt." и маппит на поля класса:
 *   jwt.secret     → secret
 *   jwt.expiration → expiration
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long   expiration;

    // Геттеры и сеттеры нужны для @ConfigurationProperties
    public String getSecret()              { return secret; }
    public void   setSecret(String secret) { this.secret = secret; }

    public long   getExpiration()                { return expiration; }
    public void   setExpiration(long expiration) { this.expiration = expiration; }
}

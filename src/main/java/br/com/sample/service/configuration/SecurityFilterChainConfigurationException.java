package br.com.sample.service.configuration;

public final class SecurityFilterChainConfigurationException extends RuntimeException {
    public SecurityFilterChainConfigurationException(Exception cause) {
        super("Não foi possível configurar a cadeia de filtros de segurança.", cause);
    }
}

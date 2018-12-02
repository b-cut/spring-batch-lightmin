package org.tuxdevelop.spring.batch.lightmin.client.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.batch.lightmin.client")
public class LightminClientProperties {

    @Setter
    private String managementUrl;
    @Setter
    private String serviceUrl;
    @Setter
    private String healthUrl;
    @Getter
    @Setter
    private boolean preferIp = false;
    @Getter
    @Setter
    private Integer serverPort;
    @Getter
    @Setter
    private Integer managementPort;
    @Getter
    @Setter
    private Boolean publishJobEvents = Boolean.TRUE;
    @Getter
    @Setter
    private Map<String, String> externalLinks = new HashMap<>();
    @Getter
    @Setter
    private String hostname;

    @Getter
    private final String name;
    @Getter
    private final String healthEndpointId;
    @Getter
    @Setter
    @NestedConfigurationProperty
    private ClientServerProperties server = new ClientServerProperties();

    private final ManagementServerProperties managementServerProperties;
    private final ServerProperties serverProperties;

    @Autowired
    public LightminClientProperties(final ManagementServerProperties managementServerProperties,
                                    final ServerProperties serverProperties,
                                    @Value("${spring.application.name:spring-boot-application}") final String name,
                                    @Value("${endpoints.health.id:health}") final String healthEndpointId) {
        this.name = name;
        this.healthEndpointId = healthEndpointId;
        this.managementServerProperties = managementServerProperties;
        this.serverProperties = serverProperties;
    }

    //TODO: refactor to only one return statement
    public String getManagementUrl() {
        if (this.managementUrl != null) {
            return this.managementUrl;
        }

        if ((this.managementPort == null || this.managementPort.equals(this.serverPort))
                && this.getServiceUrl() != null) {
            return this.append(this.getServiceUrl(), this.managementServerProperties.getServlet().getContextPath());
        }

        if (this.managementPort == null) {
            throw new IllegalStateException(
                    "serviceUrl must be set when deployed to servlet-container");
        }

        if (this.preferIp) {
            final InetAddress address = this.serverProperties.getAddress();
            final String hostAddress;
            if (address == null) {
                hostAddress = this.determineHost();
            } else {
                hostAddress = address.getHostAddress();
            }
            return this.append(this.append(this.createLocalUri(hostAddress, this.managementPort),
                    this.serverProperties.getServlet().getContextPath()), this.managementServerProperties.getServlet().getContextPath());

        }
        return this.append(this.createLocalUri(this.determineHost(), this.managementPort),
                this.managementServerProperties.getServlet().getContextPath());
    }

    public String getHealthUrl() {
        if (this.healthUrl != null) {
            return this.healthUrl;
        }
        final String managementUrl = this.getManagementUrl();
        return this.append(managementUrl, this.healthEndpointId);
    }

    //TODO: refactor to only one return statement
    public String getServiceUrl() {
        if (this.serviceUrl != null) {
            return this.serviceUrl;
        }

        if (this.serverPort == null) {
            throw new IllegalStateException(
                    "serviceUrl must be set when deployed to servlet-container");
        }

        if (this.preferIp) {
            final InetAddress address = this.serverProperties.getAddress();
            final String hostAddress;
            if (address == null) {
                hostAddress = this.determineHost();
            } else {
                hostAddress = address.getHostAddress();
            }
            return this.append(this.append(this.createLocalUri(hostAddress, this.serverPort),
                    this.serverProperties.getServlet().getContextPath()), this.serverProperties.getServlet().getContextPath());

        }
        return this.append(this.append(this.createLocalUri(this.determineHost(), this.serverPort),
                this.serverProperties.getServlet().getContextPath()), this.serverProperties.getServlet().getContextPath());
    }

    private String createLocalUri(final String host, final int port) {
        final String scheme = this.serverProperties.getSsl() != null && this.serverProperties.getSsl().isEnabled() ? "https" : "http";
        return scheme + "://" + host + ":" + port;
    }

    //TODO: refactor to only one return statement
    private String append(final String uri, final String path) {
        final String baseUri = uri.replaceFirst("/+$", "");
        if (StringUtils.isEmpty(path)) {
            return baseUri;
        }

        final String normPath = path.replaceFirst("^/+", "").replaceFirst("/+$", "");
        return baseUri + "/" + normPath;
    }

    private InetAddress getHostAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private String determineHost() {
        final String host;
        if (StringUtils.hasText(this.hostname)) {
            host = this.hostname;
        } else {
            final InetAddress inetAddress = this.getHostAddress();
            host = inetAddress.getCanonicalHostName();
        }
        return host;
    }

    @Data
    public static class ClientServerProperties {
        private String username;
        private String password;
    }

}

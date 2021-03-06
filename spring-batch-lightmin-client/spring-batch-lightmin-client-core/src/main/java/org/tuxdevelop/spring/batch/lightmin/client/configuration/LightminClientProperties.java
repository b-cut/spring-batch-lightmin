package org.tuxdevelop.spring.batch.lightmin.client.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
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

    public String getManagementUrl() {

        final String resultManagamentUrl;

        if (this.managementUrl != null) {
            resultManagamentUrl = this.managementUrl;
        } else {
            if ((this.managementPort == null || this.managementPort.equals(this.serverPort))
                    && this.getServiceUrl() != null) {
                resultManagamentUrl = this.append(this.getServiceUrl(), this.managementServerProperties.getContextPath());
            } else {
                if (this.managementPort == null) {
                    throw new IllegalStateException(
                            "serviceUrl must be set when deployed to servlet-container");
                } else {
                    if (this.preferIp) {
                        final InetAddress address = this.serverProperties.getAddress();
                        final String hostAddress = this.getHostAddress(address);
                        resultManagamentUrl = this.append(this.append(this.createLocalUri(hostAddress, this.managementPort),
                                this.serverProperties.getContextPath()), this.managementServerProperties.getContextPath());

                    } else {
                        resultManagamentUrl = this.append(this.createLocalUri(this.determineHost(), this.managementPort),
                                this.managementServerProperties.getContextPath());
                    }
                }
            }
        }
        return resultManagamentUrl;
    }

    private String getHostAddress(final InetAddress address) {
        final String hostAddress;
        if (address == null) {
            hostAddress = this.determineHost();
        } else {
            hostAddress = address.getHostAddress();
        }
        return hostAddress;
    }

    public String getHealthUrl() {
        if (this.healthUrl != null) {
            return this.healthUrl;
        }
        final String managementUrl = this.getManagementUrl();
        return this.append(managementUrl, this.healthEndpointId);
    }

    public String getServiceUrl() {

        final String resultServiceUrl;

        if (this.serviceUrl != null) {
            resultServiceUrl = this.serviceUrl;
        } else {
            if (this.serverPort == null) {
                throw new IllegalStateException(
                        "serviceUrl must be set when deployed to servlet-container");
            } else {
                if (this.preferIp) {
                    final InetAddress address = this.serverProperties.getAddress();
                    final String hostAddress = this.getHostAddress(address);
                    resultServiceUrl = this.append(this.append(this.createLocalUri(hostAddress, this.serverPort),
                            this.serverProperties.getServletPath()), this.serverProperties.getContextPath());

                } else {
                    resultServiceUrl = this.append(this.append(this.createLocalUri(this.determineHost(), this.serverPort),
                            this.serverProperties.getServletPath()), this.serverProperties.getContextPath());
                }
            }
        }
        return resultServiceUrl;
    }

    private String createLocalUri(final String host, final int port) {
        final String scheme =
                this.serverProperties.getSsl() != null && this.serverProperties.getSsl().isEnabled() ? "https" : "http";
        return scheme + "://" + host + ":" + port;
    }

    private String append(final String uri, final String path) {

        final String resultBaseUri;

        final String baseUri = uri.replaceFirst("/+$", "");
        if (StringUtils.isEmpty(path)) {
            resultBaseUri = baseUri;
        } else {
            final String normPath = path.replaceFirst("^/+", "").replaceFirst("/+$", "");
            resultBaseUri = baseUri + "/" + normPath;
        }
        return resultBaseUri;
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

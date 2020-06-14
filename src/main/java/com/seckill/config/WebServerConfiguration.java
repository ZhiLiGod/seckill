package com.seckill.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * customize tomcat config
 */
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

  @Override
  public void customize(ConfigurableWebServerFactory factory) {
    ((TomcatServletWebServerFactory) factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {

      @Override
      public void customize(Connector connector) {

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

        // disconnect keepalive if no new requests within 30s
        protocol.setKeepAliveTimeout(30000);
        // when client sends more than 10000 requests, then disconnect
        protocol.setMaxKeepAliveRequests(10000);
      }

    });
  }

}

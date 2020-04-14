package acme.frontend;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

@Path("/health")
public final class HealthService {

  /**
   * Call the other services and see if they're up.
   */
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public HealthSummary getHealth() throws IOException {
    return new HealthSummary(
        Lists.newArrayList(
            performHealthCheck("data-manager", ServicePaths.DATA_MANAGER_URL),
            performHealthCheck("devservice", ServicePaths.DEBUG_URL)));
  }

  /**
   * For every path given, add the /ping to it and see if it's up. If a service doesn't respond in
   * short order then report them as down.
   */
  private HealthCheck performHealthCheck(final String name, final String baseUrl)
      throws IOException {
    final CloseableHttpClient client = HttpClientBuilder.create().build();
    final HttpUriRequest request = RequestBuilder.get(baseUrl + "ping").build();
    final CloseableHttpResponse response = client.execute(request);
    final StatusLine statusLine = response.getStatusLine();
    int status = statusLine != null ? statusLine.getStatusCode() : -1;
    return new HealthCheck(name,
        status >= 200 && status < 300 ? HealthStatus.HEALTHY : HealthStatus.DOWN);
  }

  private enum HealthStatus {
    HEALTHY,
    DOWN
  }

  @XmlRootElement(name = "health-summary")
  public static class HealthSummary {

    @XmlElement(name = "service")
    private List<HealthCheck> services;

    private HealthSummary() {
      services = Collections.emptyList();
    }

    private HealthSummary(final List<HealthCheck> services) {
      Preconditions.checkNotNull(services, "services");
      this.services = Collections.unmodifiableList(services);
    }
  }

  public static class HealthCheck {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "status")
    private String status;

    private HealthCheck() { }

    private HealthCheck(final String name, final HealthStatus status) {
      Preconditions.checkNotNull(name, "name");
      Preconditions.checkNotNull(name, "status");
      this.name = name;
      this.status = status.name();
    }
  }
}

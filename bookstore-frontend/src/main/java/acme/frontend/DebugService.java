package acme.frontend;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * A service only intended for use by developers.
 */
@Path("/debug")
public final class DebugService {

  /**
   * Call devservice to get debug information.
   */
  @GET
  public Response getDebugInfo() throws IOException {
    final String debugInfo = callForDebugInfo("PROD");
    return Response.ok().entity(debugInfo).build();
  }

  /**
   * Get the debug information for the right environment, which in our case, is production.
   */
  private String callForDebugInfo(final String env) throws IOException {
    final CloseableHttpClient client = HttpClientBuilder.create().build();
    final HttpUriRequest request = RequestBuilder.get(ServicePaths.DEBUG_URL + "info")
        .addParameter("env", env).build();
    final CloseableHttpResponse response = client.execute(request);
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      return EntityUtils.toString(entity);
    }
    final StatusLine statusLine = response.getStatusLine();
    int status = statusLine != null ? statusLine.getStatusCode() : -1;
    return String.format("Problem connecting to: %s (response %d)", env, status);
  }
}

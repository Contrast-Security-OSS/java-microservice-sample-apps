package acme.bookstore.devservice.resources;

import java.io.IOException;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.apache.commons.io.IOUtils;

@Path("/info")
public class InfoResource {

  @GET
  public String info(@QueryParam("env") final String env) {
    return getInfoFromEnv("http://" + env + ".acmedevinfo.local/info");
  }

  private String getInfoFromEnv(final String url) {
    String rc = null;
    try {
      rc = IOUtils.toString(new URL(url));
    } catch (IOException e) {
      // suppress
    }
    return rc;
  }
}

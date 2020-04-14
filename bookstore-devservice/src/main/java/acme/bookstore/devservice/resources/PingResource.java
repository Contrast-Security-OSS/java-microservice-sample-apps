package acme.bookstore.devservice.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/ping")
public class PingResource {

  @GET
  public String ping() {
    return "ping";
  }
}

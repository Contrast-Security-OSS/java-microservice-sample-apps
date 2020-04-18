package acme.profanityfilter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/ping")
public class PingService {

  @GET
  public Response ping() {
    return Response.ok().build();
  }

}

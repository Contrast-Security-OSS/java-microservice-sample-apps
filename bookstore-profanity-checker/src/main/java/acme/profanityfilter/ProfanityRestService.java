package acme.profanityfilter;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

@Path("/profanity")
public class ProfanityRestService {

  public static class Book {
    private String title;
    private int pages;

    public int getPages() {
      return pages;
    }

    public String getTitle() {
      return title;
    }
  }

  /** This is the DTM for returning whether profanity is present. */
  public static class ProfanityResponseJSON {
    public boolean profane;
    public String piece;
  }

  @POST
  @Path("/check/book")
  @Consumes("application/json")
  @Produces("application/json")
  public Response checkForProfanity(final Book book) {
    ProfanityResponseJSON response = new ProfanityResponseJSON();
    response.piece = "title";
    response.profane = false;
    response.profane = hasProfanity(book.getTitle());
    return Response.ok().entity(response).build();
  }

  /**
   * Check to see if the given string has known profanity.
   */
  private boolean hasProfanity(final String str) {
    for(String badWord : badWords) {
      if(StringUtils.containsIgnoreCase(str, badWord)) {
        return true;
      }
    }
    return false;
  }

  @GET
  @Path("/check/title")
  @Produces("application/json")
  public Response checkForProfanity(@QueryParam("title") final String title) {
    final boolean hasProfanity = hasProfanity(title);
    final String json = "{\"title\":\"" + title + "\", \"profane\": \"" + hasProfanity + "\"}";
    return Response.ok().entity(json).build();
  }

  private final List<String> badWords = Arrays.asList(
      "darn",
      "shoot",
      "shucks"
  );
}

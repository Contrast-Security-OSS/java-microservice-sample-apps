package acme.datamanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class BookstoreDataService {

  private final Set<Book> books;
  private final Gson gson;

  BookstoreDataService() {
    books = Collections.synchronizedSet(new HashSet<>());
    books.add(new Book("Dune",491));
    books.add(new Book("The Stars My Destination",232));

    gson = new GsonBuilder().create();
  }

  @RequestMapping("/")
  public String index() {
    return "this is the bookstore home";
  }

  @RequestMapping("/ping")
  public void ping() { }

  @RequestMapping(path = "/list", method = RequestMethod.GET)
  public Collection<Book> list() {
    return books;
  }

  @RequestMapping(path = "/add", method = RequestMethod.POST)
  public void addBook(@RequestBody final Book book) throws IOException {
    if(!hasProfanity(book.getTitle())) {
      books.add(book);
    }
  }

  /** Invokes the bookstore-profanity-checker service to check if the title is safe. */
  private boolean hasProfanity(final String title) throws IOException {
    final CloseableHttpClient client = HttpClientBuilder.create().build();
    final HttpUriRequest request = RequestBuilder.get("http://bookstore-profanity-checker:8003/api/profanity/check/title")
        .addParameter("title", title).build();
    final CloseableHttpResponse response = client.execute(request);
    final int statusCode = response.getStatusLine().getStatusCode();
    if(statusCode != 200) {
      return false;
    }
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      final String json = EntityUtils.toString(entity);
      final ProfanityResponseJSON profanityResponse = gson.fromJson(json, ProfanityResponseJSON.class);
      return profanityResponse.profane;
    }
    return false;
  }

  /** This is the DTM for returning whether profanity is present. */
  public static class ProfanityResponseJSON {
    public boolean profane;
    public String piece;
  }

  /**
   * This is how our legacy systems update a book when the page count needs to be updated. This one
   * doesn't need a profanity check because these book titles are checked upstream.
   */
  @RequestMapping(path = "/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void updateBook(final HttpServletRequest request) throws Exception {
    final ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
    final Book updatedBook = (Book) ois.readObject();
    searchAndReplace(updatedBook);
  }

  /**
   * Loop through existing books and update page count if needed if needed.
   */
  private void searchAndReplace(final Book updatedBook) {
    books.removeIf(existingBook -> existingBook.getTitle().equals(updatedBook.getTitle()));
    books.add(updatedBook);
  }
}

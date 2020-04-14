package acme.frontend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Path("/")
public final class BookService {

  private final Gson gson = new GsonBuilder().create();

  /**
   * Take a request to add a new book, send it to the backend microservice asynchronously. This
   * endpoint expects the book in XML form. Validate it's a valid book before forwarding on.
   */
  @Path("/add")
  @POST
  public Response addBook(final InputStream body) throws JAXBException, IOException {
    final JAXBContext jaxb = JAXBContext.newInstance(Book.class);
    final Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    final Book book = (Book) unmarshaller.unmarshal(body);
    System.out.println("Forwarding new book " + book.title + " to data-manager");
    sendToDataManager(book);
    return Response.ok().build();
  }

  /**
   * Send the book to the microservice that actually stores it.
   */
  private void sendToDataManager(final Book book) throws IOException {
    String bookJson = gson.toJson(book);
    final CloseableHttpClient client = HttpClientBuilder.create().build();
    final HttpUriRequest request = RequestBuilder.post(ServicePaths.DATA_MANAGER_URL + "add")
        .setHeader("Content-Type", "application/json")
        .setEntity(new StringEntity(bookJson)).build();
    final CloseableHttpResponse response = client.execute(request);
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      EntityUtils.consumeQuietly(entity);
    }
  }

  /**
   * Call the bookstore-data-manager service to get the books.
   */
  @Path("/list")
  @GET
  public BookList list() throws IOException {
    return getBooksFromDataManager();
  }

  private BookList getBooksFromDataManager() throws IOException {
    final CloseableHttpClient client = HttpClientBuilder.create().build();
    final HttpUriRequest request = RequestBuilder.get(ServicePaths.DATA_MANAGER_URL + "list")
        .build();
    final CloseableHttpResponse response = client.execute(request);
    final HttpEntity entity = response.getEntity();
    final BookList booklist = new BookList();
    if (entity != null) {
      final String responseBody = EntityUtils.toString(entity);
      final Type listType = new TypeToken<List<Book>>() {
      }.getType();
      booklist.titles = gson.fromJson(responseBody, listType);
      System.out
          .println("Fetched " + booklist.titles.size() + " from bookstore-data-manger service");
    } else {
      System.err.println("Couldn't get book info from bookstore-data-manger service");
    }
    return booklist;
  }

  @XmlRootElement(name = "books")
  private static class BookList {

    @XmlElement(name = "book")
    private List<Book> titles;
  }

  @XmlRootElement(name = "book")
  private static class Book {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "pages")
    private int pages;

    @Override
    public String toString() {
      return "Book{" +
          "title='" + title + '\'' +
          ", pages=" + pages +
          '}';
    }
  }
}

package acme.datamanager;

import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class BookstoreDataService {

  private final Set<Book> books;

  BookstoreDataService() {
    books = Collections.synchronizedSet(new HashSet<>());
    books.add(new Book("Dune",491));
    books.add(new Book("The Stars My Destination",232));
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
  public void addBook(@RequestBody final Book book) {
    System.out.println("Adding book: " + book.getTitle());
    books.add(book);
  }

  /**
   * This is how our legacy systems update a book when the page count needs to be updated.
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

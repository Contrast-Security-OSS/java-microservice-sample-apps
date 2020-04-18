package acme.profanityfilter;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * Offer health and profanity services.
 */
public class App extends Application
{
    private Set<Object> singletons = new HashSet<>();

    public App() {
        singletons.add(new PingService());
        singletons.add(new ProfanityRestService());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}

package acme.bookstore.devservice;

import acme.bookstore.devservice.core.Person;
import acme.bookstore.devservice.resources.InfoResource;
import acme.bookstore.devservice.resources.PingResource;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import java.util.Map;

public final class Application extends io.dropwizard.Application<ApplicationConfiguration> {

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    private final HibernateBundle<ApplicationConfiguration> hibernateBundle =
        new HibernateBundle<ApplicationConfiguration>(Person.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(ApplicationConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };

    @Override
    public String getName() {
        return "bookstore-devservice";
    }

    @Override
    public void initialize(Bootstrap<ApplicationConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<ApplicationConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ApplicationConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<ApplicationConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(
                ApplicationConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(ApplicationConfiguration configuration, Environment environment) {
        environment.jersey().register(new PingResource());
        environment.jersey().register(new InfoResource());
    }
}

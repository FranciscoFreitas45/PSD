package Catalog;

import Catalog.Resources.ImporterResource;
import Catalog.Resources.ManufacturerResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class CompanyApplication extends Application<CompanyConfiguration> {
    public static void main(String[] args) throws Exception {
        new CompanyApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<CompanyConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(CompanyConfiguration configuration,
                    Environment environment) {
        final ManufacturerResource resource = new ManufacturerResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        final ImporterResource imp_resource = new ImporterResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(imp_resource);
        environment.jersey().register(resource);

    }

}
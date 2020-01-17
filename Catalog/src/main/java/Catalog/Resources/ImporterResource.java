package Catalog.Resources;


import Catalog.Representation.Importader;
import Catalog.Representation.Offer;
import com.codahale.metrics.annotation.Timed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Path("/importer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ImporterResource {
    private final String template;
    private volatile String defaultName;
    private final Map<String, Importader> importers;
    private final AtomicLong counter;

    public ImporterResource(String template, String defaultName){
        this.template = template;
        this.defaultName = defaultName;
        this.importers = new ConcurrentHashMap<>();
        this.counter =  new AtomicLong();

        this.importers.put("Golias",new Importader("Golias"));
        this.importers.put("MEGAMIND",new Importader("MEGAMIND"));

    }
    @GET
    @Timed
    public Response get(){
        return Response.ok(this.importers.values().stream().collect(Collectors.toList())).build();
    }

    @GET
    @Timed
    @Path("/{nameFab}")
    public Response get(@PathParam("nameFab") String name) {
        Importader i = this.importers.get(name);
        if (i == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(i).build();
    }

    @POST
    public Response postImporter(@NotNull @Valid Importader imp){
        String name = imp.getName();
        Importader i = this.importers.get(name);
        if(i==null)
        this.importers.put(name,imp);
        else{
            return Response.ok("Ja existe").build();
        }
        return Response.ok(imp).build();
    }

    @POST
    @Path("/offer/{name}")
    public Response postOrder(@PathParam("name") String name,@NotNull @Valid Offer offer){
        Importader i = this.importers.get(name);
        if (i == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else{
            long id = counter.incrementAndGet();
            offer.setId(id);
            i.addOffer(offer);
        }
        return Response.ok(offer).build();
    }




}

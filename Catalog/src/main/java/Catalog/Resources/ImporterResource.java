package Catalog.Resources;


import Catalog.Representation.Importer;
import Catalog.Representation.Importer;
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
    private final Map<String, Importer> importers;

    public ImporterResource(String template, String defaultName){
        this.template = template;
        this.defaultName = defaultName;
        this.importers = new ConcurrentHashMap<>();

        this.importers.put("Golias",new Importer("Golias"));
        this.importers.put("MEGAMIND",new Importer("MEGAMIND"));

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
        Importer i = this.importers.get(name);
        if (i == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(i).build();
    }

    @GET
    @Timed
    @Path("/{nameFab}/historic")
    public Response getHistoric(@PathParam("nameFab") String name){
        Importer i = this.importers.get(name);
        if (i == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(i.getHistoric()).build();
    }


    @POST
    public Response postImporter(@NotNull @Valid Importer imp){
        String name = imp.getName();
        Importer i = this.importers.get(name);
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
        Importer i = this.importers.get(name);
        if (i == null){
            Importer new_i = new Importer(name);
            new_i.addOffer(offer);
            this.importers.put(name,new_i);
        }
        else{
            i.addOffer(offer);
        }
        return Response.ok(offer).build();
    }

    @PUT
    @Path("/historic/{nameFab}/{idOffer}/{idOrder}/{state}")
    public Response postOrderHistoric(@PathParam("nameFab") String name,@PathParam("idOffer") String idOffer,
                                      @PathParam("idOrder") String idOrder,@PathParam("state") String state){
        Importer i = this.importers.get(name);
        if (i == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else{
            Long id_order= Long.parseLong(idOrder);
            Long id_offer= Long.parseLong(idOffer);
            i.addOfferHistoric(id_offer,id_order,state);
        }
        return Response.ok().build();
    }




}

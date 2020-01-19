package Catalog.Resources;


import Catalog.Representation.Manufacturer;
import Catalog.Representation.Manufacturer;
import Catalog.Representation.Order;
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

@Path("/manufacturer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ManufacturerResource {
    private final String template;
    private volatile String defaultName;
    private final Map<String, Manufacturer> manufacters;

    public ManufacturerResource(String template, String defaultName){
        this.template = template;
        this.defaultName = defaultName;
        this.manufacters = new ConcurrentHashMap<>();

        this.manufacters.put("McDonalds",new Manufacturer("McDonalds"));
        this.manufacters.put("IPO",new Manufacturer("IPO"));
    }
    @GET
    @Timed
    public Response get(){
        return Response.ok(this.manufacters.values().stream().collect(Collectors.toList())).build();
    }

    @GET
    @Timed
    @Path("/{nameFab}")
    public Response get(@PathParam("nameFab") String name) {
        Manufacturer f = this.manufacters.get(name);
        if (f == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(f).build();
    }
    @GET
    @Timed
    @Path("/{nameFab}/historic")
    public Response getHistoric(@PathParam("nameFab") String name){
        Manufacturer f = this.manufacters.get(name);
            if (f == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            return Response.ok(f.getHistoric()).build();
    }



    @GET
    @Timed
    @Path("/{nameFab}/orders")
    public Response getOrders(@PathParam("nameFab") String name){
        Manufacturer f = this.manufacters.get(name);
        if (f == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(f.getOrders()).build();
    }

    @POST
    public Response postFabricante(@NotNull @Valid Manufacturer fab){
        String name = fab.getName();
        Manufacturer p = this.manufacters.get(name);
        if(p == null){
            this.manufacters.put(name,fab);
        }
       else
           Response.ok("Ja existe um produtor com esse nome").build();
        return Response.ok(fab).build();
    }

    @POST
    @Path("/order/{nameFab}")
    public Response postOrder(@PathParam("nameFab") String name,@NotNull @Valid Order order){
        Manufacturer f = this.manufacters.get(name);
        if (f == null){
            Manufacturer new_f = new Manufacturer(name);
            new_f.addOrder(order);
            this.manufacters.put(name,new_f);
        }
        else{
            f.addOrder(order);
        }
        return Response.ok().build();
    }


    @PUT
    @Path("/historic/{nameFab}/{idOrder}")
    public Response postOrderHistoric(@PathParam("nameFab") String name,@PathParam("idOrder") String idOrder){
        Manufacturer f = this.manufacters.get(name);
        if (f == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else{
            Long id= Long.parseLong(idOrder);
            f.addOrderHistoric(id);
        }
        return Response.ok().build();
    }






}

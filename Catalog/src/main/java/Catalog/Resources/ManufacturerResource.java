package Catalog.Resources;


import Catalog.Representation.Manufacter;
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

@Path("/manufacter")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ManufacterResource {
    private final String template;
    private volatile String defaultName;
    private final Map<String, Manufacter> manufacters;
    private final AtomicLong counterOrder;
    private final AtomicLong counterHistoric;

    public ManufacterResource(String template, String defaultName){
        this.template = template;
        this.defaultName = defaultName;
        this.manufacters = new ConcurrentHashMap<>();
        this.counterOrder = new AtomicLong();
        this.counterHistoric = new AtomicLong();

        this.manufacters.put("MacDonalds",new Manufacter("McDonalds"));
        this.manufacters.put("IPO",new Manufacter("IPO"));
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
        Manufacter f = this.manufacters.get(name);
        if (f == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(f).build();
    }
    @GET
    @Timed
    @Path("/{nameFab}/historic")
    public Response getHistoric(@PathParam("nameFab") String name){
        Manufacter f = this.manufacters.get(name);
            if (f == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            return Response.ok(f.getHistorico()).build();
    }



    @GET
    @Timed
    @Path("/{nameFab}/order")
    public Response getOrders(@PathParam("nameFab") String name){
        Manufacter f = this.manufacters.get(name);
        if (f == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return Response.ok(f.getOrdens()).build();
    }

    @POST
    public Response postFabricante(@NotNull @Valid Manufacter fab){
        String name = fab.getNome();
        Manufacter p = this.manufacters.get(name);
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
        Manufacter f = this.manufacters.get(name);
        if (f == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else{
            long id = counterOrder.incrementAndGet();
            order.setId(id);
            f.addOrder(order);
        }
        return Response.ok().build();
    }


    @POST
    @Path("/historic/{nameFab}")
    public Response postOrderHistoric(@PathParam("nameFab") String name,@NotNull @Valid Order order){
        Manufacter f = this.manufacters.get(name);
        if (f == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else{
            long id = counterHistoric.incrementAndGet();
            order.setId(id);
            f.addOrderHistoric(order);
        }
        return Response.ok().build();
    }




    @PUT
    @Path("/{nameFab}/{idOrder}")
    public Response putAcabou(@PathParam("nameFab") String name,@PathParam("idOrder") String idOrder){
        Order o = null;
        Manufacter f = this.manufacters.get(name);
        if (f == null){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        else {
            o = f.getOrder(Long.valueOf(idOrder));
        }

            if(o==null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            else
                 o.setAcabou(1);// acabou a negociacao

        return Response.ok().build();
    }

}

package com.revolut.interview;

import java.util.ConcurrentModificationException;
import java.util.function.Supplier;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Provides a JSON REST Webservice for the {@link AccountService}
 */
@Path("accounts")
public class AccountServiceController {
    
    private AccountService accountService = new AccountServiceImpl();
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(NewAccountRequest newAccountRequest) {
        return createResponse(() -> this.accountService.createAccount(newAccountRequest));
    }
    
    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(FundTransferRequest transferRequest) {
        return createResponse(() -> {
                this.accountService.transfer(transferRequest);
                return null;
            });
    }

    @GET
    @Path("/{accountNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("accountNo") Integer accountNo) {
        return createResponse(() -> this.accountService.getAccount(accountNo));
    }
    
    private Response createResponse(Supplier<?> dataGetter) {
        try {
            return Response.status(Status.OK).entity(dataGetter.get()).build();         
        } catch (IllegalArgumentException | ConcurrentModificationException | IllegalStateException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}

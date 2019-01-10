package com.dengjunwu.api;

import com.dengjunwu.client.annotation.ThriftClient;
import com.dengjunwu.exception.ThriftServiceException;
import com.dengjunwu.tservice.TDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/thrift")
@Slf4j
@Component
public class ThriftDemoApi {

    @ThriftClient(application = "test")
    TDemoService tDemoService;

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String apiStart(){
        try {
            System.out.println(tDemoService.count());
        } catch (ThriftServiceException e) {
            e.printStackTrace();
        }
        return "thrift 测试用例";
    }
}

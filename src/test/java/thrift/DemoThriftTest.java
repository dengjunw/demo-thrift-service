package thrift;

import com.dengjunwu.ThriftBootstrap;
import com.dengjunwu.exception.ThriftServiceException;
import com.dengjunwu.tservice.TDemoService;
import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static com.google.common.net.HostAndPort.fromParts;

@SpringBootTest(
        classes = {ThriftBootstrap.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "thrift.server.port=9999"
)
@RunWith(SpringRunner.class)
@Slf4j
public class DemoThriftTest {

    private FramedClientConnector connector;
    private ThriftClientManager clientManager = new ThriftClientManager();

    private TDemoService tDemoService;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        connector = new FramedClientConnector(fromParts("127.0.0.1", 9999));
        tDemoService = clientManager.createClient(connector, TDemoService.class).get();
    }

    @After
    public void tearDown() {
        clientManager.close();
    }


    @Test
    public void testThriftMethod() throws ThriftServiceException {
        log.info("count : {}", tDemoService.count());
    }
}

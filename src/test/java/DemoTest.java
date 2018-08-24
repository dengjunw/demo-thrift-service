import com.dengjunwu.ThriftBootstrap;
import com.dengjunwu.module.Impl.TDemoTestImpl;
import com.dengjunwu.module.TDemoTest;
import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TApplicationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static com.google.common.net.HostAndPort.fromParts;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = {ThriftBootstrap.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "thrift.server.port=7777")
@ActiveProfiles(profiles = {"test"})
public class DemoTest {
    private FramedClientConnector connector;
    private ThriftClientManager clientManager = new ThriftClientManager();
    private TDemoTest tDemoTest;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        connector = new FramedClientConnector(fromParts("localhost", 7777));
        tDemoTest = clientManager.createClient(connector, TDemoTest.class).get();
        //connector = new FramedClientConnector(fromParts("47.94.47.110", 8891));//线上
    }

    @After
    public void tearDown() {
        clientManager.close();
    }


    @Test
    public void testDemo() throws ExecutionException, InterruptedException {
        try {
            log.info("result:{}", tDemoTest.isValidate());
        } catch (TApplicationException e) {
            e.printStackTrace();
        }
    }
}

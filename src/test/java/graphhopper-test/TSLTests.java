import com.graphhopper.config.LMProfile;
import com.graphhopper.json.Statement;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.TestProfiles;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.nio.file.Paths;

/**
 * Test cases generated via TSL file.
 */
public class TSLTests {
    private GraphHopper hopper;
    private String osmFile;

    /**
     * Initializes the graph hopper instance before each test execution.
     */
    @Before
    public void setUp() {
        String basePath = String.valueOf(Paths.get(System.getProperty("user.dir"), "src", "test", "java", "graphhopper-test"));

        hopper = new GraphHopper()
                .setGraphHopperLocation("test")
                .setOSMFile(basePath + "/bsu.osm")
                .setEncodedValuesString("car_access, car_average_speed")
                .setProfiles(TestProfiles.accessAndSpeed("profile", "car"))
                .setStoreOnFlush(true);
        hopper.getCHPreparationHandler()
                .setCHProfiles(new CHProfile("profile"));
        hopper.importOrLoad();
    }
}


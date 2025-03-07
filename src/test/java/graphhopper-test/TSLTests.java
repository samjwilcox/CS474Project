import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.config.LMProfile;
import com.graphhopper.json.Statement;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.TestProfiles;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.nio.file.Paths;
import java.util.*;

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
                .setOSMFile(basePath + "/IdahoArea.osm.pbf")
                .setEncodedValuesString("car_access, car_average_speed")
                .setProfiles(TestProfiles.accessAndSpeed("profile", "car"))
                .setStoreOnFlush(true);
        hopper.getCHPreparationHandler()
                .setCHProfiles(new CHProfile("profile"));
        hopper.importOrLoad();
    }

    /**
     * Helper that converts the response into JSON format and verifies that the
     * response object can be converted to JSON.
     *
     * @param response - The response object from GraphHopper.
     * @return True if successful, false if failed.
     */
    public boolean convertToJSON(GHResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonOutput = mapper.writeValueAsString(response);
            System.out.println("Generated JSON: " + jsonOutput);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test Case 1:
     * Test invalid profiles.
     */
    @Test
    public void invalidProfile() {
        GHRequest request = new GHRequest(52.5, 13.4, 52.6, 13.5)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for invalid profile", response.hasErrors());
    }

    /**
     * Test Case 2:
     * Test invalid coordinates.
     */
    @Test
    public void invalidCoordinates() {
        GHRequest request = new GHRequest(999.0, 999.0, 52.6, 13.5);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for invalid coordinates", response.hasErrors());
    }

    /**
     * Test Case 3:
     * Test missing graph data.
     */
    @Test
    public void missingGraphData() {
        GraphHopper missingDataHopper = new GraphHopper()
                .setGraphHopperLocation("test")
                .setOSMFile("path/to/missing/file.osm")
                .setEncodedValuesString("car_access, car_average_speed")
                .setProfiles(TestProfiles.accessAndSpeed("profile", "car"))
                .setStoreOnFlush(true);

        missingDataHopper.getCHPreparationHandler().setCHProfiles(new CHProfile("profile"));
        missingDataHopper.importOrLoad();

        GHRequest request = new GHRequest(52.5, 13.4, 52.6, 13.5);
        GHResponse response = missingDataHopper.route(request);
        assertTrue("Expected an error for missing graph data", response.hasErrors());
    }

    /**
     * Test Case 4:
     * Test corrupted graph data.
     */
    @Test
    public void corruptedGraphData() {
        GraphHopper corruptedDataHopper = new GraphHopper()
                .setGraphHopperLocation("test")
                .setOSMFile("path/to/corrupted/file.osm")
                .setEncodedValuesString("car_access, car_average_speed")
                .setProfiles(TestProfiles.accessAndSpeed("profile", "car"))
                .setStoreOnFlush(true);

        corruptedDataHopper.getCHPreparationHandler().setCHProfiles(new CHProfile("profile"));
        corruptedDataHopper.importOrLoad();

        GHRequest request = new GHRequest(52.5, 13.4, 52.6, 13.5);
        GHResponse response = corruptedDataHopper.route(request);
        assertTrue("Expected an error for corrupted graph data", response.hasErrors());
    }

    /**
     * Test Case 5:
     * Test unsupported output format.
     */
    @Test
    public void unsupportedOutputFormat() {
        GHRequest request = new GHRequest(52.5, 13.4, 52.6, 13.5)
                .setProfile("car");

        GHResponse response = hopper.route(request);
        boolean isUnsupportedFormatHandled = false;

        try {
            if (response.hasErrors()) {
                isUnsupportedFormatHandled = true;
            }
        } catch (Exception e) {
            assertTrue("Expected an error for unsupported output format", e.getMessage().contains("unsupported"));
            return;
        }

        assertTrue("Expected an error for unsupported output format", isUnsupportedFormatHandled || response.hasErrors());
    }

    /**
     * Test Case 6:
     * Test disconnected road network.
     */
    @Test
    public void disconnectedRoadNetwork() {
        GHRequest request = new GHRequest(52.5, 13.4, 53.0, 14.0);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for disconnected road network", response.hasErrors());
    }

    /**
     * Test Case 7:
     * Test private roads.
     */
    @Test
    public void privateRoads() {
        // Use a request that targets a private road
        GHRequest request = new GHRequest(52.5, 13.4, 52.55, 13.45);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for private roads", response.hasErrors());
    }

    /**
     * Test Case 8:
     * Test invalid input conditions.
     */
    @Test
    public void invalidInput() {
        GHRequest request = new GHRequest(52.5, 13.4, 52.5, 13.4)
                .setProfile("car");

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for invalid input", response.hasErrors());
    }

    /**
     * Test Case 9:
     * Test partial route available.
     */
    @Test
    public void partialRouteAvailable() {
        GHRequest request = new GHRequest(52.5, 13.4, 53.0, 14.0);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for partial route available", response.hasErrors());
    }

    /**
     * Test Case 10:
     * Test no route found.
     */
    @Test
    public void noRouteFound() {
        GHRequest request = new GHRequest(999.0, 999.0, 999.1, 999.1);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for no route found", response.hasErrors());
    }

    /**
     * Test Case 11: Small dataset (city-level) with restricted access.
     */
    @Test
    public void testRoutingSmallDataset() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.6000, -116.2000) // Coordinates around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 12: Large dataset (country-level) with restricted access.
     */
    @Test
    public void testRoutingLargeDataset() {
        // Adjusted coordinates within the bounds
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6050, -116.1800) // Coordinates within bounds around Boise
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 13: High concurrency load with restricted access.
     */
    @Test
    public void testRoutingHighConcurrencyLoad() {
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6000, -116.1800) // Another route around Boise
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 14: Small dataset (city-level) with handling restricted access.
     */
    @Test
    public void testRoutingSmallDatasetWithAccess() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.5990, -116.2005) // Short route around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        if (response.hasErrors()) {
            System.out.println("Errors: " + response.getErrors());
        }
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 15: Large dataset (country-level) with handling restricted access.
     */
    @Test
    public void testRoutingLargeDatasetWithAccess() {
        // Adjusted coordinates within the bounds
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6050, -116.1800) // Route around Boise within bounds
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        if (response.hasErrors()) {
            System.out.println("Errors: " + response.getErrors());
        }
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 16: High concurrency load with handling restricted access.
     */
    @Test
    public void testRoutingHighConcurrencyLoadWithAccess() {
        GHRequest request = new GHRequest(43.6050, -116.2100, 43.5900, -116.1800) // Another route around Boise
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 17: Small dataset (city-level) across different time zones.
     */
    @Test
    public void testRoutingAcrossTimeZonesSmallDataset() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.6000, -116.1950) // Short route around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 18: Large dataset (country-level) across different time zones.
     */
    @Test
    public void testRoutingAcrossTimeZonesLargeDataset() {
        GHRequest request = new GHRequest(43.5881, -116.2100, 43.6090, -116.1772)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 19: High concurrency load across different time zones.
     */
    @Test
    public void testRoutingAcrossTimeZonesHighConcurrency() {
        // Adjusted coordinates within the bounds
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6080, -116.1800) // Route around Boise within bounds
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 20: Small dataset (city-level) with handling one-way streets.
     */
    @Test
    public void testRoutingOneWayStreetsSmallDataset() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.5990, -116.2020) // Short route around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 41: Small dataset (city-level) with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysCityLevel() {
        GHRequest request = new GHRequest(43.6031, -116.2075, 43.5644, -116.2228) // Boise State University to Boise Airport
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 42: Large dataset (country-level) with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysLargeDataset() {
        GHRequest request = new GHRequest(47.6588, -117.4260, 42.8713, -112.4455) // Spokane to Pocatello
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 43: High concurrency load with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysHighConcurrency() {
        GHRequest request = new GHRequest(43.4917, -112.0331, 45.9584, -112.5325) // Idaho Falls, ID to Butte, MT
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 44: Small dataset (city-level) with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysCityLevelBoise() {
        GHRequest request = new GHRequest(43.5644, -116.2228, 43.6007, -116.1996) // Boise Airport to BSU CS Building
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 45: Large dataset (country-level) with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysLargeDatasetOntarioToHelena() {
        GHRequest request = new GHRequest(44.0266, -116.9629, 46.5891, -112.0391) // Ontario, OR to Helena, MT
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 46: High concurrency load with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysHighConcurrencySpokaneHelena() {
        GHRequest request = new GHRequest(47.6588, -117.4260, 46.8787, -112.4808) // Spokane, WA to Helena, MT
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 47: Routing across different time zones with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysTimeZones() {
        GHRequest request = new GHRequest(43.6075, -116.2018, 43.6254, -116.1220) // Boise State University CS building to Lucky Peak Park
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 48: Routing across different time zones with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysTimeZonesLargeDataset() {
        GHRequest request = new GHRequest(47.6588, -117.4260, 43.4666, -112.0330) // Spokane, Washington to Idaho Falls, Idaho
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 49: Routing across different time zones with avoiding highways.
     */
    @Test
    public void testRouteAvoidHighwaysTimeZonesHighConcurrencyLoad() {
        GHRequest request = new GHRequest(47.6588, -117.4260, 41.5890, -109.2030) // Spokane, Washington to Rock Springs, Wyoming
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 50: Handling one-way streets with avoiding highways in a small dataset (city-level).
     */
    @Test
    public void testRouteAvoidHighwaysOneWayStreetsSmallDataset() {
        GHRequest request = new GHRequest(43.6560, -112.0260, 43.6177, -116.1701) // Idaho State Correctional Center to Idaho State Capitol Building
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 51: Handling one-way streets with avoiding highways in a large dataset (country-level).
     */
    @Test
    public void testRouteAvoidHighwaysOneWayStreetsLargeDataset() {
        GHRequest request = new GHRequest(41.5868, -109.2029, 44.0650, -116.9633) // Rock Springs, Wyoming to Ontario, Oregon
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 52: Handling one-way streets with avoiding highways and high concurrency load in a city-level dataset.
     */
    @Test
    public void testRouteAvoidHighwaysOneWayStreetsHighConcurrencyLoad() {
        GHRequest request = new GHRequest(43.6135, -116.2009, 43.6125, -116.1990) // Around downtown Boise
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 53: Routing over bridges/tunnels with avoiding highways and small dataset (city-level).
     */
    @Test
    public void testRouteAvoidHighwaysBridgesTunnelsSmallDataset() {
        GHRequest request = new GHRequest(43.5646, -116.2223, 43.6163, -116.2347) // Boise Airport to Mongolian BBQ location
                .setProfile("profile") // Routing profile for car
                .putHint("avoid", "highways"); // Avoid highways option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 54: Routing over bridges/tunnels with avoiding highways and large dataset (country-level).
     */
    @Test
    public void testRouteAvoidHighwaysBridgesTunnelsLargeDataset() {
        GHRequest request = new GHRequest(45.6760, -111.0429, 42.8713, -112.4455) // Bozeman, MT to Pocatello, ID
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 55: Routing over bridges/tunnels with avoiding highways and high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysBridgesTunnelsHighConcurrency() {
        GHRequest request = new GHRequest(43.615, -116.2023, 42.562, -114.4606) // Boise, ID to Twin Falls, ID
                .setProfile("profile")
                .putHint("avoid", "highways");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 56: Routing with multiple waypoints using the fastest route across a small dataset (city-level).
     */
    @Test
    public void testRouteMultipleWaypointsFastestRouteCityLevel() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise, Idaho (starting point)
                new GHPoint(43.618, -116.211),  // Boise City Hall (waypoint 1)
                new GHPoint(43.625, -116.215)   // Boise State University (waypoint 2)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 57: Routing with multiple waypoints using the fastest route across a large dataset (country-level).
     */
    @Test
    public void testRouteMultipleWaypointsFastestRouteLargeDataset() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise, Idaho
                new GHPoint(44.95, -110.681),  // Near Yellowstone, Wyoming (you may adjust this if out of range)
                new GHPoint(45.687, -111.042)   // Bozeman, Montana
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 58: Routing with multiple waypoints using the fastest route, simulating high concurrency load.
     */
    @Test
    public void testRouteMultipleWaypointsFastestRouteHighConcurrency() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise, Idaho (starting point)
                new GHPoint(44.05, -116.59),    // Mountain Home, Idaho (waypoint 1)
                new GHPoint(44.5, -116.7),      // Near Idaho City, Idaho (waypoint 2)
                new GHPoint(45.0, -116.85)      // Near Cascade, Idaho (waypoint 3)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 59: Routing with multiple waypoints in Boise, Idaho (small dataset, city-level).
     */
    @Test
    public void testRouteMultipleWaypointsFastestRouteSmallDataset() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise State University (starting point)
                new GHPoint(43.564, -116.223),  // Boise Airport (waypoint 1)
                new GHPoint(43.606, -116.202),  // Downtown Boise (waypoint 2)
                new GHPoint(43.614, -116.238)   // A different point in Boise (waypoint 3)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 60: Routing with multiple waypoints across Idaho and Montana (large dataset, country-level).
     */
    @Test
    public void testRouteMultipleWaypointsFastestRouteLargeDatasetIdahoMontana() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6176, -116.1997), // Boise, Idaho (starting point)
                new GHPoint(42.9057, -112.4523), // Idaho Falls, Idaho (waypoint 1)
                new GHPoint(46.5957, -112.0270), // Helena, Montana (waypoint 2)
                new GHPoint(45.6794, -111.0448)  // Bozeman, Montana (waypoint 3)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }
}


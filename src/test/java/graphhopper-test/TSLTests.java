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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Test cases generated via TSL file.
 */
public class TSLTests {
    private GraphHopper hopper;
    private String osmFile;
    private static final int THREAD_COUNT = 50;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

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
                new GHPoint(44.95, -110.681),  // Near Yellowstone, Wyoming
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

    /**
     * Test Case 81: Routing with multiple waypoints, alternative routes enabled,
     * handling one-way streets, and testing performance on a large dataset (Idaho & Montana).
     */
    @Test
    public void testRouteMultipleWaypointsAlternativeRoutesLargeDataset() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise, Idaho (start)
                new GHPoint(46.591, -112.015),  // Helena, Montana (waypoint 1)
                new GHPoint(41.584, -109.220),  // Rock Springs, Wyoming (waypoint 2)
                new GHPoint(45.0, -116.85)      // Near Cascade, Idaho (waypoint 3)
        );

        for (int i = 0; i < points.size() - 1; i++) {
            GHRequest request = new GHRequest(points.get(i), points.get(i + 1))
                    .setProfile("profile")
                    .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)
                    .putHint("alternative_route.max_paths", 3)
                    .putHint("alternative_route.max_weight_factor", 5.0)
                    .putHint("alternative_route.max_share_factor", 0.6);

            GHResponse response = hopper.route(request);

            assertFalse("Expected successful response for valid input", response.hasErrors());
            assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

            // Validate that alternative routes are provided
            assertTrue("Expected at least one alternative route", response.getAll().size() > 1);
        }
    }

    /**
     * Test Case 82: High concurrency routing with multiple waypoints, alternative routes enabled,
     * handling one-way streets (separated into multiple segments).
     */
    @Test
    public void testRouteMultipleWaypointsAlternativeRoutesHighConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Boise, Idaho (start)
                new GHPoint(46.591, -112.015),  // Helena, Montana (waypoint 1)
                new GHPoint(41.584, -109.220),  // Rock Springs, Wyoming (waypoint 2)
                new GHPoint(45.0, -116.85)      // Near Cascade, Idaho (waypoint 3)
        );

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(points.get(startIdx), points.get(endIdx))
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)
                        .putHint("alternative_route.max_paths", 3)
                        .putHint("alternative_route.max_weight_factor", 2.0)
                        .putHint("alternative_route.max_share_factor", 0.4);

                GHResponse response = hopper.route(request);

                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response.getAll().size() > 1;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one alternative route in concurrent execution", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 83: Routing with multiple waypoints, alternative routes enabled,
     * and testing performance on a small dataset (city-level).
     */
    @Test
    public void testRouteMultipleWaypointsAlternativeRoutesSmallDataset() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6009, -116.2021), // BSU Student Union Building (starting point)
                new GHPoint(43.6050, -116.1933), // Near QDOBA Mexican Eats (Broadway Ave), Boise, Idaho (waypoint 1)
                new GHPoint(43.6231, -116.2532), // Near N Curtis Rd and Fairview, Boise, Idaho (waypoint 2)
                new GHPoint(43.5900, -116.2486)  // Phillippi Rd and Overland Rd, Boise, Idaho (end point)
        );

        List<GHPoint> segmentPoints;
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));

            List<GHPoint> finalSegmentPoints = segmentPoints;
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)
                        .putHint("alternative_route.max_paths", 3)
                        .putHint("alternative_route.max_weight_factor", 5.0)
                        .putHint("alternative_route.max_share_factor", 0.6);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response.getAll().size() > 1;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one alternative route in concurrent execution", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 84: Routing with multiple waypoints, alternative routes enabled,
     * routing over bridges/tunnels, and testing performance on a large dataset (country-level).
     */
    @Test
    public void testRouteMultipleWaypointsAlternativeRoutesCountryLevel() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.5689, -116.2207), // Starting point at Boise Airport, Boise, Idaho
                new GHPoint(44.0274, -116.9640), // Ontario, Oregon (waypoint 1)
                new GHPoint(44.4600, -110.8433), // Old Faithful, Yellowstone (waypoint 2)
                new GHPoint(46.6058, -112.0092)  // Helena, Montana (end point)
        );

        List<GHPoint> segmentPoints;
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)
                        .putHint("alternative_route.max_paths", 3)
                        .putHint("alternative_route.max_weight_factor", 5.0)
                        .putHint("alternative_route.max_share_factor", 0.6);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response.getAll().size() > 1;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one alternative route in concurrent execution", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 85: Routing with multiple waypoints, alternative routes enabled,
     * routing over bridges/tunnels, and testing high concurrency load.
     */
    @Test
    public void testRouteMultipleWaypointsAlternativeRoutesHighConcurrencyIdahoMontana() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6009, -116.2021), // Starting point in Boise, Idaho
                new GHPoint(44.0682, -112.5897), // Near Butte, Montana (waypoint 1)
                new GHPoint(44.5000, -112.1000), // Near Bozeman, Montana (waypoint 2)
                new GHPoint(46.5952, -112.0052), // Near Helena, Montana (waypoint 3)
                new GHPoint(46.8730, -113.9931), // Near Missoula, Montana (waypoint 4)
                new GHPoint(41.5932, -109.2247)  // Rock Springs, Wyoming (end point)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<GHPoint> segmentPoints;
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)
                        .putHint("alternative_route.max_paths", 3)
                        .putHint("alternative_route.max_weight_factor", 3.0)
                        .putHint("alternative_route.max_share_factor", 0.6);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response.getAll().size() > 1;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one alternative route in concurrent execution", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 86: Routing with multiple waypoints, avoid highways option enabled,
     * handling restricted access, and testing performance on a small dataset (city-level).
     */
    @Test
    public void testRouteAvoidHighwaysRestrictedAccessCityLevel() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6022, -116.2003), // Start Point: BSU Student Union
                new GHPoint(43.6166, -116.2023), // Waypoint 1: Downtown Boise
                new GHPoint(43.6400, -116.1964), // Waypoint 2: North End Boise
                new GHPoint(43.6280, -116.1830), // Waypoint 3: Boise River Greenbelt
                new GHPoint(43.6150, -116.1820)  // End Point: East Boise
        );

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<GHPoint> segmentPoints;
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true)
                        .putHint("vehicle.restricted", true);

                GHResponse response = hopper.route(request);

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                return true;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one route that avoids highways and handles restricted access", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 87: Routing with multiple waypoints, avoid highways option enabled,
     * handling restricted access, and testing performance on a small dataset (country-wide).
     */
    @Test
    public void testRouteAvoidHighwaysRestrictedAccessCityLevelCw() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.5459, -116.3129), // Albertsons, South Boise, Idaho
                new GHPoint(44.0262, -116.9630), // Ontario, Oregon
                new GHPoint(46.6175, -112.0156), // Buffalo Wild Wings, Helena, Montana
                new GHPoint(43.5113, -112.0182), // Idaho Falls, Idaho
                new GHPoint(43.4795, -110.7591)  // Jackson, Wyoming
        );

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<GHPoint> segmentPoints;
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true)
                        .putHint("vehicle.restricted", true);

                GHResponse response = hopper.route(request);

                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                if (response.hasErrors()) {
                    System.err.println("Routing error: " + response.getErrors());
                    return false;
                }

                return true;
            }));
        }

        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected at least one route that avoids highways and handles restricted access", future.get());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 88: Routing with multiple waypoints, avoid highways option enabled,
     * handling restricted access, and testing performance under high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysWithMultipleWaypointsAndConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6022, -116.2003), // Start Point: BSU Student Union
                new GHPoint(43.6166, -116.2023), // Waypoint 1: Downtown Boise
                new GHPoint(43.6400, -116.1964), // Waypoint 2: North End Boise
                new GHPoint(43.6280, -116.1830), // Waypoint 3: Boise River Greenbelt
                new GHPoint(43.6150, -116.1820)  // End Point: East Boise
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true)
                        .putHint("vehicle.restricted", true);

                return hopper.route(request);
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
                assertNotNull("Expected a non-null response", response);
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 89: Routing with multiple waypoints, avoid highways option enabled,
     * testing performance on a small dataset (city-level), and handling valid input.
     */
    @Test
    public void testRouteAvoidHighwaysWithMultipleWaypointsAndSmallDataset() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6022, -116.2003), // Start Point: BSU Student Union
                new GHPoint(43.6166, -116.2023), // Waypoint 1: Downtown Boise
                new GHPoint(43.6400, -116.1964), // Waypoint 2: North End Boise
                new GHPoint(43.6280, -116.1830), // Waypoint 3: Boise River Greenbelt
                new GHPoint(43.6150, -116.1820)  // End Point: East Boise
        );

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                return hopper.route(request);
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
                assertNotNull("Expected a non-null response", response);
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 90: Routing with multiple waypoints, avoid highways option enabled,
     * testing performance on a large dataset (country-level), and handling valid input.
     */
    @Test
    public void testRouteAvoidHighwaysWithMultipleWaypointsAndLargeDataset() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6022, -116.2003), // Start Point: Boise, Idaho (BSU Student Union)
                new GHPoint(42.5570, -114.4685), // Waypoint 1: Twin Falls, Idaho
                new GHPoint(46.5842, -112.0395), // Waypoint 2: Helena, Montana
                new GHPoint(45.5971, -111.0389), // Waypoint 3: Bozeman, Montana
                new GHPoint(44.0281, -116.9580)  // End Point: Kalispell, Montana
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                return hopper.route(request);
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
                assertNotNull("Expected a non-null response", response);
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 92: Routing with multiple waypoints between Idaho and Montana,
     * avoid highways option enabled, handling restricted access, and testing performance under high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysBetweenIdahoAndMontanaWithConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(47.7000, -116.7750), // Start Point: Near the Idaho-Montana border (Coeur d'Alene, ID)
                new GHPoint(46.8610, -114.1250), // Waypoint 1: Near Lolo, Montana (U.S. Route 12)
                new GHPoint(46.8772, -113.9969), // Waypoint 2: Near Missoula, Montana (I-90)
                new GHPoint(48.1920, -114.3120)  // End Point: Near Kalispell, Montana (Flathead River)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true)
                        .putHint("vehicle.restricted", true);

                GHResponse response = hopper.route(request);
                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
                assertNotNull("Expected a non-null response", response);
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 92: Routing with multiple waypoints across different time zones,
     * avoid highways option enabled, and testing performance on a small dataset (city-level).
     */
    @Test
    public void testRouteAvoidHighwaysAcrossTimeZones() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2020), // Start Point: Boise, Idaho (Mountain Time Zone)
                new GHPoint(48.2765, -116.5535), // Waypoint 1: Sandpoint, Idaho (Pacific Time Zone)
                new GHPoint(45.9982, -114.0426), // Waypoint 2: Near Missoula, Montana (Mountain Time Zone)
                new GHPoint(47.6125, -114.3235), // Waypoint 3: Near Kalispell, Montana (Mountain Time Zone)
                new GHPoint(48.2180, -114.3700)  // End Point: Flathead Lake, Montana (Mountain Time Zone)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 93: Routing with multiple waypoints across different time zones,
     * avoid highways option enabled, and testing performance on a large dataset (Idaho and Montana).
     */
    @Test
    public void testRouteAvoidHighwaysAcrossTimeZonesLargeDataset() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2020), // Start Point: Boise, Idaho (Mountain Time Zone)
                new GHPoint(48.2765, -116.5535), // Waypoint 1: Sandpoint, Idaho (Pacific Time Zone)
                new GHPoint(45.9982, -114.0426), // Waypoint 2: Near Missoula, Montana (Mountain Time Zone)
                new GHPoint(46.5900, -112.0250), // Waypoint 3: Near Helena, Montana (Mountain Time Zone)
                new GHPoint(47.6125, -114.3235)  // End Point: Near Kalispell, Montana (Mountain Time Zone)
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 94: Routing with multiple waypoints across different time zones,
     * avoid highways option enabled, and testing performance under high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysAcrossTimeZonesHighConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2020), // Start Point: Boise, Idaho (Mountain Time Zone)
                new GHPoint(48.2765, -116.5535), // Waypoint 1: Sandpoint, Idaho (Pacific Time Zone)
                new GHPoint(45.9982, -114.0426), // Waypoint 2: Near Missoula, Montana (Mountain Time Zone)
                new GHPoint(46.5900, -112.0250), // Waypoint 3: Near Helena, Montana (Mountain Time Zone)
                new GHPoint(47.6125, -114.3235)  // End Point: Near Kalispell, Montana (Mountain Time Zone)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 95: Routing with multiple waypoints, avoid highways option enabled,
     * and handling one-way streets in a city-level dataset.
     */
    @Test
    public void testRouteAvoidHighwaysWithOneWayStreets() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6166, -116.2023), // Start Point: Downtown Boise
                new GHPoint(43.6140, -116.1990), // Waypoint 1: Near City Hall
                new GHPoint(43.6100, -116.2010), // Waypoint 2: Near the Capitol Building
                new GHPoint(43.6070, -116.2045)  // End Point: Near the Basque Block
        );

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 96: Routing with multiple waypoints, avoid highways option enabled,
     * and handling one-way streets in a country-level dataset.
     */
    @Test
    public void testRouteAvoidHighwaysWithOneWayStreetsCountryLevel() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6166, -116.2023), // Start Point: Downtown Boise, Idaho
                new GHPoint(47.7000, -116.7750), // Near the Idaho-Montana border (Coeur d'Alene, ID)
                new GHPoint(46.8610, -114.1250), // Waypoint 1: Near Lolo, Montana (U.S. Route 12)
                new GHPoint(46.8772, -113.9969), // Waypoint 2: Near Missoula, Montana (I-90)
                new GHPoint(48.1920, -114.3120)  // End Point: Near Kalispell, Montana (Flathead River)
        );

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 97: Routing with multiple waypoints, avoid highways option enabled,
     * and handling one-way streets under high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysWithOneWayStreetsHighConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6166, -116.2023), // Start Point: Downtown Boise, Idaho
                new GHPoint(47.7000, -116.7750), // Near the Idaho-Montana border (Coeur d'Alene, ID)
                new GHPoint(46.8610, -114.1250), // Waypoint 1: Near Lolo, Montana (U.S. Route 12)
                new GHPoint(46.8772, -113.9969), // Waypoint 2: Near Missoula, Montana (I-90)
                new GHPoint(48.1920, -114.3120)  // End Point: Near Kalispell, Montana (Flathead River)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 98: Routing with multiple waypoints, avoid highways option enabled,
     * and routing over bridges/tunnels with a small dataset (city-level).
     */
    @Test
    public void testRouteAvoidHighwaysWithBridgesAndTunnels() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start Point: Downtown Boise
                new GHPoint(43.6250, -116.2150), // Waypoint 1: Boise River (near bridge)
                new GHPoint(43.6320, -116.2110), // Waypoint 2: Near the Veterans Memorial Parkway bridge
                new GHPoint(43.6300, -116.2070), // Waypoint 3: Boise Airport area (near tunnel)
                new GHPoint(43.6350, -116.1920)  // End Point: Boise State University (near bridge)
        );

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 99: Routing with multiple waypoints, avoid highways option enabled,
     * and routing over bridges/tunnels with a large dataset (Idaho and Montana).
     */
    @Test
    public void testRouteAvoidHighwaysWithBridgesAndTunnelsLargeDatasetIdahoMontana() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start Point: Boise, Idaho (near the Boise River and bridges)
                new GHPoint(44.5585, -111.2887), // Waypoint 1: Idaho Falls, Idaho (near Snake River Bridge)
                new GHPoint(46.5950, -112.0397), // Waypoint 2: Helena, Montana (near the Last Chance Gulch Tunnel)
                new GHPoint(47.6607, -114.3530), // Waypoint 3: Missoula, Montana (near bridges and tunnels)
                new GHPoint(48.3064, -114.3659)  // End Point: Kalispell, Montana (near bridges over Flathead River)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));
            List<GHPoint> finalSegmentPoints = segmentPoints;

            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 100: Routing with multiple waypoints, avoid highways option enabled,
     * and routing over bridges/tunnels with high concurrency load.
     */
    @Test
    public void testRouteAvoidHighwaysWithBridgesAndTunnelsHighConcurrency() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start Point: Boise, Idaho (near Boise River and bridges)
                new GHPoint(44.5585, -111.2887), // Waypoint 1: Idaho Falls, Idaho (near Snake River Bridge)
                new GHPoint(46.5950, -112.0397), // Waypoint 2: Helena, Montana (near Last Chance Gulch Tunnel)
                new GHPoint(47.6607, -114.3530), // Waypoint 3: Missoula, Montana (near bridges and tunnels)
                new GHPoint(48.3064, -114.3659)  // End Point: Kalispell, Montana (near bridges over Flathead River)
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segmentPoints = Arrays.asList(points.get(startIdx), points.get(endIdx));

            List<GHPoint> finalSegmentPoints = segmentPoints;
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(finalSegmentPoints)
                        .setProfile("profile")
                        .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertFalse("Expected successfull repsonse for valid input", response.hasErrors());
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }
}


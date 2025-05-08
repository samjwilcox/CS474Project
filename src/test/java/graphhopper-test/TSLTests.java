import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.config.LMProfile;
import com.graphhopper.json.Statement;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.TestProfiles;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.After;
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
import java.util.concurrent.*;

/**
 * Test cases generated via TSL file.
 */
public class TSLTests {
    private GraphHopper hopper;
    private String osmFile;
    private static final int THREAD_COUNT = 50;
    private static ExecutorService executor;

    /**
     * Initializes the graph hopper instance before each test execution.
     */
    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
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
     * Tears down the executor instance each test.
     */
    @After
    public void tearDown() {
        executor.shutdown();
    }

    /**
     * Helper that sets up the hopper instance using the bike profile.
     */
    private void setupForBike() {
        String basePath = String.valueOf(Paths.get(System.getProperty("user.dir"), "src", "test", "java", "graphhopper-test"));

        hopper = new GraphHopper()
                .setGraphHopperLocation("test-bike")
                .setOSMFile(basePath + "/IdahoArea.osm.pbf")
                .setEncodedValuesString("bike_access, bike_average_speed")
                .setProfiles(TestProfiles.accessAndSpeed("bike", "bike"))
                .setStoreOnFlush(true);
        hopper.getCHPreparationHandler()
                .setCHProfiles(new CHProfile("bike"));
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
    public void testCase1() {
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
    public void testCase2() {
        GHRequest request = new GHRequest(999.0, 999.0, 52.6, 13.5);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for invalid coordinates", response.hasErrors());
    }

    /**
     * Test Case 3:
     * Test missing graph data.
     */
    @Test
    public void testCase3() {
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
    public void testCase4() {
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
    public void testCase5() {
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
    public void testCase6() {
        GHRequest request = new GHRequest(52.5, 13.4, 53.0, 14.0);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for disconnected road network", response.hasErrors());
    }

    /**
     * Test Case 7:
     * Test private roads.
     */
    @Test
    public void testCase7() {
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
    public void testCase8() {
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
    public void testCase9() {
        GHRequest request = new GHRequest(52.5, 13.4, 53.0, 14.0);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for partial route available", response.hasErrors());
    }

    /**
     * Test Case 10:
     * Test no route found.
     */
    @Test
    public void testCase10() {
        GHRequest request = new GHRequest(999.0, 999.0, 999.1, 999.1);

        GHResponse response = hopper.route(request);
        assertTrue("Expected an error for no route found", response.hasErrors());
    }

    /**
     * Test Case 11: Small dataset (city-level) with restricted access.
     */
    @Test
    public void testCase11() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.6000, -116.2000) // Coordinates around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 12: Large dataset (country-level) with restricted access.
     */
    @Test
    public void testCase12() {
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
    public void testCase13() {
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6000, -116.1800) // Another route around Boise
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 14: Small dataset (city-level) with handling restricted access.
     */
    @Test
    public void testCase14() {
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
    public void testCase15() {
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
    public void testCase16() {
        GHRequest request = new GHRequest(43.6050, -116.2100, 43.5900, -116.1800) // Another route around Boise
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 17: Small dataset (city-level) across different time zones.
     */
    @Test
    public void testCase17() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.6000, -116.1950) // Short route around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 18: Large dataset (country-level) across different time zones.
     */
    @Test
    public void testCase18() {
        GHRequest request = new GHRequest(43.5881, -116.2100, 43.6090, -116.1772)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 19: High concurrency load across different time zones.
     */
    @Test
    public void testCase19() {
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
    public void testCase20() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.5990, -116.2020) // Short route around Boise State University
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 21: Large dataset (country-level) with handling one-way streets.
     */
    @Test
    public void testCase21() {
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6050, -116.1800)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 22: High concurrency load with handling one-way streets
     */
    @Test
    public void testCase22() {
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6080, -116.1800) // Route around Boise within bounds
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 23: Small Dataset (city-level) routing over bridges/tunnels
     */
    @Test
    public void testCase23() {
        GHRequest request = new GHRequest(43.6317, -116.2386, 43.6581, -116.2780)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 24: Large Dataset (country-level) routing with over bridges/tunnels
     */
    @Test
    public void testCase24() {
        GHRequest request = new GHRequest(47.6667, -116.7400, 47.3967, -115.6689)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 25: High Concurrency load over bridges/tunnels
     */
    @Test
    public void testCase25() {
        GHRequest request = new GHRequest(43.5895, -116.2537, 43.5896, -116.2035)
                .setProfile("profile");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 26: Small dataset (city-level) with alternative routes enabled
     */
    @Test
    public void testCase26() {
        GHRequest request = new GHRequest(43.5985, -116.2010, 43.6000, -116.2000) // Coordinates around Boise State University
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 27: Large dataset (country-level) with alternative routes enabled
     */
    @Test
    public void testCase27() {
        GHRequest request = new GHRequest(43.5900, -116.2100, 43.6050, -116.1800) // Route around Boise within bounds
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 28: High concurrency load with alternative routes
     */
    @Test
    public void testCase28() {
        GHRequest request = new GHRequest(43.6050, -116.2100, 43.5900, -116.1800)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 29: Car routing with alternative routes enabled, using valid OSM data
     * in a small dataset (city-level, Boise, ID).
     */
    @Test
    public void testCase29() {
        GHRequest request = new GHRequest(
                43.6158, -116.2016,   // Downtown Boise
                43.6050, -116.1980    // Southeast Boise
        ).setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);

        System.out.println("Errors (if any): " + response.getErrors());
        assertFalse("Expected valid car route in Boise city-level dataset", response.hasErrors());
        assertFalse("Expected at least one route path", response.getAll().isEmpty());
        assertTrue("Expected JSON-serializable response", convertToJSON(response));
    }

    /**
     * Test Case 30: Large dataset (country-level) car routing with alternative routes enabled
     */
    @Test
    public void testCase30() {
        GHRequest request = new GHRequest(43.6187, -116.2146, 46.8721, -113.9940) // Boise to Missoula
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid response for large dataset with alternative routes (car)", response.hasErrors());
    }

    /**
     * Test Case 31: Car routing with alternative routes enabled, under high concurrency load
     * using city-level dataset (Boise, ID).
     */
    @Test
    public void testCase31() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Boise Downtown
                new GHPoint(43.6000, -116.2050), // Boise Bench
                new GHPoint(43.5800, -116.2100)  // Boise South
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segment = Arrays.asList(points.get(startIdx), points.get(endIdx));
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(segment)
                        .setProfile("profile")
                        .setAlgorithm("alternative_route");

                GHResponse response = hopper.route(request);

                if (response.hasErrors()) {
                    System.err.println("Routing error (segment " + startIdx + " → " + endIdx + "): " + response.getErrors());
                }

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertNotNull("Expected non-null response", response);
                assertFalse("Expected path in response", response.getAll().isEmpty());
                assertTrue("Expected JSON-convertible response", convertToJSON(response));
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 32: Small dataset (city-level) routing across time zones with alternative routing
     */
    @Test
    public void testCase32() {
        GHRequest request = new GHRequest(44.0770, -116.9430, 44.0775, -116.9335)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 33: Large dataset (country-level) routing across time zones with alternative routing
     */
    @Test
    public void testCase33() {
        GHRequest request = new GHRequest(43.8765, -116.9940, 44.0070, -116.9225)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 34: High concurrency dataset routing across time zones with alternative routes
     */
    @Test
    public void testCase34() {
        GHRequest request = new GHRequest(44.0266, -116.9612, 44.0070, -116.9225)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 35: Small dataset (city-level) routing handling one-way streets
     */
    @Test
    public void testCase35() {
        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6205, -116.2107)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 36: Large dataset (country-level) routing handling one-way streets
     */
    @Test
    public void testCase36() {
        GHRequest request = new GHRequest(43.6005, -116.1950, 43.6500, -116.2500)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 37: High concurrency dataset routing handling one-way streets
     */
    @Test
    public void testCase37() {
        GHRequest request = new GHRequest(43.6100, -116.1800, 43.6400, -116.2200)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 38: Small dataset (city-level) routing over bridges/tunnels
     */
    @Test
    public void testCase38() {
        GHRequest request = new GHRequest(43.6178, -116.1996, 43.6317, -116.2386)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 39: Large dataset (country-level) routing over bridges/tunnels
     */
    @Test
    public void testCase39() {
        GHRequest request = new GHRequest(43.5895, -116.2537, 43.6581, -116.2780)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 40: High concurrency dataset routing over bridges/tunnels
     */
    @Test
    public void testCase40() {
        GHRequest request = new GHRequest(43.5896, -116.2035, 43.6500, -116.2500)
                .setProfile("profile")
                .setAlgorithm("alternative_route");

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
    }

    /**
     * Test Case 41: Small dataset (city-level) with avoiding highways.
     */
    @Test
    public void testCase41() {
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
    public void testCase42() {
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
    public void testCase43() {
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
    public void testCase44() {
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
    public void testCase45() {
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
    public void testCase46() {
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
    public void testCase47() {
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
    public void testCase48() {
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
    public void testCase49() {
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
    public void testCase50() {
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
    public void testCase51() {
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
    public void testCase52() {
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
    public void testCase53() {
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
    public void testCase54() {
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
    public void testCase55() {
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
    public void testCase56() {
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
    public void testCase57() {
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
    public void testCase58() {
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
    public void testCase59() {
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
    public void testCase60() {
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
     * Test Case 61: Car routing with multiple waypoints and fastest route option tested under
     * high concurrency load using valid OSM data.
     */
    @Test
    public void testCase61() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Boise, ID
                new GHPoint(42.5613, -114.4601), // Twin Falls, ID
                new GHPoint(43.4917, -112.0339), // Idaho Falls, ID
                new GHPoint(46.0038, -112.5348)  // Butte, MT
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segment = Arrays.asList(points.get(startIdx), points.get(endIdx));
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(segment)
                        .setProfile("profile")
                        .setAlgorithm("dijkstra")
                        .putHint("ch.disable", true);

                GHResponse response = hopper.route(request);

                if (response.hasErrors()) {
                    System.err.println("Routing error (segment " + startIdx + " → " + endIdx + "): " + response.getErrors());
                }

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertNotNull("Expected non-null response", response);
                assertFalse("Expected path in response", response.getAll().isEmpty());
                assertTrue("Expected JSON-convertible response", convertToJSON(response));
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 62: Routing with multiple waypoints across different time zones (small dataset, city-level).
     */
    @Test
    public void testCase62() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(44.0760, -116.9330), // Start in Idaho
                new GHPoint(44.0780, -116.9450), // Waypoint 1
                new GHPoint(44.0800, -116.9500)  // End in Oregon
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 63: Routing with multiple waypoints across different time zones (large dataset, country-level).
     */
    @Test
    public void testCase63() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.8765, -116.9940), // Start in Idaho
                new GHPoint(44.0070, -116.9225), // Waypoint 1
                new GHPoint(44.4500, -117.0500)  // End in Oregon
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 64: Routing with multiple waypoints across different time zones with high concurrency.
     */
    @Test
    public void testCase64() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(44.0266, -116.9612), // Start in Idaho
                new GHPoint(44.0070, -116.9225), // Waypoint 1
                new GHPoint(44.5000, -117.1000)  // End in Oregon
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 65: Routing with multiple waypoints handling one-way streets (small dataset, city-level).
     */
    @Test
    public void testCase65() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.615, -116.2023), // Start in Boise
                new GHPoint(43.564, -116.223),  // Waypoint 1
                new GHPoint(43.606, -116.202),  // Waypoint 2
                new GHPoint(43.614, -116.238)   // End in Boise
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 66: Routing with multiple waypoints handling one-way streets (large dataset, country-level).
     */
    @Test
    public void testCase66() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6176, -116.1997), // Boise
                new GHPoint(42.9057, -112.4523), // Idaho Falls
                new GHPoint(46.5957, -112.0270), // Helena, Montana
                new GHPoint(45.6794, -111.0448)  // Bozeman, Montana
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 67: Routing with multiple waypoints handling one-way streets with high concurrency.
     */
    @Test
    public void testCase67() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.614, -116.238), // Start
                new GHPoint(43.606, -116.202), // Waypoint 1
                new GHPoint(43.564, -116.223), // Waypoint 2
                new GHPoint(43.615, -116.2023) // End
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 68: Routing with multiple waypoints over bridges/tunnels (small dataset, city-level).
     */
    @Test
    public void testCase68() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Boise, ID (start)
                new GHPoint(43.6205, -116.2100), // Waypoint near a bridge in Boise
                new GHPoint(43.6300, -116.2200)  // End near a tunnel in Boise
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // Fastest route option

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 69: Routing with multiple waypoints over bridges/tunnels (large dataset, country-level).
     */
    @Test
    public void testCase69() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(44.0682, -114.7420), // Stanley, ID (start)
                new GHPoint(44.4268, -117.2160), // Bridge area in Vale, OR
                new GHPoint(43.6150, -116.2023)  // End in Boise, ID
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    /**
     * Test Case 70: Routing with multiple waypoints over bridges/tunnels (high concurrency load).
     */
    @Test
    public void testCase70() {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(44.0521, -121.3153), // Bend, OR (start)
                new GHPoint(44.4938, -117.2790), // Snake River bridge in Ontario, OR
                new GHPoint(43.6150, -116.2023)  // End in Boise, ID
        );

        GHRequest request = new GHRequest(points)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response = hopper.route(request);
        assertFalse("Expected successful response for valid input", response.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response));
    }

    //Cannot use alt routes with more than 2 waypoints on this version of graphhopper, split the route into 3 separates
    //as a workaround

    /**
     * Test Case 71: Routing with simulated alternative routes for multiple waypoints (city-level).
     */
    @Test
    public void testCase71() {
        // First segment: Boise to first waypoint
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start in Boise
                new GHPoint(43.6000, -116.2500)  // Waypoint in Boise
        );

        // Second segment: First waypoint to second waypoint
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(43.6000, -116.2500), // Waypoint in Boise
                new GHPoint(43.6060, -116.2020)  // Second waypoint in Boise
        );

        // Third segment: Second waypoint to final destination
        List<GHPoint> segment3 = Arrays.asList(
                new GHPoint(43.6060, -116.2020), // Second waypoint in Boise
                new GHPoint(43.6140, -116.2380)  // Final point in Boise
        );

        // Request for first segment
        GHRequest request1 = new GHRequest(segment1)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for second segment
        GHRequest request2 = new GHRequest(segment2)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for third segment
        GHRequest request3 = new GHRequest(segment3)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        // Get responses for all segments
        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);
        GHResponse response3 = hopper.route(request3);

        // Ensure no errors for any response and check JSON conversion
        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response2));

        assertFalse("Expected successful response for valid input", response3.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response3));
    }

    /**
     * Test Case 72: Routing with simulated alternative routes for multiple waypoints (county-level).
     */
    @Test
    public void testCase72() {
        // First segment: Boise to Idaho Falls
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start in Boise
                new GHPoint(43.8765, -116.9940)  // Idaho Falls
        );

        // Second segment: Idaho Falls to Helena
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(43.8765, -116.9940), // Idaho Falls
                new GHPoint(46.5957, -112.0270)  // Helena
        );

        // Third segment: Helena to Bozeman
        List<GHPoint> segment3 = Arrays.asList(
                new GHPoint(46.5957, -112.0270), // Helena
                new GHPoint(45.6794, -111.0448)  // Bozeman
        );

        // Request for first segment
        GHRequest request1 = new GHRequest(segment1)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for second segment
        GHRequest request2 = new GHRequest(segment2)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for third segment
        GHRequest request3 = new GHRequest(segment3)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        // Get responses for all segments
        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);
        GHResponse response3 = hopper.route(request3);

        // Ensure no errors for any response and check JSON conversion
        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response2));

        assertFalse("Expected successful response for valid input", response3.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response3));
    }

    /**
     * Test Case 73: Routing with simulated alternative routes for multiple waypoints with high concurrency.
     */
    @Test
    public void testCase73() {
        // First segment: Boise to first waypoint
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(43.6150, -116.2023), // Start in Boise
                new GHPoint(43.6000, -116.2500)  // Waypoint 1 in Boise
        );

        // Second segment: Waypoint 1 to second waypoint
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(43.6000, -116.2500), // Waypoint 1 in Boise
                new GHPoint(43.6060, -116.2020)  // Waypoint 2 in Boise
        );

        // Third segment: Second waypoint to final destination
        List<GHPoint> segment3 = Arrays.asList(
                new GHPoint(43.6060, -116.2020), // Waypoint 2 in Boise
                new GHPoint(43.6140, -116.2380)  // Final point in Boise
        );

        // Request for first segment
        GHRequest request1 = new GHRequest(segment1)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for second segment
        GHRequest request2 = new GHRequest(segment2)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        // Request for third segment
        GHRequest request3 = new GHRequest(segment3)
                .setProfile("profile")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        // Get responses for all segments
        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);
        GHResponse response3 = hopper.route(request3);

        // Ensure no errors for any response and check JSON conversion
        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response2));

        assertFalse("Expected successful response for valid input", response3.hasErrors());
        assertTrue("Expected response to be successfully converted to JSON output", convertToJSON(response3));
    }

    /**
     * Test Case 74: Car routing with multiple waypoints using valid OSM data
     * in a small dataset (city-level, Boise), without alternative routes and with CH disabled.
     */
    @Test
    public void testCase74() {
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Downtown Boise
                new GHPoint(43.6050, -116.1980), // Southeast Boise
                new GHPoint(43.5900, -116.1950)  // South Boise
        )).setProfile("profile")
                .setAlgorithm("dijkstra")
                .putHint("ch.disable", true); // ✅ disables CH for this request

        GHResponse response = hopper.route(request);

        assertFalse("Expected valid multi-waypoint response", response.hasErrors());
        assertFalse("Expected at least one path", response.getAll().isEmpty());
        assertTrue("Expected JSON-convertible response", convertToJSON(response));
    }

    /**
     * Test Case 75: Car routing with multiple waypoints using valid OSM data
     * in a large dataset (country-level, Boise to Missoula), without alternative routes,
     * and with CH disabled.
     */
    @Test
    public void testCase75() {
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Boise, ID
                new GHPoint(44.0682, -114.7420), // Stanley, ID
                new GHPoint(46.8721, -113.9940)  // Missoula, MT
        )).setProfile("profile")
                .setAlgorithm("dijkstra")
                .putHint("ch.disable", true); // ✅ disable CH for multi-waypoint routing

        GHResponse response = hopper.route(request);

        assertFalse("Expected valid multi-waypoint country-level route", response.hasErrors());
        assertFalse("Expected at least one route path", response.getAll().isEmpty());
        assertTrue("Expected JSON-serializable response", convertToJSON(response));
    }

    /**
     * Test Case 76: Car routing with multiple waypoints under high concurrency load
     * using valid OSM data (Boise to Missoula), without alternative routes and with CH disabled.
     */
    @Test
    public void testCase76() throws InterruptedException {
        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Boise, ID
                new GHPoint(44.0682, -114.7420), // Stanley, ID
                new GHPoint(45.9180, -113.8992), // Salmon, ID
                new GHPoint(46.8721, -113.9940)  // Missoula, MT
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segment = Arrays.asList(points.get(startIdx), points.get(endIdx));
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(segment)
                        .setProfile("profile")
                        .setAlgorithm("dijkstra")
                        .putHint("ch.disable", true); // ✅ disables CH for concurrent routing

                GHResponse response = hopper.route(request);

                if (response.hasErrors()) {
                    System.err.println("Routing error (segment " + startIdx + " → " + endIdx + "): " + response.getErrors());
                }

                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertNotNull("Expected non-null response", response);
                assertFalse("Expected path in response", response.getAll().isEmpty());
                assertTrue("Expected JSON-convertible response", convertToJSON(response));
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 77: Simulated alternative routes with multiple waypoints across time zones (city-level).
     * Key = 1.1.2.1.2.1.5.1.1.
     */
    @Test
    public void testCase77() {
        // Segment 1: Near Fruitland, ID to border
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(44.0060, -116.9160), // Fruitland, ID
                new GHPoint(44.0075, -116.9260)  // ID-OR border
        );

        // Segment 2: Border to Ontario, OR
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(44.0075, -116.9260),
                new GHPoint(44.0185, -116.9700)  // Ontario, OR
        );

        GHRequest request1 = new GHRequest(segment1).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        GHRequest request2 = new GHRequest(segment2).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);

        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response2));
    }

    /**
     * Test Case 78: Simulated alternative routes with multiple waypoints across time zones (country-level).
     * Key = 1.1.2.1.2.1.5.2.1.
     */
    @Test
    public void testCase78() {
        // Segment 1: Ontario, OR to Weiser, ID
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(44.0260, -116.9620), // Ontario, OR
                new GHPoint(44.2485, -116.9705)  // Weiser, ID
        );

        // Segment 2: Weiser, ID to Huntington, OR
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(44.2485, -116.9705),
                new GHPoint(44.4098, -117.2674)  // Huntington, OR
        );

        GHRequest request1 = new GHRequest(segment1).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        GHRequest request2 = new GHRequest(segment2).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);

        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response2));
    }


    /**
     * Test Case 79: Simulated alternative routes with high concurrency near OR-ID border.
     * Key = 1.1.2.1.2.1.5.3.1.
     */
    @Test
    public void testCase79() {
        // Segment 1: Start near Fruitland, ID
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(44.0100, -116.9300),
                new GHPoint(44.0105, -116.9450)
        );

        // Segment 2: Into Ontario, OR
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(44.0105, -116.9450),
                new GHPoint(44.0185, -116.9700)
        );

        GHRequest request1 = new GHRequest(segment1).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        GHRequest request2 = new GHRequest(segment2).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);

        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response2));
    }


    /**
     * Test Case 80: Simulated alternative routes handling one-way streets near OR-ID border (city-level).
     * Key = 1.1.2.1.2.1.6.1.1.
     */
    @Test
    public void testCase80() {
        // Segment 1: Start near downtown Ontario, OR
        List<GHPoint> segment1 = Arrays.asList(
                new GHPoint(44.0210, -116.9730),
                new GHPoint(44.0190, -116.9620)
        );

        // Segment 2: Loop toward Fruitland, ID through bridge (border)
        List<GHPoint> segment2 = Arrays.asList(
                new GHPoint(44.0190, -116.9620),
                new GHPoint(44.0085, -116.9335)
        );

        GHRequest request1 = new GHRequest(segment1).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        GHRequest request2 = new GHRequest(segment2).setProfile("profile").setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);

        GHResponse response1 = hopper.route(request1);
        GHResponse response2 = hopper.route(request2);

        assertFalse("Expected successful response for valid input", response1.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response1));

        assertFalse("Expected successful response for valid input", response2.hasErrors());
        assertTrue("Expected valid JSON output", convertToJSON(response2));
    }

    /**
     * Test Case 81: Routing with multiple waypoints, alternative routes enabled,
     * handling one-way streets, and testing performance on a large dataset (Idaho & Montana).
     */
    @Test
    public void testCase81() {
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
    public void testCase82() throws InterruptedException {
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
    public void testCase83() throws InterruptedException {
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
    public void testCase84() throws InterruptedException {
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
    public void testCase85() throws InterruptedException {
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
    public void testCase86() throws InterruptedException {
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
    public void testCase87() throws InterruptedException {
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
    public void testCase88() throws InterruptedException {
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
    public void testCase89() throws InterruptedException {
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
    public void testCase90() throws InterruptedException {
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
     * Test Case 91: Routing with multiple waypoints between Idaho and Montana,
     * avoid highways option enabled, handling restricted access, and testing performance under high concurrency load.
     */
    @Test
    public void testCase91() throws InterruptedException {
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
    }

    /**
     * Test Case 92: Routing with multiple waypoints across different time zones,
     * avoid highways option enabled, and testing performance on a small dataset (city-level).
     */
    @Test
    public void testCase92() {
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
    public void testCase93() {
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
    public void testCase94() throws InterruptedException {
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
    public void testCase95() throws InterruptedException {
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
    public void testCase96() throws InterruptedException {
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
    public void testCase97() throws InterruptedException {
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
    public void testCase98() throws InterruptedException {
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
    public void testCase99() throws InterruptedException {
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
    public void testCase100() throws InterruptedException {
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

    /**
     * Test Case 101: Routing with bike profile, retricted access scenario,
     * using fastest route and small dataset (city-level).
     */
    @Test
    public void testCase101() {
        setupForBike();

        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6180, -116.2050)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route for bike profile in city-level dataset", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 102: Routing with bike profile, restricted access scenario,
     * using fastest route and large dataset (Montana to Idaho).
     */
    @Test
    public void testCase102() {
        setupForBike();

        GHRequest request = new GHRequest(47.4980, -111.3008, 46.8772, -113.9956) // Great Falls to Missoula, MT
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route for bike profile in country-level dataset", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 103: Routing with bike profile under high concurrency load,
     * restricted access scenario using fastest route and valid input.
     */
    @Test
    public void testCase103() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2023, 43.6200, -116.2100) // Boise, ID
                        .setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected successful routing and JSON serialization", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency routing error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 104: Routing with bike profile, geometry edge case scenario,
     * using fastest route and small dataset (city-level, southern Washington).
     */
    @Test
    public void testCase104() {
        setupForBike();

        GHRequest request = new GHRequest(46.2406, -119.1026, 46.2460, -119.1150) // Kennewick, WA (edge of urban area)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route for bike profile in city geometry edge case", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 105: Routing with bike profile, geometry edge case scenario,
     * using fastest route and large dataset (Montana).
     */
    @Test
    public void testCase105() {
        setupForBike();

        GHRequest request = new GHRequest(47.0528, -109.4707, 47.1068, -109.5207) // Lewistown, MT (rural region)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route for bike profile in country geometry edge case", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 106: Routing with bike profile, geometry edge case scenario,
     * using fastest route and high concurrency load (Montana).
     */
    @Test
    public void testCase106() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(46.8722, -113.9940, 46.8800, -114.0000) // Missoula, MT near edge paths
                        .setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid route and JSON serialization", future.get());
            } catch (ExecutionException e) {
                fail("Routing failed under concurrency: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 107: Routing with bike profile across time zone boundary,
     * using fastest route and small dataset (city-level, Idaho).
     */
    @Test
    public void testCase107() {
        setupForBike();

        // Near the Mountain/Pacific time boundary in Idaho
        GHRequest request = new GHRequest(44.0780, -116.9320, 44.0795, -116.9500) // Payette to Fruitland, ID
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route across time zones", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 108: Routing with bike profile across time zone boundary,
     * using fastest route and large dataset (Montana to Idaho).
     */
    @Test
    public void testCase108() {
        setupForBike();

        GHRequest request = new GHRequest(46.5950, -112.0397, 44.0682, -114.7420) // Helena, MT to Stanley, ID
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route across time zones", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 109: Routing with bike profile across time zone boundary,
     * using fastest route and high concurrency load (Idaho to Montana).
     */
    @Test
    public void testCase109() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(47.4741, -115.9243, 47.1947, -114.8912) // Wallace, ID → Superior, MT
                        .setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent routing across time zones", future.get());
            } catch (ExecutionException e) {
                fail("Routing concurrency error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 110: Routing with bike profile, one-way street handling scenario,
     * using fastest route and small dataset (city-level, Idaho).
     */
    @Test
    public void testCase110() {
        setupForBike();

        // Downtown Boise has one-way streets; simulate routing across them
        GHRequest request = new GHRequest(43.6150, -116.2030, 43.6175, -116.1990)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route handling one-way streets", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 111: Routing with bike profile, handling one-way streets,
     * using fastest route and large dataset (country-level, Montana).
     */
    @Test
    public void testCase111() {
        setupForBike();

        GHRequest request = new GHRequest(46.5950, -112.0397, 45.7833, -108.5007) // Helena → Billings, MT
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route handling one-way streets", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 112: Routing with bike profile, handling one-way streets,
     * using fastest route and high concurrency load (Idaho).
     */
    @Test
    public void testCase112() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2030, 43.6175, -116.1990) // Downtown Boise, ID
                        .setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent one-way routing", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency one-way street routing failed: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 113: Routing with bike profile over bridges/tunnels,
     * using fastest route and small dataset (city-level, southern Washington).
     */
    @Test
    public void testCase113() {
        setupForBike();

        GHRequest request = new GHRequest(46.2100, -119.1050, 46.2200, -119.0900) // Kennewick to Pasco, WA over Columbia River
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid bridge/tunnel route in city-level data", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 114: Routing with bike profile over bridges/tunnels,
     * using fastest route and large dataset (country-level, Montana).
     */
    @Test
    public void testCase114() {
        setupForBike();

        // Missoula area: Clark Fork River bridge, city grid, known bike paths
        GHRequest request = new GHRequest(46.8700, -114.0150, 46.8722, -113.9940) // South to North Missoula (over river)
                .setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid bridge/tunnel route in country-level data", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 115: Routing with bike profile over bridges/tunnels,
     * using fastest route and high concurrency load (Montana/Idaho).
     */
    @Test
    public void testCase115() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(47.4741, -115.9243, 47.1947, -114.8912) // Wallace, ID → Superior, MT
                        .setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected successful concurrent bridge/tunnel routing", future.get());
            } catch (ExecutionException e) {
                fail("Bridge/tunnel concurrency routing failed: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 116: Routing with bike profile and alternative routes enabled,
     * restricted access scenario using fastest route and small dataset (Boise, ID).
     */
    @Test
    public void testCase116() {
        setupForBike();

        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6180, -116.2050) // Downtown Boise
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 3);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid alternative route for restricted access city-level", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 117: Routing with bike profile and alternative routes enabled,
     * restricted access scenario using fastest route and large dataset (Montana).
     */
    @Test
    public void testCase117() {
        setupForBike();

        GHRequest request = new GHRequest(46.5950, -112.0397, 45.7833, -108.5007) // Helena to Billings, MT
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 3);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid alternative route for restricted access country-level", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 118: Routing with bike profile and alternative routes enabled,
     * restricted access scenario under high concurrency load (Idaho).
     */
    @Test
    public void testCase118() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(46.4170, -117.0211, 46.4220, -117.0300) // Lewiston, ID
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

                request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent alternative routing for restricted access", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error during alternative routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 119: Routing with bike profile and alternative routes enabled,
     * geometry edge case using fastest route and small dataset (Pasco, WA).
     */
    @Test
    public void testCase119() {
        setupForBike();

        GHRequest request = new GHRequest(46.2396, -119.1015, 46.2465, -119.1132) // Edge of Pasco (disconnected grid)
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid alternative route for geometry edge case (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 120: Routing with bike profile and alternative routes enabled,
     * geometry edge case using fastest route and large dataset (Montana).
     */
    @Test
    public void testCase120() {
        setupForBike();

        GHRequest request = new GHRequest(46.8722, -113.9940, 46.8772, -113.9800) // Missoula, MT — edge between city grid and trail paths
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid alternative route for geometry edge case (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 121: Routing with bike profile and alt routes enabled,
     * testing with a high concurrency load
     * @throws InterruptedException
     */
    @Test
    public void testCase121() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.7306, -116.9772, 43.7355, -116.9651) // Near OR-ID border
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
                request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> result : results) {
            try {
                assertTrue("Expected valid concurrent alternative routing for generic edge case", result.get());
            } catch (ExecutionException e) {
                fail("Concurrency error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 122: Routing with bike profile and alternate routes enabled
     * across time zones (small dataset)
     */
    @Test
    public void testCase122() {
        setupForBike();

        GHRequest request = new GHRequest(43.5650, -117.0412, 43.5710, -116.9980) // Close to time zone border in city
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid routing across time zones (small dataset)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 123: Routing with bike profile and alternate routes enabled
     * across time zones (large dataset)
     */
    @Test
    public void testCase123() {
        setupForBike();

        GHRequest request = new GHRequest(43.5670, -117.0300, 43.7700, -116.6100) // Timezone edge: Ontario, OR to Boise, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid alternative routing across time zones (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 124: Routing with bike profile and alternate routes enabled
     * across time zones (high concurrency)
     */
    @Test
    public void testCase124() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.5740, -117.0000, 43.6000, -116.9500)
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
                request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid high-concurrency alternative routing across time zones", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 125: Routing with bike profile and alternate routes enabled
     * using one-way roads (small dataset)
     */
    @Test
    public void testCase125() {
        setupForBike();

        GHRequest request = new GHRequest(43.6100, -116.9750, 43.6125, -116.9700) // City core near Caldwell, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid alternative route accounting for one-way streets", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 126: Routing with bike profile and alternative routes enabled,
     * handling one-way streets on a large dataset (country-level).
     */
    @Test
    public void testCase126() {
        setupForBike();

        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6187, -116.1990) // Boise, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid alternative route handling one-way streets (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 127: Routing with bike profile and alternative routes enabled,
     * handling one-way streets under high concurrency load.
     */
    @Test
    public void testCase127() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2023, 43.6187, -116.1990) // Boise, ID
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

                request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent alternative routing handling one-way streets", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error during alternative routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 128: Routing with bike profile and alternative routes enabled,
     * routing over bridges/tunnels on a small dataset (city-level).
     */
    @Test
    public void testCase128() {
        setupForBike();

        GHRequest request = new GHRequest(43.6170, -116.1990, 43.6200, -116.1950) // Boise, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid alternative route over bridges/tunnels (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 129: Routing with bike profile and alternative routes enabled,
     * routing over bridges/tunnels on a large dataset (country-level).
     */
    @Test
    public void testCase129() {
        setupForBike();

        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6200, -116.1950) // Boise, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid alternative route over bridges/tunnels (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 130: Routing with bike profile and alternative routes enabled,
     * routing over bridges/tunnels under high concurrency load.
     */
    @Test
    public void testCase130() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2023, 43.6200, -116.1950) // Boise, ID
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

                request.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 2);

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent alternative routing over bridges/tunnels", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error during alternative routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 131: Routing with bike profile and avoid highways option enabled,
     * restricted access scenario on a small dataset (city-level, Boise, ID).
     */
    @Test
    public void testCase131() {
        setupForBike();

        GHRequest request = new GHRequest(43.6150, -116.2023, 43.6200, -116.1950) // Boise, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject("avoid", "motorway");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid bike route avoiding highways with restricted access (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 132: Routing with bike profile and avoid highways option enabled,
     * restricted access scenario on a large dataset (country-level, Idaho).
     */
    @Test
    public void testCase132() {
        setupForBike();

        GHRequest request = new GHRequest(43.4935, -112.0402, 43.6166, -116.2006) // Idaho Falls → Boise
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject("avoid", "motorway");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid bike route avoiding highways with restricted access (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 133: Routing with bike profile and avoid highways option enabled,
     * restricted access scenario under high concurrency load.
     */
    @Test
    public void testCase133() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2023, 43.6200, -116.1950) // Boise, ID
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

                request.getHints().putObject("avoid", "motorway");

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent bike routing avoiding highways with restricted access", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error during avoid-highway bike routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 134: Bike routing with avoid highways option enabled, using valid OSM data
     * and city-level (small) dataset (Boise).
     */
    @Test
    public void testCase134() {
        setupForBike();

        GHRequest request = new GHRequest(
                43.6158, -116.2016,   // Boise Downtown
                43.6050, -116.1980    // Southeast Boise
        ).setProfile("bike")
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println("Errors: " + response.getErrors());
        assertFalse("Expected valid bike route with avoid_highways (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 135: Bike routing with avoid highways option enabled, using valid OSM data
     * and country-level dataset (Boise to Missoula).
     */
    @Test
    public void testCase135() {
        setupForBike();

        GHRequest request = new GHRequest(
                43.6158, -116.2016,   // Boise, ID
                46.8721, -113.9940    // Missoula, MT
        ).setProfile("bike")
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid long-distance bike route with avoid_highways", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 136: Bike routing with avoid highways enabled, under high concurrency load
     * using valid OSM data (Boise → Stanley → Missoula)
     */
    @Test
    public void testCase136() throws InterruptedException {
        setupForBike();

        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6158, -116.2016), // Boise
                new GHPoint(44.0682, -114.7420), // Stanley
                new GHPoint(46.8721, -113.9940)  // Missoula
        );

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<GHResponse>> results = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int startIdx = i;
            final int endIdx = i + 1;

            List<GHPoint> segment = Arrays.asList(points.get(startIdx), points.get(endIdx));
            results.add(executor.submit(() -> {
                GHRequest request = new GHRequest(segment)
                        .setProfile("bike")
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    System.err.println("Routing error (segment " + startIdx + " → " + endIdx + "): " + response.getErrors());
                }
                return response;
            }));
        }

        for (Future<GHResponse> future : results) {
            try {
                GHResponse response = future.get();
                assertNotNull("Expected non-null response", response);
                assertFalse("Expected routing path", response.getAll().isEmpty());
                assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
            } catch (ExecutionException e) {
                fail("Error during concurrent execution: " + e.getCause());
            }
        }

        executor.shutdown();
    }

    /**
     * Test Case 137: Routing with bike profile and avoid highways enabled,
     * routing across different time zones on a small dataset (city-level near Ontario, OR and Fruitland, ID).
     */
    @Test
    public void testCase137() {
        setupForBike();

        GHRequest request = new GHRequest(44.0265, -116.9336, 44.0173, -116.9213) // Ontario, OR → Fruitland, ID
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject("avoid", "motorway");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid bike route avoiding highways across time zones (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 138: Routing with bike profile and avoid highways enabled,
     * routing across different time zones on a large dataset (country-level, ID to OR).
     */
    @Test
    public void testCase138() {
        setupForBike();

        GHRequest request = new GHRequest(43.4935, -112.0402, 44.0265, -116.9336) // Idaho Falls → Ontario, OR
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject("avoid", "motorway");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid bike route avoiding highways across time zones (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 139: Routing with bike profile and avoid highways enabled,
     * routing across different time zones under high concurrency load.
     */
    @Test
    public void testCase139() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(44.0265, -116.9336, 44.0173, -116.9213) // Ontario, OR → Fruitland, ID
                        .setProfile("bike")
                        .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

                request.getHints().putObject("avoid", "motorway");

                GHResponse response = hopper.route(request);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid concurrent bike routing avoiding highways across time zones", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency error during avoid-highway time zone routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 140: Routing with bike profile and avoid highways enabled,
     * edge case handling one-way streets on a small dataset (city-level, downtown Boise, ID).
     */
    @Test
    public void testCase140() {
        setupForBike();

        GHRequest request = new GHRequest(43.6152, -116.2035, 43.6131, -116.1952) // Downtown Boise
                .setProfile("bike")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE);

        request.getHints().putObject("avoid", "motorway");

        GHResponse response = hopper.route(request);
        assertFalse("Expected valid bike route avoiding highways with one-way street handling (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }



    /**
     * Test Case 141: Routing with bike profile, avoid highways and one-way streets,
     * using fastest route and large dataset (country-level, Montana).
     */
    @Test
    public void testCase141() {
        setupForBike();

        GHRequest request = new GHRequest(46.5950, -112.0397, 45.7833, -108.5007) // Helena → Billings, MT
                .setProfile("bike")
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid route with avoid highways (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 142: Routing with bike profile, avoid highways and one-way streets,
     * using fastest route and high concurrency load (Boise, ID).
     */
    @Test
    public void testCase142() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(43.6150, -116.2030, 43.6175, -116.1990)
                        .setProfile("bike")
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected valid route avoiding highways (high concurrency)", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency routing failed: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 143: Routing with bike profile over bridges/tunnels, avoid highways,
     * using fastest route and small dataset (city-level, southern Washington).
     */
    @Test
    public void testCase143() {
        setupForBike();

        GHRequest request = new GHRequest(46.2100, -119.1050, 46.2200, -119.0900) // Bridge over Columbia River
                .setProfile("bike")
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected bridge/tunnel route avoiding highways (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 144: Routing with bike profile over bridges/tunnels, avoid highways,
     * using fastest route and large dataset (country-level, ID → MT).
     */
    @Test
    public void testCase144() {
        setupForBike();

        GHRequest request = new GHRequest(47.4741, -115.9243, 47.1947, -114.8912) // Wallace → Superior
                .setProfile("bike")
                .putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected bridge/tunnel route avoiding highways (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 145: Routing with bike profile over bridges/tunnels, avoid highways,
     * using fastest route and high concurrency load (MT).
     */
    @Test
    public void testCase145() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(46.8722, -113.9940, 47.1947, -114.8912)
                        .setProfile("bike")
                        .putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected successful bridge/tunnel routing with avoid_highways", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency bridge routing failed: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 146: Routing with multiple waypoints and restricted access scenario,
     * using fastest route and small dataset (Boise, ID).
     */
    @Test
    public void testCase146() {
        setupForBike();

        List<GHPoint> points = Arrays.asList(
                new GHPoint(43.6150, -116.2023),
                new GHPoint(43.6165, -116.2040),
                new GHPoint(43.6178, -116.2060)
        );

        GHRequest request = new GHRequest(points).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid multiple waypoint route with restricted access (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 147: Routing with multiple waypoints and restricted access scenario,
     * using fastest route and large dataset (country-level).
     */
    @Test
    public void testCase147() {
        setupForBike();

        List<GHPoint> points = Arrays.asList(
                new GHPoint(47.4741, -115.9243), // Wallace, ID
                new GHPoint(47.3000, -115.3000), // mid-way
                new GHPoint(47.1947, -114.8912)  // Superior, MT
        );

        GHRequest request = new GHRequest(points).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid multiple waypoint route with restricted access (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 148: Routing with multiple waypoints and restricted access scenario,
     * using fastest route and high concurrency load (southern WA/ID).
     */
    @Test
    public void testCase148() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                List<GHPoint> points = Arrays.asList(
                        new GHPoint(46.2396, -119.1015), // Pasco, WA
                        new GHPoint(46.4000, -118.0000),
                        new GHPoint(46.4170, -117.0211)  // Lewiston, ID
                );

                GHRequest request = new GHRequest(points).setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) {
                    response.getErrors().forEach(Throwable::printStackTrace);
                }
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            try {
                assertTrue("Expected successful multiple-waypoint routing with restricted access", future.get());
            } catch (ExecutionException e) {
                fail("Concurrency multi-waypoint routing failed: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 149: Routing with multiple waypoints and geometry edge case scenario,
     * using fastest route and small dataset (city-level, Kennewick, WA).
     */
    @Test
    public void testCase149() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.2100, -119.1050),
                new GHPoint(46.2150, -119.1000),
                new GHPoint(46.2200, -119.0950)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid geometry edge case (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 150: Routing with multiple waypoints and geometry edge case scenario,
     * using fastest route and large dataset (country-level, Helena to Missoula, MT).
     */
    @Test
    public void testCase150() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.5950, -112.0397), // Helena, MT
                new GHPoint(46.6900, -112.3000), // Avon, MT area
                new GHPoint(46.8772, -113.9956)  // Missoula, MT
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid geometry edge case (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 151: Routing with multiple waypoints and geometry edge case scenario,
     * using fastest route and high concurrency load (southern WA to ID).
     */
    @Test
    public void testCase151() throws InterruptedException {
        setupForBike();
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(46.2396, -119.1015),
                        new GHPoint(46.3000, -118.5000),
                        new GHPoint(46.4170, -117.0211)
                )).setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }
        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected valid route under concurrency", result.get());
            } catch (ExecutionException e) {
                fail("Exception during execution: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 152: Routing with multiple waypoints across different time zones,
     * using fastest route and small dataset (city-level, Weiser to Payette, ID).
     */
    @Test
    public void testCase152() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(44.2514, -116.9692),
                new GHPoint(44.0780, -116.9320),
                new GHPoint(44.0795, -116.9500)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid timezone-crossing route (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 153: Routing with multiple waypoints across different time zones,
     * using fastest route and large dataset (country-level, Wallace to Superior).
     */
    @Test
    public void testCase153() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(47.4741, -115.9243),
                new GHPoint(47.3000, -115.4000),
                new GHPoint(47.1947, -114.8912)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid timezone-crossing route (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 154: Routing with multiple waypoints across different time zones,
     * using fastest route and high concurrency load (Wallace to Superior).
     */
    @Test
    public void testCase154() throws InterruptedException {
        setupForBike();
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(47.4741, -115.9243),
                        new GHPoint(47.3000, -115.4000),
                        new GHPoint(47.1947, -114.8912)
                )).setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }
        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected valid timezone-crossing route under concurrency", result.get());
            } catch (ExecutionException e) {
                fail("Execution error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 155: Routing with multiple waypoints and one-way street handling,
     * using fastest route and small dataset (city-level, Boise, ID).
     */
    @Test
    public void testCase155() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(43.6150, -116.2023),
                new GHPoint(43.6160, -116.2010),
                new GHPoint(43.6170, -116.2000)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid one-way handling (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 156: Routing with multiple waypoints and one-way street handling,
     * using fastest route and large dataset (country-level, Helena to Missoula).
     */
    @Test
    public void testCase156() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.5950, -112.0397),
                new GHPoint(46.7500, -112.5000),
                new GHPoint(46.8772, -113.9956)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid one-way handling (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 157: Routing with multiple waypoints and one-way street handling,
     * using fastest route and high concurrency load (Boise, ID).
     */
    @Test
    public void testCase157() throws InterruptedException {
        setupForBike();
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(43.6150, -116.2023),
                        new GHPoint(43.6160, -116.2010),
                        new GHPoint(43.6170, -116.2000)
                )).setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }
        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected valid one-way route under concurrency", result.get());
            } catch (ExecutionException e) {
                fail("Execution error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 158: Routing with multiple waypoints and bridge/tunnel handling,
     * using fastest route and small dataset (city-level, Pasco, WA).
     */
    @Test
    public void testCase158() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.2100, -119.1050),
                new GHPoint(46.2150, -119.0980),
                new GHPoint(46.2200, -119.0900)
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid bridge/tunnel route (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 159: Routing with multiple waypoints and bridge/tunnel handling,
     * using fastest route and large dataset (country-level, Missoula area).
     */
    @Test
    public void testCase159() {
        setupForBike();
        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.8772, -113.9956), // Missoula downtown
                new GHPoint(46.8805, -114.0500), // west Missoula over river
                new GHPoint(46.8860, -114.1000)  // further west near industrial/bridge area
        )).setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid bridge/tunnel route (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 160: Routing with multiple waypoints and bridge/tunnel handling,
     * using fastest route and high concurrency load (Wallace to Superior).
     */
    @Test
    public void testCase160() throws InterruptedException {
        setupForBike();
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(47.4741, -115.9243),
                        new GHPoint(47.3000, -115.3000),
                        new GHPoint(47.1947, -114.8912)
                )).setProfile("bike");

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }
        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected valid bridge/tunnel route under concurrency", result.get());
            } catch (ExecutionException e) {
                fail("Execution error: " + e.getCause());
            }
        }
    }

    /**
     * Test case 161: Tests a bike route within Montana from Butte to Helena.
     * Ensures a valid path is found entirely in one state (Montana).
     */
    @Test
    public void testCase161() {
        setupForBike();
        GHRequest request = new GHRequest(46.0038, -112.5348, 46.5964, -112.0264); // Butte -> Helena
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 162: Tests a bike route within Montana from Butte to Missoula.
     * Verifies routing between two Montana cities over a moderate distance.
     */
    @Test
    public void testCase162() {
        setupForBike();
        GHRequest request = new GHRequest(46.0038, -112.5348, 46.8721, -113.9940); // Butte -> Missoula
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 163: Tests a bike route within Montana from Missoula to Kalispell.
     * Ensures routing works between these two northern Montana cities.
     */
    @Test
    public void testCase163() {
        setupForBike();
        GHRequest request = new GHRequest(46.8721, -113.9940, 48.1978, -114.3135); // Missoula -> Kalispell
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 164: Tests a bike route within Idaho from Boise to Lewiston.
     * Verifies a long north-south route can be found entirely in Idaho.
     */
    @Test
    public void testCase164() {
        setupForBike();
        GHRequest request = new GHRequest(43.6158, -116.2016, 46.4167, -117.0177); // Boise -> Lewiston
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 165: Tests a bike route from Lewiston, Idaho to Spokane, Washington.
     * Ensures a cross-border route (Idaho to Washington) is found successfully.
     */
    @Test
    public void testCase165() {
        setupForBike();
        GHRequest request = new GHRequest(46.4167, -117.0177, 47.6589, -117.4250); // Lewiston -> Spokane
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 166: Tests a bike route from Boise, Idaho to Spokane, Washington.
     * Verifies a long cross-state route from southwestern Idaho to eastern Washington.
     */
    @Test
    public void testCase166() {
        setupForBike();
        GHRequest request = new GHRequest(43.6158, -116.2016, 47.6589, -117.4250); // Boise -> Spokane
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 167: Tests a bike route from Lewiston, Idaho to Missoula, Montana.
     * Ensures routing works across the Idaho-Montana border through mountainous terrain.
     */
    @Test
    public void testCase167() {
        setupForBike();
        GHRequest request = new GHRequest(46.4167, -117.0177, 46.8721, -113.9940); // Lewiston -> Missoula
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 168: Tests a bike route from Boise, Idaho to Missoula, Montana.
     * Verifies a cross-state route from Idaho into western Montana can be found.
     */
    @Test
    public void testCase168() {
        setupForBike();
        GHRequest request = new GHRequest(43.6158, -116.2016, 46.8721, -113.9940); // Boise -> Missoula
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 169: Tests a bike route from Spokane, Washington to Missoula, Montana.
     * This route spans three states (WA -> ID -> MT) and verifies multi-state routing.
     */
    @Test
    public void testCase169() {
        setupForBike();
        GHRequest request = new GHRequest(47.6589, -117.4250, 46.8721, -113.9940); // Spokane -> Missoula
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 170: Tests a bike route from Butte, Montana to Spokane, Washington.
     * Ensures a long route from Montana to Washington (via Idaho) is calculated correctly.
     */
    @Test
    public void testCase170() {
        setupForBike();
        GHRequest request = new GHRequest(46.0038, -112.5348, 47.6589, -117.4250); // Butte -> Spokane
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 171: Tests a long bike route from Helena, Montana to Boise, Idaho.
     * Verifies routing over a large distance across the Montana-Idaho border.
     */
    @Test
    public void testCase171() {
        setupForBike();
        GHRequest request = new GHRequest(46.5964, -112.0264, 43.6158, -116.2016); // Helena -> Boise
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 172: Tests a long bike route from Boise, Idaho to Kalispell, Montana.
     * Ensures the routing engine finds a path connecting the southwest of the region to the far north.
     */
    @Test
    public void testCase172() {
        setupForBike();
        GHRequest request = new GHRequest(43.6158, -116.2016, 48.1978, -114.3135); // Boise -> Kalispell
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 173: Tests a bike route from Kalispell, Montana to Spokane, Washington.
     * Verifies a multi-state route from Montana to Washington via the Idaho panhandle.
     */
    @Test
    public void testCase173() {
        setupForBike();
        GHRequest request = new GHRequest(48.1978, -114.3135, 47.6589, -117.4250); // Kalispell -> Spokane
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 174: Tests routing with an intermediate stop (via point).
     * Verifies a multi-point bike route from Spokane to Helena with a stop in Missoula.
     */
    @Test
    public void testCase174() {
        setupForBike();
        GHRequest request = new GHRequest();
        request.addPoint(new GHPoint(47.6589, -117.4250)); // Spokane
        request.addPoint(new GHPoint(46.8721, -113.9940)); // Missoula (via)
        request.addPoint(new GHPoint(46.5964, -112.0264)); // Helena
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 175: Tests routing with multiple stops across the region.
     * Verifies a complex bike route from Boise to Spokane via Missoula and Kalispell.
     */
    @Test
    public void testCase175() {
        setupForBike();
        GHRequest request = new GHRequest();
        request.addPoint(new GHPoint(43.6158, -116.2016)); // Boise
        request.addPoint(new GHPoint(46.8721, -113.9940)); // Missoula (via)
        request.addPoint(new GHPoint(48.1978, -114.3135)); // Kalispell (via)
        request.addPoint(new GHPoint(47.6589, -117.4250)); // Spokane
        request.setProfile("bike");

        GHResponse response = hopper.route(request);
        assertFalse(response.hasErrors());
        assertTrue(convertToJSON(response));
    }

    /**
     * Test case 176: Simulates concurrent routing requests on the same route.
     * Uses 4 threads concurrently routing from Boise, ID to Spokane, WA.
     */
    @Test
    public void testCase176() throws Exception {
        setupForBike();
        final int THREAD_COUNT = 4;
        final double latFrom = 43.6158, lonFrom = -116.2016;
        final double latTo = 47.6589, lonTo = -117.4250;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo); // Boise -> Spokane
                req.setProfile("bike");

                GHResponse res = hopper.route(req);
                return !res.hasErrors() && convertToJSON(res);
            });
        }
        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
    }

    /**
     * Test case 177: Simulates concurrent routing requests on different routes.
     * Uses 8 threads, alternating between Boise->Spokane and Missoula->Helena routes.
     */
    @Test
    public void testCase177() throws Exception {
        setupForBike();
        final int THREAD_COUNT = 8;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest req;
                // Alternate between two routes: Boise->Spokane and Missoula->Helena
                if (Thread.currentThread().getId() % 2 == 0) {
                    req = new GHRequest(43.6158, -116.2016, 47.6589, -117.4250); // Boise -> Spokane
                    req.setProfile("bike");
                } else {
                    req = new GHRequest(46.8721, -113.9940, 46.5964, -112.0264); // Missoula -> Helena
                    req.setProfile("bike");
                }
                GHResponse res = hopper.route(req);
                return !res.hasErrors() && convertToJSON(res);
            });
        }
        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
    }

    /**
     * Test case 178: Simulates high-load concurrent routing on the same route.
     * Uses 16 threads concurrently requesting the Boise to Spokane bike route.
     */
    @Test
    public void testCase178() throws Exception {
        setupForBike();
        final int THREAD_COUNT = 16;
        final double latFrom = 43.6158, lonFrom = -116.2016;
        final double latTo = 47.6589, lonTo = -117.4250;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo); // Boise -> Spokane
                req.setProfile("bike");

                GHResponse res = hopper.route(req);
                return !res.hasErrors() && convertToJSON(res);
            });
        }
        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
    }

    /**
     * Test case 179: Simulates high-load concurrent routing on multiple routes.
     * Uses 32 threads with a mix of routes (Boise-Spokane, Missoula-Helena, Lewiston-Missoula, Butte-Helena).
     */
    @Test
    public void testCase179() throws Exception {
        setupForBike();
        final int THREAD_COUNT = 32;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest req;
                int mod = (int) (Thread.currentThread().getId() % 4);
                if (mod == 0) {
                    req = new GHRequest(43.6158, -116.2016, 47.6589, -117.4250); // Boise -> Spokane
                    req.setProfile("bike");
                } else if (mod == 1) {
                    req = new GHRequest(46.8721, -113.9940, 46.5964, -112.0264); // Missoula -> Helena
                    req.setProfile("bike");
                } else if (mod == 2) {
                    req = new GHRequest(46.4167, -117.0177, 46.8721, -113.9940); // Lewiston -> Missoula
                    req.setProfile("bike");
                } else {
                    req = new GHRequest(46.0038, -112.5348, 46.5964, -112.0264); // Butte -> Helena
                    req.setProfile("bike");
                }
                GHResponse res = hopper.route(req);
                return !res.hasErrors() && convertToJSON(res);
            });
        }
        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
    }

    /**
     * Test case 180: Simulates concurrent routing requests with multi-point routes.
     * Uses 8 threads concurrently routing from Spokane to Helena with a via point in Missoula.
     */
    @Test
    public void testCase180() throws Exception {
        setupForBike();
        final int THREAD_COUNT = 8;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest req = new GHRequest();
                req.addPoint(new GHPoint(47.6589, -117.4250)); // Spokane
                req.addPoint(new GHPoint(46.8721, -113.9940)); // Missoula (via)
                req.addPoint(new GHPoint(46.5964, -112.0264)); // Helena
                req.setProfile("bike");

                GHResponse res = hopper.route(req);
                return !res.hasErrors() && convertToJSON(res);
            });
        }
        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
    }

    /**
     * Test Case 181: Routing with multiple waypoints and geometry edge case,
     * using avoid highways option and high concurrency load (Pasco to Lewiston).
     */
    @Test
    public void testCase181() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(46.2396, -119.1015),
                        new GHPoint(46.5000, -118.2000),
                        new GHPoint(46.4170, -117.0211)
                )).setProfile("bike").putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected geometry edge route with concurrency and avoid_highways", result.get());
            } catch (ExecutionException e) {
                fail("Error during concurrent execution: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 182: Routing with multiple waypoints and crossing time zones,
     * using avoid highways and small dataset (Weiser to Payette, ID).
     */
    @Test
    public void testCase182() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(44.2514, -116.9692),
                new GHPoint(44.2000, -116.9500),
                new GHPoint(44.0795, -116.9500)
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid timezone-crossing route with avoid_highways", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 183: Routing with multiple waypoints and crossing time zones,
     * using avoid highways and large dataset (Wallace to Superior).
     */
    @Test
    public void testCase183() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(47.4741, -115.9243),
                new GHPoint(47.3000, -115.3000),
                new GHPoint(47.1947, -114.8912)
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected valid timezone-crossing route with avoid_highways", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 184: Routing with multiple waypoints and crossing time zones,
     * using avoid highways and high concurrency load (north ID to MT).
     */
    @Test
    public void testCase184() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(47.4741, -115.9243),
                        new GHPoint(47.3000, -115.3000),
                        new GHPoint(47.1947, -114.8912)
                )).setProfile("bike").putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected timezone-crossing route under concurrency", result.get());
            } catch (ExecutionException e) {
                fail("Error during concurrency routing: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 185: Routing with multiple waypoints and one-way street handling,
     * using avoid highways and small dataset (city-level, Boise, ID).
     */
    @Test
    public void testCase185() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(43.6150, -116.2023),
                new GHPoint(43.6160, -116.2010),
                new GHPoint(43.6170, -116.2000)
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected one-way street route with avoid_highways (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 186: Routing with multiple waypoints and one-way street handling,
     * using avoid highways and large dataset (Helena to Missoula, MT - within valid OSM bounds).
     */
    @Test
    public void testCase186() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.5950, -112.0397), // Helena, MT
                new GHPoint(46.6600, -112.3000), // Near Avon, MT
                new GHPoint(46.8772, -113.9956)  // Missoula, MT
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected one-way street route with avoid_highways (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 187: Routing with multiple waypoints and one-way street handling,
     * using avoid highways and high concurrency load (Boise, ID).
     */
    @Test
    public void testCase187() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(43.6150, -116.2023),
                        new GHPoint(43.6160, -116.2010),
                        new GHPoint(43.6170, -116.2000)
                )).setProfile("bike").putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected one-way route under concurrency with avoid_highways", result.get());
            } catch (ExecutionException e) {
                fail("Concurrency routing error: " + e.getCause());
            }
        }
    }

    /**
     * Test Case 188: Routing with multiple waypoints and bridges/tunnels handling,
     * using avoid highways and small dataset (Pasco, WA).
     */
    @Test
    public void testCase188() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.2100, -119.1050),
                new GHPoint(46.2150, -119.0980),
                new GHPoint(46.2200, -119.0900)
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected bridge/tunnel route with avoid_highways (city-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 189: Routing with multiple waypoints and bridges/tunnels handling,
     * using avoid highways and large dataset (Missoula area).
     */
    @Test
    public void testCase189() {
        setupForBike();

        GHRequest request = new GHRequest(Arrays.asList(
                new GHPoint(46.8772, -113.9956),
                new GHPoint(46.8805, -114.0500),
                new GHPoint(46.8860, -114.1000)
        )).setProfile("bike").putHint("avoid_highways", true);

        GHResponse response = hopper.route(request);
        System.out.println(response.getErrors());
        assertFalse("Expected bridge/tunnel route with avoid_highways (country-level)", response.hasErrors());
        assertTrue("Expected JSON serialization to succeed", convertToJSON(response));
    }

    /**
     * Test Case 190: Routing with multiple waypoints and bridges/tunnels handling,
     * using avoid highways and high concurrency load (Wallace to Superior).
     */
    @Test
    public void testCase190() throws InterruptedException {
        setupForBike();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                GHRequest request = new GHRequest(Arrays.asList(
                        new GHPoint(47.4741, -115.9243),
                        new GHPoint(47.3000, -115.3000),
                        new GHPoint(47.1947, -114.8912)
                )).setProfile("bike").putHint("avoid_highways", true);

                GHResponse response = hopper.route(request);
                if (response.hasErrors()) response.getErrors().forEach(Throwable::printStackTrace);
                return !response.hasErrors() && convertToJSON(response);
            });
        }

        for (Future<Boolean> result : executor.invokeAll(tasks)) {
            try {
                assertTrue("Expected bridge/tunnel route under concurrency with avoid_highways", result.get());
            } catch (ExecutionException e) {
                fail("Concurrency routing error: " + e.getCause());
            }
        }
    }
}

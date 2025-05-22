import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*

class NavigationControllerTest {
    @Test
    fun testDocsEndpoint() = testApplication {
        val response = client.get("/docs")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        
        // Just make sure the response is a valid JSON
        val json = try {
            Json.parseToJsonElement(body)
            true
        } catch (e: Exception) {
            false
        }
        assertTrue(json, "Response should be valid JSON")
        
        // Check that response contains all expected namespaces
        val expectedNamespaces = listOf(
            "sklearn", "sklearn.base", "sklearn.calibration", 
            "sklearn.cluster", "sklearn.compose", "sklearn.covariance"
        )
        
        for (namespace in expectedNamespaces) {
            assertTrue(body.contains("\"$namespace\""), "Response should contain namespace '$namespace'")
        }
    }
    
    @Test
    fun testDocsNamespaceEndpoint() = testApplication {
        val response = client.get("/docs/sklearn.covariance")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        
        // Parse the JSON response
        val jsonElement = Json.parseToJsonElement(body)
        
        // Convert to string map for easier assertion
        val jsonString = body.trim()
        
        // Verify structure - main fields
        assertTrue(jsonString.contains("\"type\""), "Response should contain type field")
        assertTrue(jsonString.contains("\"namespace\""), "Response should contain namespace field")
        assertTrue(jsonString.contains("\"parameters\""), "Response should contain parameters field")
        assertTrue(jsonString.contains("\"attributes\""), "Response should contain attributes field")
        
        // Verify content - specific values
        assertTrue(jsonString.contains("\"type\": \"function\""), "Response should have function type")
        assertTrue(jsonString.contains("\"namespace\": \"sklearn.covariance.ShrunkCovariance\""), "Response should have correct namespace")
        
        // Verify parameters - check for specific parameter
        assertTrue(jsonString.contains("\"name\": \"store_precision\""), "Should have store_precision parameter")
        assertTrue(jsonString.contains("\"name\": \"assume_centered\""), "Should have assume_centered parameter")
        assertTrue(jsonString.contains("\"name\": \"shrinkage\""), "Should have shrinkage parameter")
        
        // Verify attributes - check for specific attributes
        assertTrue(jsonString.contains("\"name\": \"covariance_\""), "Should have covariance_ attribute")
        assertTrue(jsonString.contains("\"name\": \"location_\""), "Should have location_ attribute")
        assertTrue(jsonString.contains("\"name\": \"precision_\""), "Should have precision_ attribute")
        assertTrue(jsonString.contains("\"name\": \"n_features_in_\""), "Should have n_features_in_ attribute")
        assertTrue(jsonString.contains("\"name\": \"feature_names_in_\""), "Should have feature_names_in_ attribute")
    }
    
    @Test
    fun testFrontendCompatibleDocsEndpoint() = testApplication {
        // Test the /docs endpoint that provides data for frontend navigation
        val response = client.get("/docs")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        
        // Ensure the response is valid JSON for frontend parsing
        val json = try {
            Json.parseToJsonElement(body)
            true
        } catch (e: Exception) {
            false
        }
        assertTrue(json, "Response should be valid JSON for frontend consumption")
        
        // Check for namespaces the frontend needs to display in the navigation
        val expectedNamespaces = listOf("sklearn.covariance", "sklearn.compose")
        for (namespace in expectedNamespaces) {
            assertTrue(body.contains("\"$namespace\""), "Response should contain namespace '$namespace' for frontend navigation")
        }
        
        // Check for functions mapping structure that the frontend needs
        assertTrue(body.contains("\"functions\""), "Response should contain 'functions' map for frontend navigation")
        
        // Check for specific function names that should be in the response
        assertTrue(body.contains("\"empirical_covariance\""), "Response should contain function names for frontend navigation")
        assertTrue(body.contains("\"graphical_lasso\""), "Response should contain function names for frontend navigation")
    }
    
    @Test
    fun testFrontendCompatibleNamespaceEndpoint() = testApplication {
        // Test the /docs/{namespace} endpoint for frontend component compatibility
        val response = client.get("/docs/sklearn.covariance")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        
        // Ensure the response is valid JSON for frontend parsing
        val json = try {
            Json.parseToJsonElement(body)
            true
        } catch (e: Exception) {
            false
        }
        assertTrue(json, "Response should be valid JSON for frontend consumption")
        
        // Check fields required by the Details page component
        assertTrue(body.contains("\"type\""), "Response should contain 'type' field for frontend typing")
        assertTrue(body.contains("\"namespace\""), "Response should contain 'namespace' field for frontend display")
        
        // Check parameter structure needed by the frontend parameter list component
        assertTrue(body.contains("\"parameters\""), "Response should contain 'parameters' array for frontend rendering")
        assertTrue(body.contains("\"name\""), "Parameters should have 'name' field for frontend display")
        assertTrue(body.contains("\"type\""), "Parameters should have 'type' field for frontend display")
        assertTrue(body.contains("\"defaultValue\""), "Parameters should have 'defaultValue' field for frontend forms")
        
        // Check attribute structure needed by the frontend attribute list component
        assertTrue(body.contains("\"attributes\""), "Response should contain 'attributes' array for frontend rendering")
        
        // Verify structure needed for frontend component rendering
        val hasStructureForFrontend = body.contains("\"parameters\"") && 
                                     body.contains("\"attributes\"") && 
                                     body.contains("\"type\"") &&
                                     body.contains("\"namespace\"")
        assertTrue(hasStructureForFrontend, "Response structure should support frontend component rendering")
    }

    @Test
    fun testFrontendReactComponentCompatibility() = testApplication {
        // Test the API endpoint compatibility with the React components in the frontend
        
        // Test the /docs endpoint which is used by the NamespaceAccordion.jsx component
        val docsResponse = client.get("/docs")
        assertEquals(HttpStatusCode.OK, docsResponse.status)
        val docsBody = docsResponse.bodyAsText()
        
        // Validate that the response structure matches what NamespaceAccordion.jsx expects
        assertTrue(docsBody.contains("\"sklearn.covariance\""), "Response should contain namespace keys for NamespaceAccordion.jsx")
        assertTrue(docsBody.contains("\"functions\""), "Response should contain functions array for NamespaceAccordion.jsx")
        
        // Test the /docs/{namespace} endpoint which is used by the Details.jsx component
        val namespaceResponse = client.get("/docs/sklearn.covariance")
        assertEquals(HttpStatusCode.OK, namespaceResponse.status)
        val namespaceBody = namespaceResponse.bodyAsText()
        
        // Validate that the response contains the fields needed by Details.jsx
        assertTrue(namespaceBody.contains("\"type\""), "Response should contain type field for Details.jsx")
        assertTrue(namespaceBody.contains("\"namespace\""), "Response should contain namespace field for Details.jsx")
        assertTrue(namespaceBody.contains("\"parameters\""), "Response should contain parameters array for Details.jsx")
        assertTrue(namespaceBody.contains("\"attributes\""), "Response should contain attributes array for Details.jsx")
        
        // Check for specific formatting elements that Details.jsx needs to render parameters
        assertTrue(namespaceBody.contains("\"name\""), "Response should contain name fields for parameter rendering")
        assertTrue(namespaceBody.contains("\"type\""), "Response should contain type fields for parameter rendering")
        assertTrue(namespaceBody.contains("\"defaultValue\""), "Response should contain defaultValue fields for parameter rendering")
        
        // Verify search capabilities needed by the frontend search component
        val searchableContent = namespaceBody.contains("\"name\"") && 
                              namespaceBody.contains("\"type\"") && 
                              namespaceBody.contains("\"parameters\"")
        assertTrue(searchableContent, "Response should support frontend search functionality")
    }

    @Test
    fun testApiResponseMatchesFrontendComponents() = testApplication {
        // Test that API responses match exactly what the frontend components expect
        
        // 1. Test /docs endpoint against NamespaceAccordion.jsx requirements
        val docsResponse = client.get("/docs")
        assertEquals(HttpStatusCode.OK, docsResponse.status)
        val docsBody = docsResponse.bodyAsText()
        
        // Parse the JSON to validate exact structure
        val docsJson = Json.parseToJsonElement(docsBody).jsonObject
        
        // NamespaceAccordion.jsx expects a map with namespace keys and objects containing functions and classes
        val sampleNamespace = "sklearn.covariance"
        assertTrue(docsJson.containsKey(sampleNamespace), 
            "Response should have namespaces as top-level keys for NamespaceAccordion")
        
        val namespaceContent = docsJson[sampleNamespace]?.jsonObject
        assertTrue(namespaceContent != null, "Namespace content should be a JSON object")
        assertTrue(namespaceContent!!.containsKey("functions"), 
            "Namespace should contain 'functions' array as used in NamespaceAccordion.jsx line 68-70")
        assertTrue(namespaceContent.containsKey("classes") || namespaceContent["classes"]?.jsonArray?.isEmpty() == false, 
            "Namespace should contain 'classes' array as used in NamespaceAccordion.jsx line 64-66")
        
        // 2. Test /docs/{namespace} endpoint against Details.jsx requirements
        val namespaceResponse = client.get("/docs/sklearn.covariance")
        assertEquals(HttpStatusCode.OK, namespaceResponse.status)
        val namespaceBody = namespaceResponse.bodyAsText()
        
        // Parse the JSON to validate exact structure for Details.jsx
        val namespaceJson = Json.parseToJsonElement(namespaceBody).jsonObject
        
        // Details.jsx line 29 accesses data.namespace
        assertTrue(namespaceJson.containsKey("namespace"), 
            "Response should have 'namespace' field as displayed in Details.jsx line 29")
        
        // Details.jsx line 36-40 processes data.parameters
        assertTrue(namespaceJson.containsKey("parameters"), 
            "Response should have 'parameters' array as used in Details.jsx line 36")
        
        // Test parameter structure (name, type, defaultValue, description)
        val parameters = namespaceJson["parameters"]?.jsonArray
        assertTrue(parameters != null && parameters.isNotEmpty(), 
            "Parameters should be a non-empty array")
        
        val sampleParam = parameters!![0].jsonObject
        assertTrue(sampleParam.containsKey("name"), 
            "Parameter should have 'name' field as displayed in Details.jsx line 38")
        assertTrue(sampleParam.containsKey("type"), 
            "Parameter should have 'type' field as displayed in Details.jsx line 38")
        assertTrue(sampleParam.containsKey("defaultValue"), 
            "Parameter should have 'defaultValue' field as displayed in Details.jsx line 38")
        assertTrue(sampleParam.containsKey("description"), 
            "Parameter should have 'description' field as displayed in Details.jsx line 41")
        
        // Details.jsx line 46-57 processes data.attributes
        assertTrue(namespaceJson.containsKey("attributes"), 
            "Response should have 'attributes' array as used in Details.jsx line 46-57")
        
        // Test attribute structure
        val attributes = namespaceJson["attributes"]?.jsonArray
        assertTrue(attributes != null && attributes.isNotEmpty(), 
            "Attributes should be a non-empty array")
        
        val sampleAttr = attributes!![0].jsonObject
        assertTrue(sampleAttr.containsKey("name"), 
            "Attribute should have 'name' field as displayed in Details.jsx line 53")
        assertTrue(sampleAttr.containsKey("type"), 
            "Attribute should have 'type' field as displayed in Details.jsx line 53")
        assertTrue(sampleAttr.containsKey("description"), 
            "Attribute should have 'description' field as displayed in Details.jsx line 56")
    }
}



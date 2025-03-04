//package com.security;
//
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.w3c.dom.*;
//import javax.xml.parsers.*;
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.*;
//import org.json.JSONObject;
//import org.json.JSONArray;
//import org.owasp.dependencycheck.Engine;
//import org.owasp.dependencycheck.dependency.Dependency;
//import org.owasp.dependencycheck.dependency.Vulnerability;
//import org.owasp.dependencycheck.utils.Settings;
//
//@RestController
//@RequestMapping("/api/dependency")
//public class DependencyAnalyzerController {
//
//    private static final String MAVEN_SEARCH_URL = "https://search.maven.org/solrsearch/select";
//    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";
//    private static final String NVDAPI_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";
//
//    // You should store this securely in application.properties
//    @Value("${nvd.api.key}")
//    private String nvdApiKey;
//
//    @PostMapping("/analyze")
//    public ResponseEntity<?> analyzeDependencies(@RequestParam("pomFile") MultipartFile pomFile) {
//        try {
//            Map<String, DependencyAnalysis> analysis = analyzePomFile(pomFile);
//            return ResponseEntity.ok(analysis);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error analyzing dependencies: " + e.getMessage());
//        }
//    }
//
//    private Map<String, DependencyAnalysis> analyzePomFile(MultipartFile pomFile) throws Exception {
//        Map<String, DependencyAnalysis> results = new HashMap<>();
//        Document doc = loadPomFile(pomFile);
//        NodeList dependencies = doc.getElementsByTagName("dependency");
//
//        for (int i = 0; i < dependencies.getLength(); i++) {
//            Element dependency = (Element) dependencies.item(i);
//            String groupId = getElementContent(dependency, "groupId");
//            String artifactId = getElementContent(dependency, "artifactId");
//            String currentVersion = getElementContent(dependency, "version");
//
//            DependencyAnalysis analysis = analyzeDependency(groupId, artifactId, currentVersion);
//            String key = groupId + ":" + artifactId;
//            results.put(key, analysis);
//        }
//
//        return results;
//    }
//
//    private DependencyAnalysis analyzeDependency(String groupId, String artifactId, String currentVersion) {
//        DependencyAnalysis analysis = new DependencyAnalysis();
//        analysis.setGroupId(groupId);
//        analysis.setArtifactId(artifactId);
//        analysis.setCurrentVersion(currentVersion);
//
//        try {
//            // Get latest version from Maven Central
//            String latestVersion = getLatestVersion(groupId, artifactId);
//            analysis.setLatestVersion(latestVersion);
//
//            // Check for vulnerabilities in current version
//            List<VulnerabilityInfo> vulnerabilities = checkVulnerabilities(groupId, artifactId, currentVersion);
//            analysis.setVulnerabilities(vulnerabilities);
//
//            // Determine if upgrade is needed
//            boolean needsUpgrade = vulnerabilities.stream()
//                .anyMatch(v -> v.getSeverity().equals("HIGH") || v.getSeverity().equals("CRITICAL"));
//            analysis.setNeedsUpgrade(needsUpgrade);
//
//            // Get suggested version (if needs upgrade)
//            if (needsUpgrade) {
//                String suggestedVersion = findSafeVersion(groupId, artifactId, vulnerabilities);
//                analysis.setSuggestedVersion(suggestedVersion);
//            }
//
//        } catch (Exception e) {
//            analysis.setError("Error analyzing dependency: " + e.getMessage());
//        }
//
//        return analysis;
//    }
//
//    private List<VulnerabilityInfo> checkVulnerabilities(String groupId, String artifactId, String version) {
//        List<VulnerabilityInfo> vulnerabilities = new ArrayList<>();
//
//        try (Engine engine = new Engine(new Settings())) {
//            // Create temporary POM with single dependency
//            File tempPom = createTempPom(groupId, artifactId, version);
//
//            engine.scan(tempPom);
//            engine.analyzeDependencies();
//
//            for (Dependency dependency : engine.getDependencies()) {
//                for (Vulnerability vuln : dependency.getVulnerabilities()) {
//                    VulnerabilityInfo info = new VulnerabilityInfo();
//                    info.setCve(vuln.getName());
//                    info.setSeverity(vuln.getCvssV3() != null ?
//                        vuln.getCvssV3().getBaseSeverity() : "UNKNOWN");
//                    info.setDescription(vuln.getDescription());
//
//                    // Get additional details from NVD API
//                    enrichVulnerabilityInfo(info);
//
//                    vulnerabilities.add(info);
//                }
//            }
//
//            tempPom.delete();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return vulnerabilities;
//    }
//
//    private void enrichVulnerabilityInfo(VulnerabilityInfo info) {
//        try {
//            URL url = new URL(NVDAPI_URL + "?cveId=" + info.getCve());
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestProperty("apiKey", nvdApiKey);
//
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(conn.getInputStream()))) {
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//
//                JSONObject json = new JSONObject(response.toString());
//                // Parse and add additional vulnerability details
//                // Implementation depends on NVD API response structure
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String findSafeVersion(String groupId, String artifactId,
//            List<VulnerabilityInfo> knownVulnerabilities) {
//        try {
//            List<String> availableVersions = getAvailableVersions(groupId, artifactId);
//
//            // Sort versions in descending order
//            Collections.sort(availableVersions, Collections.reverseOrder());
//
//            for (String version : availableVersions) {
//                if (isSafeVersion(groupId, artifactId, version, knownVulnerabilities)) {
//                    return version;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private boolean isSafeVersion(String groupId, String artifactId, String version,
//            List<VulnerabilityInfo> knownVulnerabilities) {
//        List<VulnerabilityInfo> vulnerabilities = checkVulnerabilities(groupId, artifactId, version);
//        return vulnerabilities.stream()
//            .noneMatch(v -> v.getSeverity().equals("HIGH") || v.getSeverity().equals("CRITICAL"));
//    }
//
//    // Helper classes
//    @Data
//    public static class DependencyAnalysis {
//        private String groupId;
//        private String artifactId;
//        private String currentVersion;
//        private String latestVersion;
//        private String suggestedVersion;
//        private boolean needsUpgrade;
//        private List<VulnerabilityInfo> vulnerabilities;
//        private String error;
//    }
//
//    @Data
//    public static class VulnerabilityInfo {
//        private String cve;
//        private String severity;
//        private String description;
//        private String fixedInVersion;
//        private List<String> affectedVersions;
//    }
//
//    // Utility methods
//    private Document loadPomFile(MultipartFile file) throws Exception {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        return builder.parse(file.getInputStream());
//    }
//
//    private String getElementContent(Element parent, String tagName) {
//        NodeList nodes = parent.getElementsByTagName(tagName);
//        if (nodes.getLength() > 0) {
//            return nodes.item(0).getTextContent();
//        }
//        return null;
//    }
//
//    private String getLatestVersion(String groupId, String artifactId) throws IOException {
//        String query = String.format("g:\"%s\" AND a:\"%s\"", groupId, artifactId);
//        URL url = new URL(MAVEN_SEARCH_URL + "?q=" + query + "&rows=1&wt=json");
//
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(url.openStream()))) {
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//
//            JSONObject json = new JSONObject(response.toString());
//            JSONObject docs = json.getJSONObject("response")
//                                .getJSONArray("docs")
//                                .getJSONObject(0);
//            return docs.getString("latestVersion");
//        }
//    }
//
//    private List<String> getAvailableVersions(String groupId, String artifactId) throws IOException {
//        // Similar to getLatestVersion but returns all versions
//        // Implementation depends on Maven repository API
//        return new ArrayList<>();
//    }
//
//    private File createTempPom(String groupId, String artifactId, String version) throws IOException {
//        File tempFile = File.createTempFile("temp-pom", ".xml");
//        try (FileWriter writer = new FileWriter(tempFile)) {
//            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
//                "    <modelVersion>4.0.0</modelVersion>\n" +
//                "    <groupId>temp</groupId>\n" +
//                "    <artifactId>temp</artifactId>\n" +
//                "    <version>1.0</version>\n" +
//                "    <dependencies>\n" +
//                "        <dependency>\n" +
//                "            <groupId>" + groupId + "</groupId>\n" +
//                "            <artifactId>" + artifactId + "</artifactId>\n" +
//                "            <version>" + version + "</version>\n" +
//                "        </dependency>\n" +
//                "    </dependencies>\n" +
//                "</project>");
//        }
//        return tempFile;
//    }
//}
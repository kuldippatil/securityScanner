package com.security.scanner;

public class Main {
    public static void main(String[] args) {

        String artifactPath = "/Users/kuldippatil/Documents/llm/morfeusadmin.war";
        String outputPath = "/Users/kuldippatil/Documents/llm/report_11.csv";

//        DependencyVulnerabilityScanner scanner = new DependencyVulnerabilityScanner();
//        scanner.scan(artifactPath, outputPath);
        
        System.out.println("Scan completed. Report generated at: " + outputPath);
    }
} 
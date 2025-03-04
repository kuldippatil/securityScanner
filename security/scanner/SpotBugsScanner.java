//package com.security.scanner;
//
//import edu.umd.cs.findbugs.BugCollection;
//import edu.umd.cs.findbugs.BugInstance;
//import edu.umd.cs.findbugs.FindBugs2;
//import edu.umd.cs.findbugs.Project;
//import edu.umd.cs.findbugs.DetectorFactoryCollection;
//import edu.umd.cs.findbugs.Priorities;
//import edu.umd.cs.findbugs.SortedBugCollection;
//
//public class SpotBugsScanner {
//
//    public String analyze(String projectPath) {
//        try {
//            Project project = new Project();
//            project.addFile(projectPath);
//
//            FindBugs2 findBugs = new FindBugs2();
//            findBugs.setProject(project);
//
//            // Configure security-focused detectors
//            findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
//
//            BugCollection bugCollection = new SortedBugCollection();
//            findBugs.setBugCollection(bugCollection);
//            findBugs.execute();
//
//            StringBuilder report = new StringBuilder();
//            for (BugInstance bug : bugCollection) {
//                if (bug.getPriority() <= Priorities.NORMAL_PRIORITY) {
//                    report.append(String.format("Priority: %d, Type: %s, Class: %s, Method: %s\n",
//                        bug.getPriority(),
//                        bug.getBugPattern().getType(),
//                        bug.getPrimaryClass().getClassName(),
//                        bug.getPrimaryMethod().getMethodName()));
//                }
//            }
//
//            return report.toString();
//        } catch (Exception e) {
//            return "Error during analysis: " + e.getMessage();
//        }
//    }
//}
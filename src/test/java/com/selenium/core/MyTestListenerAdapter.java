package com.selenium.core;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selenium.helper.GeneralHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.*;

public class MyTestListenerAdapter extends TestListenerAdapter {
    ObjectMapper mapper = new ObjectMapper();
    JSONParser parser = new JSONParser();
    boolean removeRetries = false;
    String markNameToRemove = "remove";
    Path projectDir = Paths.get(System.getProperty("user.dir"));
    HashMap<String,List<String>> retryInfo;
    String retryKey = "retry_%s";

    /***
     * hook run at the end of all tests
     * @param context : test results
     */
    @Override
    public void onFinish(ITestContext context) {
       try {
           if(!removeRetries) {
               return;
           }

           // Initialize list of retry test cases
           retryInfo = initializeRetryInfo();

           // Get json reports
           Path jsonPath = Paths.get(projectDir.toString(), "test-reports", "cucumber-report");
           List<String> cucumberJsonFiles = findJsonFiles(jsonPath);

           // Scan json report files, remove duplicated entries executed by retry mechanism
           for(String jsonFile : cucumberJsonFiles) {
               System.out.println("+++++++++ Start updating cucumber report: " + jsonFile + " +++++++++");
               JSONArray newReportData = cucumberRemoveRetries(jsonFile);

               if(newReportData == null) {
                   System.out.println("+++++++++ Error when updating cucumber report, keep old report! +++++++++");
                   continue;
               }

               // Write updated data into existing json report file
               mapper.writeValue(new File(jsonFile), newReportData);

               System.out.println("+++++++++ Updated cucumber report successfully: " + jsonFile + " +++++++++");
           }

           // Write retry test name executed into flaky-test/file-name
           writeFlakyTestName2File();

           // write retry info (no. of failed test) of retry 1,2... into ./src/test/conf/retry-info.properties
           writeRetryInfo2Properties(retryInfo);

       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    public void writeRetryInfo2Properties(HashMap<String,List<String>> info) {
        try {
            // Write retry info into properties file
            HashMap<String,String> data = new HashMap<>();
            for (HashMap.Entry<String, List<String>> retry : info.entrySet()) {
                String retryCount = retry.getKey();
                List<String> retryList = retry.getValue();

                data.put(retryCount, String.valueOf(retryList.size()));
            }

            GeneralHelper.writeDataIntoPropertyFile(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeFlakyTestName2File() {
        try {
            // Write flaky test into files to keep track
            String flakyDir = projectDir.toString() + "/flaky-tests";
            File dir = new File(flakyDir);
            if(!dir.exists()) {
                dir.mkdir();
            }

            // remove old files, just keep recent files
            int itemsToKeep = 7;
            removeOldFiles(dir.listFiles(),itemsToKeep);
            Path flakyTests = Paths.get(flakyDir,generateRandomStr() + "_flaky_test.txt");

            System.out.println("Write log to: " + flakyTests);

            FileWriter writer = new FileWriter(flakyTests.toString());
            //writer.write("Total number of failures before retry: " + retryInfo.size() + "\n");

            for (HashMap.Entry<String, List<String>> retry : retryInfo.entrySet()) {
                String retryIdx = retry.getKey();

                for(String str : retryInfo.get(retryIdx)) {
                    String tcName = retryIdx + " - " + str;
                    writer.write(tcName + "\n");
                    System.out.println(tcName + "\n");
                }
            }
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void removeOldFiles(File[] lstOfFiles, int itemsToKeep) throws IOException {
        // sort list of files
        Arrays.stream(lstOfFiles).sorted();

        if(lstOfFiles.length > itemsToKeep) {
            for(int i = 0; i < lstOfFiles.length; i++) {
                if(i < lstOfFiles.length - itemsToKeep) {
                    Files.deleteIfExists(Paths.get(lstOfFiles[i].toString()));
                }
            }
        }
    }

    public HashMap<String,List<String>> initializeRetryInfo() {
        HashMap<String,List<String>> retryInfo = new HashMap<>();
        for(int i = 1; i <= RetryAnalyzer.retryLimit; i++) {
            retryInfo.put(String.format(retryKey,i), new ArrayList<String>());
        }

        return retryInfo;
    }

    public String generateRandomStr() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    }

    /***
     * Author: Duy Tu, This function is using to process cucumber json files to exclude duplicated entries generated by failing retry
     * @param jsonReportFile : path of cucumber json report file
     * @return updated json report (remove duplicated entries caused by failed retry
     */
    private JSONArray cucumberRemoveRetries(String jsonReportFile) {
        try {

            // Parse report of thread into an object
            JSONArray threadReport = (JSONArray) parser.parse(new FileReader(jsonReportFile));
            for (Object o : threadReport) {

                // Get feature report (a .feature file)
                JSONObject featureReport = (JSONObject) o;

                // Get all test case in a feature file
                JSONArray elements = (JSONArray) featureReport.get("elements");
                if (elements.size() <= 1) continue;

                for (int i = 0; i < elements.size(); i++) {
                    // Get name of test case
                    String name = (String) ((JSONObject) elements.get(i)).get("name");

                    // Compare with all other test cases in same feature file to see if any duplication
                    int countDuplicatedEntry = (int) elements.stream().filter(e -> ((JSONObject) e).get("name").equals(name)).count();
                    boolean isDuplicated = countDuplicatedEntry > 1;

                    if (isDuplicated) {
                        // If any duplication, mark the name to "remove"
                        String failedTcName = ((JSONObject) elements.get(i)).get("name").toString();

                        // add failed test case with retry count indicator
                        List<String> myListRetry = retryInfo.get(String.format(retryKey,countDuplicatedEntry-1));
                        myListRetry.add(failedTcName);
                        ((JSONObject) elements.get(i)).put("name",markNameToRemove);
                    }
                }

                // Remove all test cases which had been marked as "remove"
                elements.removeIf(e -> ((JSONObject) e).get("name").equals(markNameToRemove));
            }

            return threadReport;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<String> findJsonFiles(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }
        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(f -> f.endsWith("json"))
                    .collect(Collectors.toList());
        }
        return result;
    }

}
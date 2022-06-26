package com.hc.graph_module;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class DistributedCacheFile {
    @Rule public final TemporaryFolder tempFolder = new TemporaryFolder();
    public static void main(String[] args) throws IOException {
        new DistributedCacheFile().mkdirTempFile();
    }
    public void mkdirTempFile() throws IOException {

        final String testString = "Et tu, Brute?";
        final String testName = "testing_caesar";

        final File folder = tempFolder.newFolder();
        final File resultFile = new File(folder, UUID.randomUUID().toString());

        String testPath = resultFile.toString();
        String resultPath = resultFile.toURI().toString();

        File tempFile = new File(testPath);
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(testString);
        }

        System.out.println(testPath+"==========="+resultPath);
    }
}

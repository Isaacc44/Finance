package org.gradle.wrapper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        Path project = Paths.get(System.getProperty("gradle.wrapper.project.dir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
        Path propsPath = project.resolve("gradle/wrapper/gradle-wrapper.properties");
        if (!Files.isRegularFile(propsPath)) throw new FileNotFoundException("Cannot find " + propsPath);
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(propsPath)) { properties.load(in); }
        String distributionUrl = properties.getProperty("distributionUrl");
        if (distributionUrl == null || !distributionUrl.startsWith("https://")) throw new IllegalStateException("A valid HTTPS distributionUrl is required in gradle-wrapper.properties");
        String filename = distributionUrl.substring(distributionUrl.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._-]", "_");
        String distributionName = filename.replace("-bin.zip", "").replace("-all.zip", "");
        Path cache = Paths.get(System.getProperty("user.home"), ".gradle", "wrapper", "dists", "finansflow", distributionName).toAbsolutePath();
        Files.createDirectories(cache);
        Path zip = cache.resolve(filename);
        Path gradleHome = cache.resolve(distributionName);
        if (!Files.isRegularFile(gradleHome.resolve("bin/gradle")) && !Files.isRegularFile(gradleHome.resolve("bin/gradle.bat"))) {
            if (!Files.isRegularFile(zip) || Files.size(zip) == 0) {
                Path temp = cache.resolve(filename + ".download");
                System.out.println("Downloading Gradle distribution: " + distributionUrl);
                try (InputStream in = new URL(distributionUrl).openStream(); OutputStream out = Files.newOutputStream(temp)) {
                    byte[] buffer = new byte[64 * 1024]; int n;
                    while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n);
                } catch (Exception e) { Files.deleteIfExists(temp); throw e; }
                Files.move(temp, zip, StandardCopyOption.REPLACE_EXISTING);
            }
            try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    Path target = cache.resolve(entry.getName()).normalize();
                    if (!target.startsWith(cache)) throw new IOException("Unsafe path in Gradle distribution archive");
                    if (entry.isDirectory()) Files.createDirectories(target);
                    else {
                        Files.createDirectories(target.getParent());
                        try (OutputStream out = Files.newOutputStream(target)) {
                            byte[] buffer = new byte[64 * 1024]; int n;
                            while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n);
                        }
                    }
                    in.closeEntry();
                }
            }
            if (!Files.isRegularFile(gradleHome.resolve("bin/gradle")) && !Files.isRegularFile(gradleHome.resolve("bin/gradle.bat"))) throw new IOException("Gradle archive did not contain expected Gradle executable");
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        Path executable = gradleHome.resolve("bin").resolve(windows ? "gradle.bat" : "gradle");
        if (!windows) executable.toFile().setExecutable(true);
        java.util.List<String> command = new java.util.ArrayList<>();
        if (windows) { command.add("cmd"); command.add("/c"); }
        command.add(executable.toString());
        java.util.Collections.addAll(command, args);
        ProcessBuilder pb = new ProcessBuilder(command).directory(project.toFile()).inheritIO();
        pb.environment().putIfAbsent("JAVA_HOME", System.getProperty("java.home"));
        int exit = pb.start().waitFor();
        System.exit(exit);
    }
}

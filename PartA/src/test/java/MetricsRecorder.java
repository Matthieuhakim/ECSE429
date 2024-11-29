import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class MetricsRecorder {

    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private static final Runtime runtime = Runtime.getRuntime();
    private final FileWriter writer;

    public MetricsRecorder(String fileName) throws IOException {
        writer = new FileWriter(fileName);
        writer.write("operation,numObjects,duration,cpuUsage,memoryUsage\n"); // Write the header
    }

    public void recordMetrics(String operation, int numObjects, long duration) throws IOException {
        double cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        long memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB

        writer.write(String.format("%s,%d,%d,%.2f,%d\n", operation, numObjects, duration, cpuLoad, memoryUsage));
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

}

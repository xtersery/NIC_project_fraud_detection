import java.io.*;
import java.util.*;

public class DatasetLoader {

    public static List<float[]> trainFeatures = new ArrayList<>();
    public static List<float[]> testFeatures = new ArrayList<>();
    public static List<Integer> trainTargets = new ArrayList<>();
    public static List<Integer> testTargets = new ArrayList<>();

    public static int numberOfTrainDataPoints;
    public static int numberOfTestDataPoints;
    public static int numberOfAttributes;
    public static float[][] trainAttributes;
    public static float[][] testAttributes;

    
    public static void loadCSV(String filePath, boolean isFeatures) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            if (isFeatures) {
                float[] featureRow = new float[values.length];
                for (int i = 0; i < values.length; i++) {
                    featureRow[i] = Float.parseFloat(values[i]);
                }
                if (filePath.contains("train")) {
                    trainFeatures.add(featureRow);
                } else {
                    testFeatures.add(featureRow);
                }
            } else {
                if (filePath.contains("train")) {
                    trainTargets.add(Integer.parseInt(values[0]));
                } else {
                    testTargets.add(Integer.parseInt(values[0]));
                }
            }
        }
        br.close();
    }

    public static void standardizeData() {
        numberOfTrainDataPoints = trainFeatures.size();
        numberOfTestDataPoints = testFeatures.size();
        numberOfAttributes = trainFeatures.get(0).length; 

        trainAttributes = new float[numberOfTrainDataPoints][numberOfAttributes];
        testAttributes = new float[numberOfTestDataPoints][numberOfAttributes];
       
        for (int i = 0; i < numberOfTrainDataPoints; i++) {
            trainAttributes[i] = trainFeatures.get(i);
        }

        for (int i = 0; i < numberOfTestDataPoints; i++) {
            testAttributes[i] = testFeatures.get(i);
        }

        System.out.println("Standardization and data splitting complete.");
    }
}

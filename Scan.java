import java.io.File;
import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Scan {

  private Random randGen = new Random();

  private int[][][] pixelData;
  private double[][][] densityData;
  private double[][][] eLossMag;
  private int[][][] cancerData;
  private double[] xyz0;
  private double[] xyz1;
  private double height;
  private double width;
  private double depth;
  private double pixelHeight;
  private double pixelWidth;
  private double sliceThickness;
  private int counter;

  public Scan(double[] position, String directory) {
    this.xyz0 = position;
    this.generate(directory);
    this.counter = 0;
  }

  public int[][][] getPixelData() {return this.pixelData;}
  public double[][][] getDensityData() {return this.densityData;}
  public double[][][] getELossData() {return this.eLossMag;}
  public int[][][] getCancerData() {return this.cancerData;}
  public double[] getXYZ0() {return this.xyz0;}
  public double[] getXYZ1() {return this.xyz1;}
  public double getHeight() {return this.height;}
  public double getWidth() {return this.width;}
  public double getDepth() {return this.depth;}
  public double getPixelHeight() {return this.pixelHeight;}
  public double getPixelWidth() {return this.pixelWidth;}
  public double getSliceThickness() {return this.sliceThickness;}

  public void generate(String directory) {
    File folder = new File(directory);
    File[] files = folder.listFiles();
    this.pixelData = new int[files.length][][];
    this.densityData = new double[files.length][][];
    this.eLossMag = new double[files.length][][];
    this.cancerData = new int[files.length][][];

    for (int y = 0; y < files.length; y++) {
      System.out.println(y);
      File file = files[y];
      DicomReader reader = new DicomReader(file);

      this.pixelHeight = reader.getPixelHeight();
      this.pixelWidth = reader.getPixelWidth();
      this.sliceThickness = reader.getSliceThickness();
      this.height = files.length * this.sliceThickness;
      this.width = this.pixelWidth * reader.getWidth();
      this.depth = this.pixelHeight * reader.getHeight();

      int[][] pixelSlice = new int[reader.getWidth()][reader.getHeight()];
      double[][] slice = new double[reader.getWidth()][reader.getHeight()];
      double[][] eLossSlice = new double[reader.getWidth()][reader.getHeight()];
      int[][] cancerSlice = new int[reader.getWidth()][reader.getHeight()];

      for (int x = 0; x < reader.getWidth(); x++) {
        for (int z = 0; z < reader.getHeight(); z++) {
          pixelSlice[x][(reader.getHeight() - z) - 1] = reader.getRGB(x, z)[0];
          slice[x][(reader.getHeight() - z) - 1] = getDensityFromRGB(reader.getRGB(x, z)[0]);
          eLossSlice[x][z] = 0;
          cancerSlice[x][z] = 0;
        }
      }
      this.pixelData[(files.length - y) - 1] = pixelSlice;
      this.densityData[(files.length - y) - 1] = slice;
      this.eLossMag[y] = eLossSlice;
      this.cancerData[y] = cancerSlice;
    }
  }

  public boolean isInScan(Proton p) {
    double[] position = p.getLastPosition();
    if (position[1] >= this.xyz0[0] && position[2] >= this.xyz0[1] && position[3] >= this.xyz0[2]) {
      if (position[1] <= this.xyz1[0] && position[2] <= this.xyz0[1] && position[3] <= this.xyz0[2]) {
        return true;
      }
    }
    return false;
  }

  public int[] getVolume(Proton p) {
    double[] position = p.getLastPosition();
    int x = (int) Math.floor((position[1] - this.xyz0[0]) / this.pixelWidth);
    int y = (int) Math.floor((position[2] - this.xyz0[1]) / this.sliceThickness);
    int z = (int) Math.floor((position[3] - this.xyz0[2]) / this.pixelHeight);
    int[] index = {x, y, z};
    return index;
  }

  public double getDensity(Proton p) {
    int[] index = this.getVolume(p);
    return densityData[index[1]][index[0]][index[2]];
  }

  public double getDensityFromRGB(int colour) {
    if (colour == 0) {
      return 0.001225;
    }
    double min = 0.9094;
    double max = 1.75;
    return min + (colour / 256);
  }

  public void doELoss(Proton p) {
    EnergyLoss ELoss = new EnergyLoss(new Material(getDensity(p), 6, 12));
    double energyLoss = ELoss.getEnergyLoss(p) * p.getLastDistance();
    int[] index = this.getVolume(p);
    this.eLossMag[index[1]][index[0]][index[2]] += energyLoss;
    p.reduceEnergy(energyLoss);
  }

  public void doMCS(Proton p) {
    MCS multScatter = new MCS(new Material(getDensity(p), 6, 12));
    double theta0 = multScatter.getTheta0(p);
    p.applySmallRotation((theta0 * randGen.nextGaussian()), (theta0 * randGen.nextGaussian()));
  }

  public void findTumour() {
    for (int y = 0; y < pixelData.length; y++) {
      int[][] slice = pixelData[y];
      double average = 0;
      int count = 0;
      for (int x = 0; x < slice.length; x++) {
        for (int z = 0; z < slice.length; z++) {
          if (pixelData[y][x][z] != 0) {
            average += pixelData[y][x][z];
            count++;
          }
        }
      }
      average /= count;
      this.counter = 0;
      //System.out.println("Checking: " + y);
      here: for (int i = 0; i < slice.length; i++) {
        for (int j = 0; j < slice.length; j++) {
          int[] index = {i, y, j};
          if (check(index, average) == true) {
            break here;
          }
        }
      }
    }
  }

  public boolean check(int[] index, double average) {
    int shade = this.pixelData[index[1]][index[0]][index[2]];
    if (shade - average >= 30) {
      //System.out.println("found one");
      this.cancerData[index[1]][index[0]][index[2]]++;
      this.counter++;
      for(int x = index[0] - 1; x <= index[0] +1; x++) {
        for (int z = index[2] - 1; z <= index[2] + 1; z++) {
          if (x < 0 || x >= this.cancerData[0].length) {
            continue;
          } else if (z < 0 || z >= this.cancerData[0].length) {
            continue;
          } else {
            if (this.cancerData[index[1]][x][z] != 0) {
              this.cancerData[index[1]][index[0]][index[2]]++;
            } else {
              int[] nextIndex = {index[1], x, z};
              this.check(nextIndex, average);
              if (this.cancerData[index[1]][x][z] != 0) {
                this.cancerData[index[1]][index[0]][index[2]]++;
              }
            }
          }
        }
      }
    }
    //System.out.println(this.counter);
    if (this.counter >= 1000) {
      System.out.println("Success!");
      return true;
    } else {
      return false;
    }
  }

  public void writeToDisk(String filename) throws IOException {
    FileWriter file = new FileWriter(filename);     // this creates the file with the given name
    PrintWriter outputFile = new PrintWriter(file); // this sends the output to file1
    int[][][] data = this.cancerData;
    int[][] fileData = new int[512][512];
    for (int n = 0; n < 512; n++) {
      for (int m = 0; m < 512; m++) {
        fileData[n][m] = 0;
      }
    }
    for (int i = 0; i < 361; i++) {
      int[][] slice = data[i];
      for (int j = 512 - 1; j >= 0; j--) {
        for (int k = 0; k < 512; k++) {
          fileData[k][j] += slice[k][j];
        }
      }
    }
    for (int z = 512 - 1; z >= 0; z--) {
      for (int x = 0; x < 512; x++) {
        outputFile.print(fileData[x][z] + ",");
      }
      outputFile.println();
    }
    outputFile.close(); // close the output file
  }
}

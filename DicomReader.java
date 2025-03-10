import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.TagFromName;
import com.idrsolutions.image.dicom.DicomDecoder;
import java.io.File;
import java.awt.image.BufferedImage;

public class DicomReader {

  private static AttributeList list;
  private static DicomDecoder decoder = new DicomDecoder();
  private BufferedImage decodedImage;

  public DicomReader(File dicomFile) {
      this.list = new AttributeList();
      //this.decoder = new DicomDecoder();
      try {
        this.list.setDecompressPixelData(false);
        this.list.read(dicomFile);
        this.decodedImage = decoder.read(dicomFile);
      }
      catch(Exception e) {
        System.out.println("Error");
        this.decodedImage = null;
        e.printStackTrace();
      }
  }

  public int getHeight() {return this.decodedImage.getHeight();}

  public int getWidth() {return this.decodedImage.getWidth();}

  public int[] getRGB(int x, int y) {
    int data = this.decodedImage.getRGB(x, y);
    int r = (int) ((Math.pow(256, 3) + data) / 65536);
    int g = (int) (((Math.pow(256, 3) + data) / 256) % 256);
    int b = (int) ((Math.pow(256, 3) + data) % 256);
    int[] rgb = {r, g, b};
    return rgb;
  }

  public double getPixelHeight() {
    String data = getTagInformation(TagFromName.PixelSpacing);
    String[] heightWidth = data.split("\\\\");
    return Double.valueOf(heightWidth[0]);
  }

  public double getPixelWidth() {
    String data = getTagInformation(TagFromName.PixelSpacing);
    String[] heightWidth = data.split("\\\\");
    return Double.valueOf(heightWidth[1]);
  }

  public double getSliceThickness() {
    return Double.valueOf(getTagInformation(TagFromName.SliceThickness));
  }

  private String getTagInformation(AttributeTag attrTag) {
    return Attribute.getDelimitedStringValuesOrEmptyString(this.list, attrTag);
  }

  public String test() {
    System.out.println(getTagInformation(TagFromName.PixelSpacing));
    return "Hello";
  }
}

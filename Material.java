public class Material {

  private double rho;
  private double Z;
  private double A;

  public Material(double rho, double Z, double A) {
    this.rho = rho;
    this.Z = Z;
    this.A = A;
  }

  public double getRho() {return this.rho;}

  public double getZ() {return this.Z;}

  public double getA() {return this.A;}
}

public class Proton {

    public Proton() {

    }

    public double getLastDistance() {return 0.1;}
    public double getCharge() {return 0.1;}
    public double getMass() {return 0.1;}
    public double getLastMomentumMag() {return 0.1;}
    public double[] getLastPosition() {
        double[] position = {0, 0, 0, 0};
        return position;
    }
    public double getBeta() {return 0.1;}
    public double getGamma() {return 0.1;}
    public void reduceEnergy(double energy) {return;}
    public void applySmallRotation(double x, double y) {return;}
}

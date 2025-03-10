public class Geometry {

    private Cuboid[] cuboids;
    private int volumes;
    private Scan scan;

    public Geometry(int maxVolumes) {
        this.cuboids = new Cuboid[maxVolumes];
        this.volumes = 0;
    }

    public void addCuboid(double[] xyz0, double[] xyz1, Material material) {
        Cuboid cuboid = new Cuboid(xyz0, xyz1, material);
        cuboids[volumes] = cuboid;
        volumes++;
    }

    public void addScan(double[] xyz0, String directory) {
        this.scan = new Scan(xyz0, directory);
    }

    public boolean doELoss(Proton p) {
        if (this.scan.isInScan(p)) {
            this.scan.doELoss(p);
            return true;
        } else {
            for (int i = volumes - 1; i >= 0; i++) {
                if (this.cuboids[i].isInVolume(p)) {
                    this.cuboids[i].doELoss(p);
                    return true;
                }
            }
            return false;
        }
    }

    public void doMCS(Proton p) {
        if (this.scan.isInScan(p)) {
            this.scan.doMCS(p);
            return;
        } else {
            for (int i = volumes - 1; i >= 0; i++) {
                if (this.cuboids[i].isInVolume(p)) {
                    this.cuboids[i].doMCS(p);
                    return;
                }
            }
        }
    }
}

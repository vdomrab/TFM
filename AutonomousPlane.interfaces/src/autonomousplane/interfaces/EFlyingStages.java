package autonomousplane.interfaces;

public enum EFlyingStages {
	 	TAKEOFF(0, 1000),
	    CLIMB(1001, 9000),
	    CRUISE(9001, 13000),
	    DESCENT(13000, 1001),
	    LANDING(0, 0);


    private final int altMin;
    private final int altMax;

    EFlyingStages(int altMin, int altMax) {
        this.altMin = altMin;
        this.altMax = altMax;
    }

    public boolean contieneAltura(int altura) {
        return altura >= altMin && altura <= altMax;
    }
}
import java.util.Random;

class NoiseGenerator {
	private int		noiseType;
	private double 	eta;
	private Random 	randomGenerator;
	private static final int NOISETYPE_LINEAR = 	0;
	private static final int NOISETYPE_GAUSSIAN = 	1;

	public NoiseGenerator( double noiseEta ) {
		eta = noiseEta;
		noiseType = NOISETYPE_GAUSSIAN;
		randomGenerator = new Random();
	}

	public void setNoiseType( int type ) {
		noiseType = type;
	}

	public double generateNoise( double t ) {
		if ( noiseType == NOISETYPE_LINEAR ) {
			return linearNoise(t);
		} else if ( noiseType == NOISETYPE_GAUSSIAN ) {
			return gaussianNoise(t);
		}

		return 0.0;
	}

	public double linearNoise( double t ) {
		return randomGenerator.nextDouble() * eta;
	}

	public double gaussianNoise( double t ) {
		return randomGenerator.nextGaussian() * eta;
	}

	public void setEta( double e ) {
		eta = e;
	}
}

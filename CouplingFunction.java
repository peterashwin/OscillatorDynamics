import java.lang.Math;

class CouplingFunction extends IntegratorFunction {
	/**
	 * Initialise the equation with some default parameters
	 */
	double omega, eta;

	public CouplingFunction() {
		super(2);

		omega = eta = 0.0;
		// default is param set 3
		setParams(3);
	}

	public void setParams( int paramSet ) {

		if (paramSet == 1) {
			// default : gives (2,2,2) cycle for N=6
			clearParams();
			addParam(0, 0.31185);
			addParam(0, 0.37096);
			addParam(0, 0.0);
			addParam(0, 0.99008);
			addParam(1, 0.10793);
			addParam(1, 0.58180);
			addParam(1, 0.0);
			addParam(1, -0.14053);
		} 
		else
		if (paramSet == 2) {
			// gives funny cycle for N=6		
			clearParams();
			addParam( 0, 0.31185);
			addParam( 0, 0.39);
			addParam( 0, 0.0);
			addParam( 0, 0.99008);
			addParam( 1, 0.10793);
			addParam( 1, 0.58180);
			addParam( 1, 0.0);
			addParam( 1, -0.14053);
		}
		else
		if( paramSet == 3) {
			// gives (2,1,2) het cycle for N=5 			
			clearParams();
			addParam( 0, -0.9738);
			addParam( 0, -0.18186);
			addParam( 0, 0.0);
			addParam( 0, 0.0);
			addParam( 1, 0.2292);
			addParam( 1, -0.0832118);
			addParam( 1, 0.0);
			addParam( 1, 0.0);
		} 
		else
		if( paramSet == 4) {
			// gives chaos for N=4 		
			clearParams();
			addParam( 0, -0.5*Math.cos(0.1104));
			addParam( 0, -0.5*Math.cos(-0.1104));
			addParam( 0, -0.25*Math.cos(0.1104+0.055855));
			addParam( 0, -0.22*Math.cos(0.1104+0.055855));
			addParam( 1, 0.5*Math.sin(0.1104));
			addParam( 1, 0.5*Math.sin(-0.1104));
			addParam( 1, 0.25*Math.sin(0.1104+0.055855));
			addParam( 1, 0.22*Math.sin(0.1104+0.055855));
		}
		else
		{
			System.out.println("WARNING - not defined global params!");
		}

	}


	/**
	 * Over-ridden evaluate function from IntegratorFunction
	 */

	public double evaluate( double time, double[] inputValues, int currentEquation) {
		/*double[] 	phi = new double[inputValues.length];
		double		gsum2 = 0.0;

		for( int i = 0; i < inputValues.length; i++ ) {
			//The plus doesn't do anything because phi[x] was initialized to 0 by java
			phi[i] += inputValues[currentEquation] - inputValues[i];
		}

		for( int i = 0; i < inputValues.length; i++ ) {
			gsum2 += gPhi(phi[i]);
		}*/
		
		double	gsum = 0.0;
		int 	n = inputValues.length;
		int 	ii;
		int 	radius=getCouplingRadius();
		for( int i = 0; i < n; i++ ) {
			ii=i;
			if (currentEquation>=ii-radius && currentEquation<=ii+radius)
			{
				//System.out.println("AComparing " + currentEquation+ " and "+ii);
				gsum += gPhi(inputValues[currentEquation] - inputValues[ii]);
			}
			else if (currentEquation>=ii+n-radius || currentEquation<=ii-n+radius)
			{
				//System.out.println("BComparing " + currentEquation+ " and "+ii);
				gsum += gPhi(inputValues[currentEquation] - inputValues[ii]);
			}		
		}
	
		/*if(gsum != gsum2)
			System.out.println("Doesnt work!");*/
		return omega + (gsum) / inputValues.length;
	}


	/**
	 * Internal function for calculating the g of Phi
	 */

	public double gPhi( double phi ) {
		double res = 0;
		for(int i = 0; i < getParameterCount(0); i++){
			res += getParam(0, i) * Math.cos((i + 1) * phi);
			res += getParam(1, i) * Math.sin((i + 1) * phi);
		}
		return res;
		/*return  parameters.get("c1") * Math.cos(phi) + parameters.get("c2") * Math.cos(2 * phi) + 
				parameters.get("c3") * Math.cos(3 * phi) + parameters.get("c4") * Math.cos(4 * phi) + 
				parameters.get("s1") * Math.sin(phi) + parameters.get("s2") * Math.sin(2 * phi) + 
				parameters.get("s3") * Math.sin(3 * phi) + parameters.get("s4") * Math.sin(4 * phi);*/
	}
}

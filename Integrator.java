import java.lang.Math;
import java.util.ArrayList;

public class Integrator {
	private double[] 		 currentValues;
	private double[]		 lastOutValues;
	private double			 currentTime;
	private double			 currentSpeed;
	private double			 valueModulus;

	private IntegratorFunction[]	currentFunctions;
	private int			integratorMethod;
	public static final int		RUNGE_KUTTA_4 = 1;


	/**
	 * Initialise a new integrator with the specified type and value modulus.
	 *
	 *
	 * @param integratorType the type of integration to perform.
	 * @param valueModulus setting a value modulus will force the integrator to automatically
	 * 						take the modulus of the value that is any greater than the specified 'valueModulus'.
	 *    					This is intended for oscillatory models that are forced to remain in the bounds [0, 2*PI).
	 */

	Integrator( int integratorType, double valueModulus ) {
		integratorMethod = integratorType;
		this.valueModulus = valueModulus;
		this.currentSpeed = 1000.0;
	}


	/**
	 * Initialises a new integrator with the specified type and no modulus restrictions
	 *
	 * @param integratorType the type of integration to perform
	 *
	 */

	Integrator( int integratorType ) {
			integratorMethod = integratorType;
			valueModulus = 0.0;
	}


	/**
	 * Initialises a new integrator of default type Runge-Kutta 4
	 */

	Integrator( ) {
			integratorMethod = RUNGE_KUTTA_4;
			valueModulus = 0.0;
	}



	/**
	 * Evaluates the integral given to the Integrator through this function's parameters.
	 *
	 * @param initialValues an array of the numerical value at the start of this integration period.
	 * @param functions an array of functions (the same length as initialValues) which represents a function (that can have it's own local parameter set) that applies to the parallel array of values.
	 * @param t the time at the start of this period of integration
	 * @param dt the amount of time that should pass during this integration
	 * @param steps how many steps that should be taken to perform this period of integration
	 * @return the array of values at the end of the given period of numerical integration
	 */

	public synchronized double[] evaluate( double[] initialValues, IntegratorFunction[] functions, double t, double dt, int steps ) {
		currentValues = initialValues;
		currentTime = t;
		currentFunctions = functions;

		for( int i = 0; i < steps; i++ ) {
			if ( integratorMethod == RUNGE_KUTTA_4 ) {
				stepRK4(dt/steps);
			}
		}

		return currentValues;
	}

	private void stepRK4( double dt ) {
		int i = 0;
		int order = currentValues.length;

		double[][] 	k = new double[order][4];
		double[] 	tempValues = new double[order];
		double[] 	outValues = new double[order];
		double  	avSpeed = 0.0;
		double		temp;

		for( i = 0; i < order; i++ ) {
			outValues[i] = ((IntegratorFunction)currentFunctions[i]).evaluate(currentTime, currentValues, i);
			avSpeed+=outValues[i];
		}

		// compute current rms speed relative to group orbit drift
		// at rate avSpeed
		avSpeed = avSpeed / ((double) order);
		currentSpeed = 0.0;
		for( i = 0; i < order; i++ ) {
			temp= (outValues[i]-avSpeed);
			currentSpeed += temp*temp;
		}
		currentSpeed = Math.sqrt(currentSpeed);
		//System.out.println("currentSpeed is "+currentSpeed);

		for( i = 0; i < order; i++ ) {
			k[i][0] = outValues[i];
			tempValues[i] = currentValues[i] + ( (dt / 2) * k[i][0] );
		}

		for( i = 0; i < order; i++ ) {
			outValues[i] = ((IntegratorFunction)currentFunctions[i]).evaluate(currentTime + (dt/2), tempValues, i);
		}

		for( i = 0; i < order; i++ ) {
			k[i][1] = outValues[i];
			tempValues[i] = currentValues[i] + ( (dt / 2) * k[i][1] );
		}

		for( i = 0; i < order; i++ ) {
			outValues[i] = ((IntegratorFunction)currentFunctions[i]).evaluate(currentTime + (dt/2), tempValues, i);
		}

		for( i = 0; i < order; i++ ) {
			k[i][2] = outValues[i]; tempValues[i] = currentValues[i] + ( dt * k[i][2] );
		}

		for( i = 0; i < order; i++ ) {
			outValues[i] = ((IntegratorFunction)currentFunctions[i]).evaluate(currentTime + dt, tempValues, i);
		}

		for( i = 0; i < order; i++ ) {
			k[i][3] = outValues[i];
			currentValues[i] = currentValues[i] + ( (dt/6) * ( k[i][0] + (2 * k[i][1]) + (2 * k[i][2]) + k[i][3] ) );
		}

		if ( (valueModulus) > 0 ) {
			for( i = 0; i < order; i++ ) {
				while( currentValues[i] > valueModulus ) { currentValues[i] -= valueModulus; }
				while( currentValues[i] < 0.0 ) { currentValues[i] += valueModulus; }
			}
		}
		
		lastOutValues = currentValues;
		outValues = null;
		tempValues = null;
		k = null;

		currentTime += dt;
	}

	public double getSpeed() {
		//System.out.println("currentSpeed is " + currentSpeed);
		return currentSpeed;
	}

	public double[] getLastOutValues(){
		return lastOutValues;
	}
}

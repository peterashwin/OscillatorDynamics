import java.util.*;
import java.awt.*;

public class StateClassifier {
	PhaseOscillatorSystem 	phaseOscillatorSystem;
	public String			stateString;
	public String			stateTypeString;
	public int[]			oscillatorGroups;
	public Color[]			oscillatorColours;

	public StateClassifier( PhaseOscillatorSystem pos ) {
		phaseOscillatorSystem = pos;
		System.out.println("Stateclassifier initialized for length " + phaseOscillatorSystem.oscillators.length);
	}

	public void classify( double thres ) {
		double[] currentValues = new double[phaseOscillatorSystem.oscillators.length];

		for( int i = 0; i < phaseOscillatorSystem.oscillators.length; i++ ) {
			currentValues[i] = phaseOscillatorSystem.oscillators[i].phase;
		}


		oscillatorGroups = new int[currentValues.length];
		oscillatorColours = new Color[currentValues.length];

		double minAdvance = thres;
		double minRetard = -thres;

// added 2/1/13
//		int order = currentValues.length;

//		double[][] k = new double[order][4];
//		double[] tempValues = new double[order];
//		double[] outValues = new double[order];


		for( int i = 0; i < currentValues.length; i++ ) {
			double dist = currentValues[i] - phaseOscillatorSystem.orderParameter.theta;
			while( dist < 0.0 ) { dist += (2 * Math.PI); }
			while( dist > (2*Math.PI) ) { dist -= (2 * Math.PI); }

			if ( (dist > minAdvance) && (dist < Math.PI) ) {
				oscillatorGroups[i]=3;
			} else if ( (dist<(2*Math.PI-minRetard)) && (dist>Math.PI)) {
				oscillatorGroups[i]=1;
			} else {
				oscillatorGroups[i]=2;
			}
		}

		//System.out.println(Arrays.toString(oscillatorGroups));

		int[] countup=new int[3];
		for (int i = 0; i<3; i++){
			countup[i]=0;
		}


		for( int i = 0; i < currentValues.length; i++ ) {
			countup[oscillatorGroups[i]-1]++;
		}


		/*
		 * Calculate stateType - ie, cluster numbers [2, 2, 1] for example
		 */

		int[] groupElements = new int[3];

		for( int i = 0; i < currentValues.length; i++ ) {
			groupElements[oscillatorGroups[i]-1]++;
		}

		Arrays.sort(groupElements);
		stateTypeString = Arrays.toString(groupElements);
		stateTypeString = stateTypeString.substring(1, stateTypeString.length()-1);
		stateTypeString = stateTypeString.replaceAll(" ", "");

		/*
		 * Calculate stateString - ie, exact oscillator grouping [1, 1, 2, 3, 3]
		 */
		
		stateString = Arrays.toString(oscillatorGroups);
		stateString = stateString.substring(1, stateString.length()-1);
		stateString = stateString.replaceAll(" ", "");
		
		double[] closeness = new double[currentValues.length];
		double[] stability = new double[currentValues.length];

		for( int i = 0; i < currentValues.length; i++ ) {
			closeness[i] = stability[i] = 1.0;

			for( int j = 0; j < currentValues.length; j++ ) {
				if ( (i != j) && (Math.abs(currentValues[j]-currentValues[i]) < closeness[i] ) ) {
					closeness[i] = Math.abs(currentValues[j]-currentValues[i]);
				}

				if ( i != j ) {
					stability[i] += Math.sin(currentValues[j]-currentValues[i]);
				}
			}

			stability[i] = 0.25 * stability[i];

			if ( stability[i] < 0 ) {
				stability[i] = 0;
			}

			if ( stability[i] > 1 ) {
				stability[i] = 1;
			}
		}

        for( int i = 0; i < currentValues.length; i++ ) {
			double saturation = 1 - closeness[i];
			double hue = stability[i];

			if ( closeness[i] < 0.3 ) {
				saturation = 1.0;
			} else {
				oscillatorColours[i] = Color.GREEN;
				continue;
			}

			double r = (0.4 + hue) + ((1-hue) * Math.min(1-saturation, 1));
			double g = (0.4 + hue) + ((1-hue) * Math.min(1-saturation, 1));
			double b = (1 - hue) + ((hue) * Math.min(1-saturation, 1));

			r = Math.min(r, 1);
			r = Math.max(r, 0);
			g = Math.min(g, 1);
			g = Math.max(g, 0);
			b = Math.min(b, 1);
			b = Math.max(b, 0);

			oscillatorColours[i] = new Color((float)r, (float)g, (float)b);
		}

		for ( int i = 0; i < phaseOscillatorSystem.oscillators.length; i++ ) {
			phaseOscillatorSystem.oscillators[i].color = oscillatorColours[i];
		}
	}
}

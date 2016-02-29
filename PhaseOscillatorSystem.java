import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.awt.*;
import java.awt.geom.*;

// calls routines to sketches network as well
public class PhaseOscillatorSystem implements Runnable {
	private Thread			thread;
	private ArrayList<ParamsChangedListener> eventlistener;
	public Integrator		integrator;
	public PhaseOscillator[] 	oscillators;
	public Complex			orderParameter;
	public NoiseGenerator		noiseGenerator;
	public StateClassifier		stateClassifier;
	public DataOutput		dataOutput;
	public NetworkBuilder		networkBuilder;
	public double			simulationTime;
	public double			speedNow;
	public double			speedWas;
	// This is the value we set to make the system start or stop.
	private boolean			running;			
	// This value tells us whether or not the system is current 
	// doing maths (so we should wait for it to finish)
	private boolean			isExecuting;		
	// This value says whether we should sketch "whole network" in panel
	private boolean			drawOption;
	private boolean			noiseOn;
	private Random			randomGenerator;
	// The amount of time to sleep between integrating the next step. 
	// For slower machines, keep above 1-5.
	public int			sleepTime;			
	public double			noiseStrength; 
	public boolean			maxSpeed;
	public double			tstep;
	// Speed threshold to classify as periodic orbit
	public double			speedThres;
	// Threshold to classify as same cluster
	public double			clusThres;
	public final int 		stepTime = 10;
	public int				nOscs;
	public int 				rOscs;
	

	public PhaseOscillatorSystem() {
		randomGenerator = new Random();
		integrator = new Integrator(Integrator.RUNGE_KUTTA_4, 2*Math.PI);
		eventlistener = new ArrayList<ParamsChangedListener>();
		oscillators = null;
		simulationTime = 0.0;
		//default size of system = 5
		// default radius
		rOscs=2;
		loadSystem(5);
		isExecuting = false;
		noiseGenerator = new NoiseGenerator(1E-4);
		drawOption = true;
		noiseStrength = 0.0;
		running = false;
		thread = new Thread(this);
		thread.start();
		step();
		sleepTime = 2;
		maxSpeed = false;
		tstep = 0.1;
		speedThres = 0.1;
		clusThres = 0.7;
		noiseOn = false;
	}

	public void AddListener(ParamsChangedListener l){
		eventlistener.add(l);
	}
	
	private void FireEvent(){
		for(ParamsChangedListener l : eventlistener){
			l.paramsChanged(this);
		}
	}
	
	public void loadSystem(int nn) {
		// NUMBER OF OSCILLATORS
		nOscs=nn;
		int oscillatorCount = nOscs;
		
		System.out.println("Creating system of " + nOscs + " oscillators");
		System.out.println("with " + rOscs + " neighbours");
		oscillators = new PhaseOscillator[oscillatorCount];
		for( int i = 0; i < oscillatorCount; i++ ) {
			oscillators[i] = new PhaseOscillator((IntegratorFunction)(new CouplingFunction()), randomGenerator.nextDouble() * Math.PI * 2);
			oscillators[i].function.setCouplingRadius( rOscs);
		}
		stateClassifier = new StateClassifier(this);
		dataOutput = new DataOutput(this);
		networkBuilder = new NetworkBuilder(this);
		FireEvent();
	}

	public void setCouplingRadius(int rC ) { 
		for( int i = 0; i < oscillators.length; i++ ) {
			oscillators[i].function.setCouplingRadius( rC);			
		}
		FireEvent();
	}
	
	public int getCouplingRadius() { 
		return	oscillators[0].function.getCouplingRadius();			
	}
	
	public void setParams( int pSet ) { 
		for( int i = 0; i < oscillators.length; i++ ) {
			oscillators[i].function.setParams( pSet);			
		}
		FireEvent();
	}
	
	
	public void setCParam( int index, double value ) {
		for(int i = 0; i < oscillators.length; i++) {
			oscillators[i].function.setParam( 0, index, value );
		}
		FireEvent();
	}
	public void setSParam( int index, double value ) {
		for(int i = 0; i < oscillators.length; i++) {
			oscillators[i].function.setParam( 1, index, value );
		}
		FireEvent();
	}
	public Double getCParam( int index ) {
			return oscillators[0].function.getParam( 0, index );
	}
	public Double getSParam( int index ) {
		return oscillators[0].function.getParam( 1, index);
	}
	public void addParam( double c, double s ) {
		for(int i = 0; i < oscillators.length; i++) {
			oscillators[i].function.addParam( 0, c );
			oscillators[i].function.addParam( 1, s );
		}
		FireEvent();
	}
	public void removeLastParam( ) {
		for(int i = 0; i < oscillators.length; i++) {
			oscillators[i].function.removeLastParam( 0 );
			oscillators[i].function.removeLastParam( 1 );
		}
		FireEvent();
	}
	public void setParameters(ArrayList<Double> c, ArrayList<Double> s){
		for(int i = 0; i < oscillators.length; i++) {
			oscillators[i].function.setParameters( 0, c );
			oscillators[i].function.setParameters( 1, s );
		}
		FireEvent();
	}
	public int getParameterCount(){
		return oscillators[0].function.getParameterCount(0);
	}
	
	public void step() {
		isExecuting = true;
		double[] phases = new double[oscillators.length];
		IntegratorFunction[] functions = new IntegratorFunction[oscillators.length];

		for( int i = 0; i < oscillators.length; i++ ) {
			phases[i] = oscillators[i].phase;
			functions[i] = oscillators[i].function;
		}

		phases = integrator.evaluate( phases, functions, simulationTime, tstep, 10 );
		speedWas = speedNow;
		speedNow = integrator.getSpeed();
		simulationTime += tstep;

		for( int i = 0; i < oscillators.length; i++ ) {
			oscillators[i].phase = phases[i];
		}

		if ( noiseOn ) {
			for( int i = 0; i < oscillators.length; i++ ) {
				oscillators[i].phase += noiseGenerator.generateNoise(simulationTime);
			}
		}

		calculateOrderParameter();

		// only update if we are close to new rel P.O.
		if ( speedWas > speedThres && speedNow <= speedThres) {
			System.out.println("New cluster po");
			if ( stateClassifier != null ) {
				stateClassifier.classify(clusThres);
			}
			if ( networkBuilder != null ) {
				networkBuilder.updateNetwork();
			}
		}

		isExecuting = false;

		if ( dataOutput != null && dataOutput.isRunning()) {
			dataOutput.outputData();
		}

	}


	public void run() {
		long start, delta;
		while( true ) {
			if ( running ) {
				try {
					start = System.currentTimeMillis();
					step();
					delta = System.currentTimeMillis() - start;
					//networkBuilder.updateNetwork();
					//thread.sleep(10);
					/*if(!maxSpeed){
						Thread.sleep(sleepTime);
					}*/
					if ( delta < stepTime ) {
							Thread.sleep(stepTime - delta);
					} else {
						System.out.println("running slowly");
					}
				} catch ( Exception ex ) { }
			} else {
				try {
					Thread.sleep(100);
				} catch ( Exception ex ) { }
			}
		}
	}


	public void startSimulation() {
		running = true;
	}

	public void stopSimulation() {
		//if ( isRunning() ) {
		//	waitUntilExecuted();
		//}

		running = false;
	}

	public void toggleNoise() {
		noiseOn = !noiseOn;
	}

	public boolean isNoiseEnabled(){
		return noiseOn;
	}
	
	public void restartSimulation() {
		ArrayList<Double> c,s;
		for( int i = 0; i < oscillators.length; i++ ) {
			// take old parameter values
			c = oscillators[i].function.getParameters(0);
			s = oscillators[i].function.getParameters(1);
			int rC = oscillators[i].function.getCouplingRadius();
			// put into new system
			oscillators[i] = new PhaseOscillator((IntegratorFunction)(new CouplingFunction()), randomGenerator.nextDouble() * Math.PI * 2);
			oscillators[i].function.setCouplingRadius(rC);
			setParameters(c, s);
			
		}
		simulationTime = 0.0;
	}

	public void perturbSimulation() {
		for( int i = 0; i < oscillators.length; i++ ) {
			oscillators[i].phase = oscillators[i].phase + 5.0e-4 * randomGenerator.nextDouble();
			System.out.println("Perturbed phases");
		}

		//simulationTime = 0.0;
	}

	public boolean isRunning() {
		return running;
	}

	public void waitUntilExecuted() {
		while( isExecuting == true ) { }
	}


	private void calculateOrderParameter() {
		orderParameter = new Complex(0.0,0.0);

		for( int i = 0; i < oscillators.length; i++ ) {
			orderParameter = Complex.add(orderParameter, new Complex(1, oscillators[i].phase));
		}

		while( orderParameter.theta < 0.0 ) { orderParameter.theta += (2 * Math.PI); }
		while( orderParameter.theta > (2*Math.PI) ) { orderParameter.theta -= (2 * Math.PI); }

		orderParameter.r = orderParameter.r / oscillators.length;
	}

	public void setNoiseStrength( double strength ) {
		noiseStrength = strength;
		noiseGenerator.setEta(strength);
		System.out.println("Noise Set to " + strength);
	}
	
	
	public void setRunAtMaxSpeed( boolean max ) {
		maxSpeed = max;
	}
	
	public void switchView() {
		if (drawOption == false) {
			drawOption=true;
		} else {
			drawOption=false;
		}

	}
	
	public void render(Graphics g, int width, int height) {
		if (drawOption == false) {
			renderold(g,width,height);
		} else {
			rendernew(g,width,height);
		}
	}

	public void renderold(Graphics g, int width, int height) {
		boolean originallyRunning = isRunning();

		if ( originallyRunning == true ) {
			stopSimulation();
		}


		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);

		boolean snapToOrderParameter = true;

		int r;
		int baseLineWidth;

		if ( width < height ) {
			r = (int)(width / 2.5);
		} else {
			r = (int)(height / 2.5);
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if ( width < 1000 ) {
			baseLineWidth = 1;
		} else {
			baseLineWidth = 4;
		}

		g2d.setStroke(new BasicStroke(baseLineWidth));
		AffineTransform at = new AffineTransform();
		at.setToTranslation(width/2, height/2);
		g2d.transform(at);

		g2d.setColor(new Color(0.90f, 0.90f, 0.90f));
		g2d.draw(new Ellipse2D.Double(-r, -r, r*2, r*2));

		AffineTransform resetTransform = g2d.getTransform();

		for( int i = 0; i < oscillators.length; i++ ) {
			if ( snapToOrderParameter ) {
				at.setToRotation(-oscillators[i].phase + orderParameter.theta);
			} else {
				at.setToRotation(-oscillators[i].phase);
			}

			g2d.transform(at);
			g2d.setColor(Color.BLACK);
			g2d.draw(new Line2D.Double(0, 0, r, 0));
			at.setToTranslation(r,0.0);
			g2d.transform(at);

			g2d.setColor(new Color(Color.HSBtoRGB((float)i / oscillators.length, 1f, 1f)));
			//g2d.setColor(oscillators[i].color);

			// Start big get smaller
			float size = (r*0.2f) - (((float)i/(float)oscillators.length) * (r*0.1f));
			g2d.fill(new Ellipse2D.Double(-size/2, -size/2, size, size));

			g2d.setColor(Color.BLACK);
			g2d.draw(new Ellipse2D.Double(-size/2, -size/2, size, size));

			g2d.setTransform(resetTransform);
		}

		g2d.setColor(new Color(0.3f, 0.3f, 0.9f));
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.1f));
		g2d.fill(new Ellipse2D.Double(-orderParameter.r * r, -orderParameter.r * r, orderParameter.r*2*r, orderParameter.r*2*r));

		float[] dashPattern = {10, 10};
		g2d.setColor(new Color(0.3f, 0.3f, 0.9f));
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f));
		g2d.setStroke(new BasicStroke(baseLineWidth*2, BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER, 10,dashPattern, 0));
		g2d.draw(new Ellipse2D.Double(-orderParameter.r * r, -orderParameter.r * r, orderParameter.r*2*r, orderParameter.r*2*r));

		if ( snapToOrderParameter ) {
			at.setToRotation(0);
		} else {
			at.setToRotation(-orderParameter.theta);
		}

		g2d.transform(at);
		g2d.setColor(Color.BLACK);
		g2d.draw(new Line2D.Double(0, 0, orderParameter.r*r, 0));

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));

		if ( originallyRunning ) {
			startSimulation();
		}
	}

// this version - new - draws network as well as phases
	public void rendernew(Graphics g, int width, int height) {
		boolean originallyRunning = isRunning();
		double x1,y1,x2,y2,x3,y3,alphai,alphaj,ph,ph2;
		float temp;

		if ( originallyRunning == true ) {
			stopSimulation();
		}


		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);

		boolean snapToOrderParameter = true;

		int r;
		int baseLineWidth;

		if ( width < height ) {
			r = (int)(width / 2.5);
		} else {
			r = (int)(height / 2.5);
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if ( width < 1000 ) {
			baseLineWidth = 1;
		} else {
			baseLineWidth = 4;
		}	
		// circle radius
		double rc=0.8*(double) r;
		// osc radius
		double ro=0.3*(double) r;
		// ball radius
		double rb=0.1*(double) r;


		g2d.setStroke(new BasicStroke(baseLineWidth));
		AffineTransform at = new AffineTransform();
		at.setToTranslation(width/2, height/2);
		g2d.transform(at);

		g2d.setColor(new Color(0.90f, 0.90f, 0.90f));
		g2d.draw(new Ellipse2D.Double(-rc, -rc, rc*2, rc*2));

		for( int i = 0; i < oscillators.length; i++ ) {
			alphai= 2*Math.PI*(double) i /(double) oscillators.length;
			x1=rc*Math.cos(alphai);
			y1=rc*Math.sin(alphai);
			if ( snapToOrderParameter ) {
				ph = -oscillators[i].phase + orderParameter.theta;
			} else {
				ph = -oscillators[i].phase;
			}
			int rC =oscillators[i].function.getCouplingRadius();
			int n=oscillators.length;
			for( int j = i+1; j < n; j++ ) {
				if (j-i<=rC || j-i>=n-rC)
				{
					// sketch connections
					alphaj= 2*Math.PI*(double) j /(double) oscillators.length;
					if ( snapToOrderParameter ) {
						ph2 = -oscillators[j].phase + orderParameter.theta;
					} else {
						ph2 = -oscillators[j].phase;
					}
					// color by phase difference
					temp=(float) ( (Math.cos(ph-ph2)+1.0)/2.0);
					g2d.setColor(new Color(temp, 0.4f *temp, 1.00f-temp ));
					x2=rc*Math.cos(alphaj);
					y2=rc*Math.sin(alphaj);
	
					g2d.setStroke(new BasicStroke(baseLineWidth*3));
					g2d.draw(new Line2D.Double(x1,y1,x2,y2));
					g2d.setStroke(new BasicStroke(baseLineWidth));
				}
			}
				
			if ( snapToOrderParameter ) {
				ph = -oscillators[i].phase + orderParameter.theta;
			} else {
				ph = -oscillators[i].phase;
			}
			//at.setToRotation(ph);
			//g2d.transform(at);

			x3=x1+ro*Math.cos(ph);
			y3=y1+ro*Math.sin(ph);
			g2d.setColor(Color.BLACK);
			g2d.draw(new Line2D.Double(x1,y1,x3,y3));

			// color in sequence
			g2d.setColor(new Color(Color.HSBtoRGB((float)i / oscillators.length, 1f, 1f)));
			// or use specified colors from grouping
			//g2d.setColor(oscillators[i].color);
			
			// Start big get smaller
			float size = (float)(rb - (((float)i/(float)oscillators.length) * (rb*0.3f)));
			g2d.fill(new Ellipse2D.Double(x3-size/2, y3-size/2, size, size));

			g2d.setColor(Color.BLACK);
			g2d.draw(new Ellipse2D.Double(x3-size/2, y3-size/2, size, size));

			//g2d.setTransform(resetTransform);
		}

		g2d.setColor(new Color(0.3f, 0.3f, 0.9f));
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.1f));
		g2d.fill(new Ellipse2D.Double(-orderParameter.r * r, -orderParameter.r * r, orderParameter.r*2*r, orderParameter.r*2*r));

		float[] dashPattern = {10, 10};
		g2d.setColor(new Color(0.3f, 0.3f, 0.9f));
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f));
		g2d.setStroke(new BasicStroke(baseLineWidth*2, BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER, 10,dashPattern, 0));
		g2d.draw(new Ellipse2D.Double(-orderParameter.r * r, -orderParameter.r * r, orderParameter.r*2*r, orderParameter.r*2*r));

		if ( snapToOrderParameter ) {
			at.setToRotation(0);
		} else {
			at.setToRotation(-orderParameter.theta);
		}

		g2d.transform(at);
		g2d.setColor(Color.BLACK);
		g2d.draw(new Line2D.Double(0, 0, orderParameter.r*r, 0));

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));

		if ( originallyRunning ) {
			startSimulation();
		}
	}


}

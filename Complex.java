public class Complex {
	public double r, theta;

	public Complex( double r, double theta ) {
		this.r = r;
		this.theta = theta;
	}

	public Complex( ) {
		r = theta = 0.0;
	}

	public double cartesianX() { return r * Math.cos(theta); }
	public double cartesianY() { return r * Math.sin(theta); }

	public void setCartesian( double x, double y ) {
		r = Math.sqrt((x*x)+(y*y));
		theta = Math.atan2(y,x);
	}


	public static final Complex add( Complex c1, Complex c2 ) {
		Complex result = new Complex();
		result.setCartesian( c1.cartesianX() + c2.cartesianX(), c1.cartesianY() + c2.cartesianY() );
		return result;
	}
}

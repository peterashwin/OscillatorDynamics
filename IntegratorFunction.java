import java.util.*;


public abstract class IntegratorFunction {
	protected ArrayList<Double>[]	params;
	protected int rC;

	public IntegratorFunction(int paramSets) {
		params = new ArrayList[paramSets];
		for(int i = 0; i < paramSets; i++){
			params[i] = new ArrayList<Double>();
		}
	}


	/**
	 * This function must be overridden by a child class. This is the function that performs the mathematical
	 * calculations of the system
	 */
	abstract public double evaluate( double time, double[] inputValues, int currentEquation  );
	
	public int getParameterCount(int set){
		return params[set].size();
	}
	public void addParam(int set, double value){
		params[set].add(value);
	}
	public void setParam(int set, int index, double value){
		params[set].set(index, value);
	}
	public double getParam(int set, int index){
		return params[set].get(index);
	}
	public void setCouplingRadius(int i){
		rC=i;
	}
	public int getCouplingRadius(){
		return rC;
	}
	
	public ArrayList<Double> getParameters(int set){
		return params[set];
	}
	public ArrayList<Double>[] getAllParameters(){
		return params;
	}
	public void setParameters(int set, ArrayList<Double> values){
		//Copy the value list
		params[set] = new ArrayList<Double>(values);
	}
	public void setAllParameters(ArrayList<Double>[] values){
		params = values;
	}
	public void removeLastParam(int set){
		params[set].remove(params[set].size() - 1);
	}
	protected void clearParams(){
		for(ArrayList<Double> al : params){
			al.clear();
		}
	}
	/**
 	 * Override this function to specify multiple parameter sets
 	 * Take a look at CouplingFunction for an example 
 	 */
	public void setParams( int pSet) {
	
	}
}

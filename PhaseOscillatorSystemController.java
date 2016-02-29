import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.io.*;

public class PhaseOscillatorSystemController {
	private PhaseOscillatorSystem phaseOscillatorSystem;
	String pName;


	public PhaseOscillatorSystemController( PhaseOscillatorSystem pos , String progName) {
		phaseOscillatorSystem = pos;
		pName=progName;
	}

	
	public void runScript( String filename ) { // Defunct
		BufferedReader input = null;
		
		try {
			input = new BufferedReader( new FileReader(filename) );
			String line = null;
			
			while ((line = input.readLine()) != null) {
				System.out.println(line);
				processCommand(line);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				if (input != null) {
					input.close();
				}	
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	

	public void processCommand( String command ) {
	    command = command.trim();
		String[] cmd = command.split(" ", 2);		// cmd[0] = Command, cmd[1] = All Parameters.
		String[] params = null;
		
		if ( cmd.length == 2 ) {
			params = cmd[1].split(",");
		}

		if ( params == null ) {
			params = new String[1];
		}
		
		if ( (cmd[0].length() > 0) && (cmd[0].charAt(1) == '%') ) {
			return;
		}

		if ( cmd[0].compareTo("exit") == 0 ) {
			System.exit(0);
		} else
		if ( cmd[0].compareTo("stop") == 0 ) {
			phaseOscillatorSystem.stopSimulation();
		} else
		if ( cmd[0].compareTo("start") == 0 ) {
			phaseOscillatorSystem.startSimulation();
		} else
		if ( cmd[0].compareTo("newtraj") == 0 ) {
			phaseOscillatorSystem.restartSimulation();
		} else
		if ( cmd[0].compareTo("params") == 0 && cmd.length == 2) {
			int n = Integer.parseInt(cmd[1]);
			phaseOscillatorSystem.setParams(n);
		} else
		if ( cmd[0].compareTo("addparam") == 0 ){
			phaseOscillatorSystem.addParam(0d, 0d);
		} else
		if ( cmd[0].compareTo("rmparam") == 0 ){
				phaseOscillatorSystem.removeLastParam();
		}
		else if ( cmd[0].compareTo("loadsystem") == 0 && cmd.length == 2 ) {
			//ArrayList<Double>[] p = phaseOscillatorSystem.oscillators[0].function.getAllParameters();
			ArrayList<Double> p0 = phaseOscillatorSystem.oscillators[0].function.getParameters(0);
			ArrayList<Double> p1 = phaseOscillatorSystem.oscillators[0].function.getParameters(1);
			phaseOscillatorSystem.loadSystem(Integer.parseInt(cmd[1]));
			phaseOscillatorSystem.setParameters(p0, p1);
		} else
		if ( cmd[0].compareTo("perturb") == 0 ) {
			phaseOscillatorSystem.perturbSimulation();
		} else
		if ( cmd[0].compareTo("clear") == 0 ) {
			phaseOscillatorSystem.networkBuilder.resetNetwork();
		} else
		if ( cmd[0].compareTo("switch") == 0 ) {
			phaseOscillatorSystem.switchView();
		} else
		if ( cmd[0].compareTo("setnoise") == 0 ) {
			phaseOscillatorSystem.toggleNoise();
		} 
		if ( cmd[0].compareTo("setradius") == 0 && cmd.length == 2) {
			int n = Integer.parseInt(cmd[1]);
			System.out.println(cmd[0]+cmd[1]);
			phaseOscillatorSystem.setCouplingRadius(n);
		} 
		if ( cmd[0].compareTo("saveparams") == 0 && cmd.length == 2 ) {
			String outFile = cmd[1];
			PrintWriter printWriter = null;
			try {
				printWriter = new PrintWriter(new FileWriter(outFile));
			} catch( Exception ex ) {
				System.out.println("Could not write to file: " + outFile );
			}
			IntegratorFunction f = phaseOscillatorSystem.oscillators[0].function;
			ArrayList<Double> l = f.getParameters(0);
			// pName is Program name!
			printWriter.println("#" + pName);
			Calendar rN= Calendar.getInstance(); 
			String nC = rN.getTime().toString();
			printWriter.println("//Parameters saved on : " + nC);
			int nIs = phaseOscillatorSystem.nOscs;
			int rIs = phaseOscillatorSystem.rOscs;
			printWriter.println("N = " + nIs);
			printWriter.println("R = " + rIs);
			printWriter.println("// parameters of coupling function");
			for( int i = 0; i < l.size(); i++ ){
				printWriter.println("c("+i+") = " + l.get(i));
			}
			l = f.getParameters(1);
			for( int i = 0; i < l.size(); i++ ){
				printWriter.println("s("+i+") = " + l.get(i));
			}
			printWriter.println("// phases");
			for( int i = 0; i < nIs; i++ ){
				printWriter.println("phi("+i+") = " + phaseOscillatorSystem.oscillators[i].phase);
			}
			printWriter.close();
		} else
		if ( cmd[0].compareTo("loadparams") == 0 && cmd.length == 2 ) {
			String fileName = cmd[1];
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException e) {
				System.out.println("Could not find file: " + fileName);
			}
			String line;
			try {
				while( (line = reader.readLine()) != null ){
					//remove spaces, makes regular expressions easier
					line = line.replaceAll("\\s", "");
					
					//Added # as possible comment char
					if(line.length() == 0 || (line.charAt(0) == '#' || line.startsWith("//")))
						continue;
					//If line starts with N= followed by 1 or more numbers
					if(line.matches("N=[0-9]+")){
						String[] str = line.split("( *= *)");
						processCommand("loadsystem " + str[1]);
					}
					//I wanted to get into regular expressions ;)
					else if(line.matches("[a-zA-Z]+\\([0-9]+\\)=-?[0-9]+\\.?[0-9]*")){
						String[] str = line.split("(\\) *= *|\\( *)");
						int index = Integer.parseInt(str[1]);
						double value = Double.parseDouble(str[2]);
						if(str[0].compareTo("s") == 0){
							while(index >= phaseOscillatorSystem.getParameterCount())
								phaseOscillatorSystem.addParam(0, 0);
							phaseOscillatorSystem.setSParam(index, value);
						}else
						if(str[0].compareTo("c") == 0){
							while(index >= phaseOscillatorSystem.getParameterCount())
								phaseOscillatorSystem.addParam(0, 0);
							phaseOscillatorSystem.setCParam(index, value);
						}else
						if(str[0].compareTo("phi") == 0){
							phaseOscillatorSystem.oscillators[Integer.parseInt(str[1])].phase = Double.parseDouble(str[2]);
						}
					}
					else {
						System.out.println("Warning: could not parse line \"" + line + "\"");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
		if ( (cmd[0].compareTo("startorderparametertrace") == 0) && (cmd.length > 1)) {
			phaseOscillatorSystem.dataOutput.startOrderParameterTrace(cmd[1]);
		} else
		if ( cmd[0].compareTo("stoporderparametertrace") == 0 ) {
			phaseOscillatorSystem.dataOutput.stopOrderParameterTrace();
		} else
		if ( (cmd[0].compareTo("startstatetrace") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.startStateTrace(cmd[1]);
		} else
		if ( (cmd[0].compareTo("stopstatetrace") == 0) ) {
			phaseOscillatorSystem.dataOutput.stopStateTrace();
		} else
		if ( (cmd[0].compareTo("startnetworktrace") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.startNetworkTrace(cmd[1]);
		} else
		if ( (cmd[0].compareTo("stopnetworktrace") == 0) ) {
			phaseOscillatorSystem.dataOutput.stopNetworkTrace();
		} else
		if ( (cmd[0].compareTo("savenetworkadjacency") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.exportNetworkAdjacencyMatrix(cmd[1]);
		} else
		if ( (cmd[0].compareTo("savenetworktransition") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.exportTransitionMatrix(cmd[1]);
		} else
		if ( (cmd[0].compareTo("savenetworkprobability") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.exportProbabilityMatrix(cmd[1]);
		} else
		if ( (cmd[0].compareTo("savestatetable") == 0 ) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.exportStateTable(cmd[1]);
		} else
		if ( (cmd[0].compareTo("startphasetrace") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.startPhaseTrace(cmd[1]);
		} else
		if ( (cmd[0].compareTo("stopphasetrace") == 0) ) {
			phaseOscillatorSystem.dataOutput.stopPhaseTrace();
		} else
		if ( (cmd[0].compareTo("startframedump") == 0) && (cmd.length > 1) ) {
			phaseOscillatorSystem.dataOutput.startFrameDump(cmd[1], 2);
		} else
		if ( (cmd[0].compareTo("stopframedump") == 0) ) {
			phaseOscillatorSystem.dataOutput.stopFrameDump();
		} else
		if ( (cmd[0].compareTo("preloadstate") == 0) && (params.length == 2) ) {
			phaseOscillatorSystem.networkBuilder.preloadState( Integer.valueOf(params[0].trim()), params[1].trim() );
		} else 
		if ( (cmd[0].compareTo("preloadstatetable") == 0) && (params.length == 1) ) {
			phaseOscillatorSystem.networkBuilder.preloadStateTable( params[0] );
			System.out.println("State Table Preloaded");
		} else
		if ( (cmd[0].compareTo("maxspeed") == 0 ) && (params.length == 1) ) {
			if ( Integer.valueOf(params[0].trim()).intValue() == 1) {
				phaseOscillatorSystem.setRunAtMaxSpeed(true);
				System.out.println("Running at Max Speed - Will make a single core processor very slow!");
			} else {
				phaseOscillatorSystem.setRunAtMaxSpeed(false);
				System.out.println("Running at regular speed once again.");
			}
		}
		else 
		if ( cmd[0].compareTo("help") == 0 ) {

		}
	}


	public PhaseOscillatorSystem getPhaseOscillatorSystem() {
		return phaseOscillatorSystem;
	}


}

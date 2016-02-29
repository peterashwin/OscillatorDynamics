import java.io.*;
import java.util.Vector;

public class ScriptRunner extends Thread {
	private PhaseOscillatorSystemController systemController;
	private PhaseOscillatorSystem		oscillatorSystem;
	private Vector<String>			commandList;
	public boolean 				finished;
	public double				waitUntil;

	ScriptRunner( PhaseOscillatorSystem pos, PhaseOscillatorSystemController posc, String filename ) {
		System.out.println("Initialising Script Runner");
		oscillatorSystem = pos;
		systemController = posc;
		commandList = new Vector<String>();
		finished = false;
		waitUntil = 0.0;
		
		BufferedReader input = null;
		
		try {
			input = new BufferedReader( new FileReader(filename) );
			String line = null;
			
			while ((line = input.readLine()) != null) {
				commandList.add(line);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			finished = true;
		}
		finally {
			try {
				if (input != null) {
					input.close();
				}	
			}
			catch (IOException ex) {
				ex.printStackTrace();
				finished = true;
			}
		}

		start();
	}

	public void run() {
		while( finished == false ) { 
			if ( commandList.size() > 0 ) {
				if ( (waitUntil == 0) || (waitUntil <= oscillatorSystem.simulationTime) ) {
					waitUntil = 0;
					String s = commandList.get(0);
					commandList.remove(0);
		
					String[] cmd = s.split(" ", 2);		// cmd[0] = Command, cmd[1] = Parameter
						
					if ( cmd[0].compareTo("wait") == 0 ) {
						waitUntil = oscillatorSystem.simulationTime + Double.valueOf(cmd[1].trim()).doubleValue();
					} else {
						systemController.processCommand(s);				
					}
				}
				
				try{ 
					sleep(20);
				} catch( InterruptedException e ) {
					System.out.println("Interrupted Exception caught");
				}
			} else {
				finished = true;
			}
		}
	}    
}

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.awt.*;
import java.awt.image.*;

import javax.imageio.ImageIO;


public class DataOutput {
	private PhaseOscillatorSystem 	phaseOscillatorSystem;
	private PrintWriter		orderParameterTrace;
	private PrintWriter		stateTrace;
	private PrintWriter		networkTrace;
	private PrintWriter		phaseTrace;
	private DecimalFormat	decimalFormat;
	private DecimalFormat 	frameFormat;
	private String			frameDirectory;
	private boolean			outputtingData;
	private boolean 		running;

	private int			lastPrintedNetworkId;
	private int 			lastFrame;
	private int			lastOutputtedFrame;
	private int 			everyNthFrame;
	


	public DataOutput(PhaseOscillatorSystem pos) {
		phaseOscillatorSystem = pos;
		decimalFormat = new DecimalFormat("0.00000");
		frameFormat = new DecimalFormat("00000");
		outputtingData = false;
		lastPrintedNetworkId = -1;
		lastFrame = 0;
		lastOutputtedFrame = 0;
		frameDirectory = null;
	}



	public void startOrderParameterTrace( String outFile ) {
		try {
			orderParameterTrace = new PrintWriter(new FileWriter(outFile));
			running = true;
			System.out.println("Tracing Order Parameter in File: " + outFile);
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
			System.out.println("Error: " + ex.getMessage());
			orderParameterTrace = null;
		}
	}



	public void stopOrderParameterTrace() {
		if(orderParameterTrace != null) {
			System.out.println("No longer outputting Order Parameter Data");
			waitUntilFinished();
			orderParameterTrace.close();
			orderParameterTrace = null;
			running = false;
		}
	}



	public void startStateTrace( String outFile ) {
		try {
			stateTrace = new PrintWriter(new FileWriter(outFile));
			running = true;
			System.out.println("Tracing Current State in File: " + outFile);
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
			stateTrace = null;
		}
	}

	public void stopStateTrace() {
		System.out.println("No longer outputting State Data");
		waitUntilFinished();
		stateTrace.close();
		stateTrace = null;
		running = false;
	}



	public void startNetworkTrace( String outFile ) {
		try {
			networkTrace = new PrintWriter(new FileWriter(outFile));
			running = true;
			System.out.println("Tracing Network in File: " + outFile);
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
			networkTrace = null;
		}
	}

	public void stopNetworkTrace() {
		System.out.println("No longer outputting Network Data");
		networkTrace.close();
		networkTrace = null;
		running = false;
	}



	public void startPhaseTrace( String outFile ) {
		try {
			phaseTrace = new PrintWriter(new FileWriter(outFile));
			running = true;
			System.out.println("Tracing Oscillator Phases in File: " + outFile);

			phaseTrace.print("Time (t)");

			for( int i = 0; i < phaseOscillatorSystem.oscillators.length; i++ ) {
				phaseTrace.print(", Oscillator " + i);
			}

			phaseTrace.println("");

		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
			phaseTrace = null;
		}
	}

	public void stopPhaseTrace() {
		System.out.println("No longer outputting Network Data");
		waitUntilFinished();
		phaseTrace.close();
		phaseTrace = null;
		running = false;
	}


	public void startVideoAnimation() {
		/*Format formats[] = new Format[1];
		formats[0] = new VideoFormat(VideoFormat.CINEPAK);

 	 	FileTypeDescriptor outputType = new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME);
 	 	Processor p = null;

		try {
			p = Manager.createRealizedProcessor(new ProcessorModel(formats,outputType));
 	 	} catch (Exception e) {
			return false;
		}

		DataSource source = p.getDataOutput();
		MediaLocator dest = new MediaLocator("file://foo.mov");
		DataSink filewriter = null;

		try {
			filewriter = Manager.createDataSink(source, dest);
			filewriter.open();
		} catch (Exception e) {
			return false;
		}

		try {
			filewriter.start();
		} catch (IOException e) {
			return false;
		}

		p.start();*/
	}


	public void startFrameDump( String directory, int NthFrames ) {
		frameDirectory = directory;
		lastFrame = 0;
		lastOutputtedFrame = 0;
		everyNthFrame = NthFrames;
		running = true;
	}


	public void stopFrameDump( ) {
		frameDirectory = null;
		running = false;
	}


	public void outputData() {
		outputtingData = true;

		if ( orderParameterTrace != null ) {
			orderParameterTrace.print(decimalFormat.format(phaseOscillatorSystem.simulationTime) + "\t");
			orderParameterTrace.print(decimalFormat.format(phaseOscillatorSystem.orderParameter.r) + "\t");
			orderParameterTrace.println(decimalFormat.format(phaseOscillatorSystem.orderParameter.theta));
		}

		if ( stateTrace != null ) {
			stateTrace.print(decimalFormat.format(phaseOscillatorSystem.simulationTime)  + "\t");
			stateTrace.println(phaseOscillatorSystem.stateClassifier.stateString);
		}

		if ( networkTrace != null ) {
			int currentId = phaseOscillatorSystem.networkBuilder.getCurrentId();

			if ( currentId != lastPrintedNetworkId ) {
				networkTrace.print(decimalFormat.format(phaseOscillatorSystem.simulationTime)  + "\t");
				networkTrace.println(currentId);
				networkTrace.flush();

				lastPrintedNetworkId = currentId;
			}
		}

		if ( phaseTrace != null ) {
			phaseTrace.print(decimalFormat.format(phaseOscillatorSystem.simulationTime));

			for( int i = 0; i < phaseOscillatorSystem.oscillators.length; i++ ) {
				phaseTrace.print(", " + decimalFormat.format(phaseOscillatorSystem.oscillators[i].phase));
			}

			phaseTrace.println("");
		}

		if ( frameDirectory != null ) {
			lastFrame += 1;

			if ( (lastFrame % everyNthFrame) == 0 ) {
				BufferedImage image = new BufferedImage(480, 480, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = image.createGraphics();
				phaseOscillatorSystem.render(g2d, 480, 480);

				try{
					lastOutputtedFrame += 1;
					File file = new File(frameDirectory + "/" + frameFormat.format(lastOutputtedFrame) + ".png");
					ImageIO.write(image, "png", file);
				} catch( Exception e ) { e.printStackTrace(); }
			}
		}

		outputtingData = false;
	}



	public void exportNetworkAdjacencyMatrix( String outFile ) {
		PrintWriter printWriter = null;

		try {
			printWriter = new PrintWriter(new FileWriter(outFile));
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
		}

		if ( printWriter != null ) {
			try {
				for(Entry<String, NetworkNode> entry : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()){
					NetworkNode n = entry.getValue();
					printWriter.print(entry.getKey());


					for(Entry<String, NetworkNode> entry2 : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
						NetworkNode n2 = entry2.getValue();

						if ( n.childNodes.contains(n2) ) {
							printWriter.print("\t1");
						} else {
							printWriter.print("\t0");
						}
					}

					printWriter.println("");
				}


				waitUntilFinished();
				printWriter.close();
				printWriter = null;
				System.out.println("Exporting Adjacency Matrix: " + outFile );
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		
	}

	public void exportTransitionMatrix( String outFile ) {
		PrintWriter printWriter = null;

		try {
			printWriter = new PrintWriter(new FileWriter(outFile));
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
		}

		if ( printWriter != null ) {
			try {
				
				for (Entry<String, NetworkNode> entry : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
					NetworkNode n = entry.getValue();
					printWriter.print(entry.getKey());

					for(Entry<String, NetworkNode> entry2 : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
						NetworkNode n2 = entry2.getValue();

						if ( n.childNodes.contains(n2) ) {
							printWriter.print("\t" + n2.hitCount);
						} else {
							printWriter.print("\t0");
						}
					}

					printWriter.println("");
				}


				waitUntilFinished();
				printWriter.close();
				printWriter = null;
				System.out.println("Exporting Transition Matrix: " + outFile );
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}

	}

	
	public void exportProbabilityMatrix( String outFile ) {
		PrintWriter printWriter = null;

		try {
			printWriter = new PrintWriter(new FileWriter(outFile));
			printWriter.println("Your Adjacency Matrix Sir");
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
		}

		if ( printWriter != null ) {
			try {
				for (Entry<String, NetworkNode> entry : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
					NetworkNode n = entry.getValue();
					printWriter.print(entry.getKey());

					int totalHits = 0;

					for(Entry<String, NetworkNode> entry2 : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
						NetworkNode n2 = entry2.getValue();

						if ( n.childNodes.contains(n2) ) {
							totalHits += n2.hitCount;
						} 
					}
					for(Entry<String, NetworkNode> entry2 : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
						NetworkNode n2 = entry2.getValue();

						if ( n.childNodes.contains(n2) ) {
							printWriter.print("\t" + ((double)n2.hitCount / (double)totalHits));
						} else {
							printWriter.print("\t0");
						}
					}
					
					printWriter.println("");
				}


				waitUntilFinished();
				printWriter.close();
				printWriter = null;
				System.out.println("Exporting Transition Matrix: " + outFile );
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}

	}

	

	public void exportStateTable( String outFile ) {
		PrintWriter printWriter = null;

		try {
			printWriter = new PrintWriter(new FileWriter(outFile));
		} catch( Exception ex ) {
			System.out.println("Could not write to file: " + outFile );
		}

		if ( printWriter != null ) {
			try {
				Map<Integer, NetworkNode> sortedMap = new TreeMap<Integer, NetworkNode>();

				for(Entry<String, NetworkNode> entry : phaseOscillatorSystem.networkBuilder.visitedNodes.entrySet()) {
					NetworkNode n = entry.getValue();
					sortedMap.put(new Integer(n.id), n);
				}

				for(Entry<Integer, NetworkNode> entry : sortedMap.entrySet()) {
					NetworkNode n = entry.getValue(); 
					printWriter.println(n.id + ": " + n.stateString);
				}

				waitUntilFinished();
				printWriter.close();
				printWriter = null;
				System.out.println("Exporting Current State Table: " + outFile );
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	}


	public void waitUntilFinished() {
		while( outputtingData == true ) { }
	}


	public boolean isOutputtingData() {
		return outputtingData;
	}
	
	public boolean isRunning(){
		return running;
	}
}

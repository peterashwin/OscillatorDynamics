import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.Math;

public class NetworkBuilder {
	PhaseOscillatorSystem 		phaseOscillatorSystem;
	String						lastStateString;
	String						currentStateString;
	int							nextId;
	Map<String, NetworkNode>	visitedNodes;

	public NetworkBuilder( PhaseOscillatorSystem pos ) {
		phaseOscillatorSystem = pos;
		visitedNodes = new HashMap<String, NetworkNode>();
		lastStateString = currentStateString = "";
		nextId = 1;
	}

	public void resetNetwork() {
		/*
		 * I got a ConcurrentModificationException, because updateNetwork tried adding a node while
		 * render was iterating through the map. The problem is, that updateNetwork is called
		 * by PhaseOscillatorSystem.step which runs in another thread. ConcurrentHashMap claims
		 * to be fully thread safe, so hopefully this error won't happen anymore.
		 */
		visitedNodes = new ConcurrentHashMap<String, NetworkNode>();
		lastStateString = currentStateString = "";
		nextId = 1;
	}

	public void updateNetwork() {
		String newState = phaseOscillatorSystem.stateClassifier.stateString;

		if ( newState == "" || newState.compareTo(currentStateString) == 0 ) {
			return;
		}


		if ( visitedNodes.containsKey(newState) ) {
			visitedNodes.get(newState).hitCount++;

			if ( !visitedNodes.get(lastStateString).childNodes.contains(visitedNodes.get(newState)) ) {
				visitedNodes.get(lastStateString).childNodes.add(visitedNodes.get(newState));
			}

			lastStateString = currentStateString = newState;
			System.out.println("Revisited Node " + visitedNodes.get(newState).id + " state (" + newState + ")");
			return;
		}

		NetworkNode n = new NetworkNode(nextId++, newState);
		n.hitCount = 1;
		visitedNodes.put(newState, n);

		if ( visitedNodes.containsKey(lastStateString) ) {	// This won't happen the first time round.
			// adds an exit set
			visitedNodes.get(lastStateString).childNodes.add(n);
		}

		lastStateString = currentStateString = newState;
		System.out.println("Added Node " + (nextId-1) + " state (" + newState + ")");
	}

	
	public void preloadState(int id, String state) {
		NetworkNode n = new NetworkNode(id, state);
		nextId++;
		n.hitCount = 0;
		visitedNodes.put(state, n);
	}
	
	
	public void preloadStateTable( String filename ) { 
		BufferedReader input = null;
		
		try {
			input = new BufferedReader( new FileReader(filename) );
			String line = null;
			
			while ((line = input.readLine()) != null) {
				String[] data = line.trim().toLowerCase().split("\t", 2);
				
				if ( data.length == 2 ) {
					preloadState(Integer.valueOf(data[0].trim()), data[1].trim());
				}
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
	

	public int getCurrentId() {
		if ( currentStateString.length() == 0 ) {
			return 0;
		}

		if ( visitedNodes.containsKey(currentStateString) ) {
			return visitedNodes.get(currentStateString).id;
		}

		return 0;
	}
	
	
	public void render(Graphics g, int width, int height) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);

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
			baseLineWidth = 2;
		} else {
			baseLineWidth = 4;
		}

		g2d.setStroke(new BasicStroke(baseLineWidth));
		AffineTransform at = new AffineTransform();
		at.setToTranslation(width/2, height/2);
		g2d.transform(at);

		// light grey
		g2d.setColor(new Color(0.90f, 0.90f, 0.90f));
		g2d.draw(new Ellipse2D.Double(-r, -r, r*2, r*2));

		double singleAngle = (2 * Math.PI) / visitedNodes.size();
		int i = 0;

		for(Entry<String, NetworkNode> entry : visitedNodes.entrySet()) {
			NetworkNode n = entry.getValue();

			double alphai=i*singleAngle; 

			g2d.setColor(Color.BLACK);
			//System.out.println("*** i = " + i + " nextId-1 =" + (nextId-1) + " (getCurrentId-1) =" + (getCurrentId()-1) );
			//System.out.println("n.stateString= " + n.stateString);
			g2d.setColor(Color.GREEN);
			double size = 10.0;
			g2d.fill(new Ellipse2D.Double(r*Math.cos(alphai)-size/2, r*Math.sin(alphai)-size/2, size, size));
			
			g2d.setColor(Color.BLACK);
			g2d.draw(new Ellipse2D.Double(r*Math.cos(alphai)-size/2, r*Math.sin(alphai)-size/2, size, size));
			if(Math.cos(alphai)<0.0) {
				// left hand side of circle - should right justify !! TO DO PROPERLY!
				g2d.drawString(n.stateString,(float) ((1.03*r*Math.cos(alphai)-size/2)-0.2*r), (float) (1.03*r*Math.sin(alphai)-size/2));
			} else{
				// right hand side of circle - left justify
				g2d.drawString(n.stateString,(float) (1.03*r*Math.cos(alphai)-size/2), (float) (1.03*r*Math.sin(alphai)-size/2));
			}

			int j = 0;
		
			// connect the dots 
			for(Entry<String, NetworkNode> entry2 : visitedNodes.entrySet()) {
				NetworkNode n2 = entry2.getValue();

				if ( n.childNodes.contains(n2) && (i != j) ) {
					double alphaj = j * singleAngle;
					double x1=r*Math.cos(alphai);
					double y1=r*Math.sin(alphai);
					double x2=r*Math.cos(alphaj);
					double y2=r*Math.sin(alphaj);

					g2d.draw(new Line2D.Double(x1,y1,x2,y2));
					
					// arrow at end
					double dx=x1-x2;
					double dy=y1-y2;
					double dn=Math.sqrt(dx*dx+dy*dy);
					double x3=x2+0.1*r*(dx+0.2*dy)/dn;
					double y3=y2+0.1*r*(-0.2*dx+dy)/dn;
					double x4=x2+0.1*r*(dx-0.2*dy)/dn;
					double y4=y2+0.1*r*(0.2*dx+dy)/dn;

					g2d.draw(new Line2D.Double( x2, y2, x3, y3 ));
					g2d.draw(new Line2D.Double( x2, y2, x4, y4 ));

				} 
				j++;
			}
			i++;
		}
	}
}

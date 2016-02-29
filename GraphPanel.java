import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class GraphPanel extends JPanel implements ParamsChangedListener{
	PhaseOscillatorSystem phaseOscillatorSystem;
	boolean draggingY;
	boolean draggingGraph;
	float x,y,u,v;
	public float xMin = (float)-Math.PI * 2;
	public float xMax = (float)Math.PI * 2;
	public float yMin = -5;
	public float yMax = 5;
	public float ctsx,ctsy,stcx,stcy;
	DecimalFormat df2;
	ArrayList<ShapeInformation> shapes;
	
	public GraphPanel(PhaseOscillatorSystem system){
		phaseOscillatorSystem = system;
		system.AddListener(this);
		df2 = new DecimalFormat("0.##");
		shapes = new ArrayList<ShapeInformation>();
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}

	@Override
	public void paint(Graphics g){
		render((Graphics2D)g, getWidth(), getHeight());
	}
	
	private void recalculate(int width, int height){
		ctsx = (float)width / (xMax - xMin);
		ctsy = (float)height / (yMax - yMin);
		stcx = (xMax - xMin) / (float)width; 
		stcy = (yMax - yMin) / (float)height;
	}
	
	private void DrawGraph(Graphics2D g, CouplingFunction c, int width, int height){
		g.setColor(Color.BLACK);
		g.drawLine(0, height / 2, width, height / 2);
		g.drawLine(Math.round(-xMin * ctsx), 0, Math.round(-xMin * ctsx), height);

		int xs,ys;
		for(float i = 0; i < xMax; i += 0.5) {
			xs = (int)Math.round((i * Math.PI - xMin) * ctsx);
			ys = Math.round(height / 2);
			g.drawLine(xs, ys-4, xs, ys+4);
			g.drawString(df2.format(i) + "PI", xs + 2, ys - 2);
		}
		for(float i = 0; i > xMin; i -= 0.5) {
			xs = (int)Math.round((i * Math.PI - xMin) * ctsx);
			ys = Math.round(height / 2);
			g.drawLine(xs, ys-4, xs, ys+4);
			g.drawString(df2.format(i) + "PI", xs + 2, ys - 2);
		}
		xs = (int)(-xMin * ctsx);
		for(int i = Math.round(yMin); i < yMax; i++){
			ys = (int)Math.round((-i + yMax) * ctsy);
			g.drawLine(xs - 2, ys, xs + 2, ys);
			g.drawString(df2.format(i), xs + 2, ys - 2);
		}
		int[] x = new int[width];
		int[] y = new int[width];
		for(int i = 0; i < width; i++){
			x[i] = i;
			y[i] = (int) Math.round((yMax - c.gPhi(i * stcx + xMin)) * ctsy);
		}
		g.setColor(Color.RED);
		g.drawPolyline(x, y, x.length);
	}
	
	public void render(Graphics2D g, int width, int height) {
		recalculate(width, height);
		g.setColor(Color.WHITE);
		g.clearRect(0, 0, width, height);
		CouplingFunction c= (CouplingFunction)phaseOscillatorSystem.oscillators[0].function;
		DrawGraph(g, c, width, height);
		ArrayList<ShapeInformation> sl = (ArrayList<ShapeInformation>)shapes.clone();
		for(ShapeInformation s : sl){
			g.setColor(s.color);
			if(s.fill)
				g.fill(s.shape);
			else
				g.draw(s.shape);
		}
	}
	
	public void paramsChanged(PhaseOscillatorSystem src){
		repaint();
	}
	
	@Override
	public void processMouseEvent(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(e.getID() == MouseEvent.MOUSE_PRESSED){
				CouplingFunction cf= (CouplingFunction)phaseOscillatorSystem.oscillators[0].function;
				x = e.getX() * stcx + xMin;
				y = (float)cf.gPhi(x);
				float cy = yMax - ((e.getY()) * stcy);
				if(cy + 0.1 * yMax >= y && cy - 0.1 * yMax <= y){
					//System.out.println("Y: " + y);
					draggingGraph = true;
					ShapeInformation si = new ShapeInformation();
					si.color = Color.ORANGE;
					si.fill = true;
					si.shape = new Ellipse2D.Float(e.getX() - 5, (yMax - y) * ctsy - 5, 10, 10);
					shapes.add(si);
					//Graphics g = getGraphics();
					//g.setColor(Color.ORANGE);
					//g.drawLine(0, 0, getWidth(), 0);
					//g.fillOval(e.getX() - 5, Math.round((yMax - y) * ctsy) - 5, 10, 10);
				}else
					draggingY = true;
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED){
				if(draggingGraph){
					int n = phaseOscillatorSystem.oscillators[0].function.getParameterCount(0);
					float v = (-(e.getY()) * stcy) + yMax;
					float d = ( ((v - y) * n) / ( (float)Math.PI * (((n * n - n) / 3f) + 1) ) );
					
					/*
					*       (v - y) * n
					* pi * (n * (  n - 1 ) + 1)
					*			(  3   3 )
					*
					*/
					double c;
					double s;
					for(int i = 0; i < n; i++) {
						c = phaseOscillatorSystem.getCParam(i);
						c += d * Math.cos((i + 1) * x);
						phaseOscillatorSystem.setCParam(i, c);
					}
					for(int i = 0; i < n; i++) {
						s = phaseOscillatorSystem.getSParam(i);
						s += d * Math.sin((i + 1) * x);
						phaseOscillatorSystem.setSParam(i, s);
					}
					draggingGraph = false;
					shapes.clear();
				} else if(draggingY){
					float w = xMax - xMin;
				
					xMin -= e.getX() * stcx - (x - xMin);
					xMax = w + xMin;
					draggingY = false;
				}
				repaint();
			}
		}
		super.processMouseEvent(e);
	}
	
	@Override
	public void processMouseMotionEvent(MouseEvent e){
		if(draggingGraph){
			ShapeInformation si;
			for(int i = 0; i < shapes.size(); i++){
				si = shapes.get(i);
				if(si.shape instanceof Line2D.Float){
					Line2D.Float l = (Line2D.Float)(si.shape);
					l.setLine((x - xMin) * ctsx, (yMax - y) * ctsy, (x - xMin) * ctsx, e.getY());
					repaint();
					return;
				}
			}
			si = new ShapeInformation();
			si.color = Color.BLUE;
			si.shape = new Line2D.Float((x - xMin) * ctsx, (yMax - y) * ctsy, (x - xMin) * ctsx, e.getY());
			shapes.add(si);
			repaint();
		}
	}
	
	@Override
	public void processMouseWheelEvent(MouseWheelEvent e){
		float x = e.getX() * stcx+xMin;
		if(SwingUtilities.isMiddleMouseButton(e) || e.isControlDown()){
			yMax *= (1.0+0.1*e.getWheelRotation());
			yMin *= (1.0+0.1*e.getWheelRotation());
			
		} else {
			//float d = (x / (xMax - xMin));
			xMax = (float) ((xMax-x) * (1.0+0.1*e.getWheelRotation() )+x);
			xMin = (float) ((xMin-x) * (1.0+0.1*e.getWheelRotation() )+x);
		}
		
		repaint();
		super.processMouseWheelEvent(e);
	}
}

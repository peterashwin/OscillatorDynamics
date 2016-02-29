import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SystemPanel extends JPanel implements ParamsChangedListener, ChangeListener{
	SystemFrame systemFrame;
	PhaseOscillatorSystem phaseOscillatorSystem;	
	ArrayList<JSpinner> c;
	ArrayList<JSpinner> s;
	ArrayList<JSpinner> a;
	ArrayList<JSpinner> p;
	JLabel n;
	DecimalFormat df0;
	DecimalFormat df5;
	boolean listen;

	public SystemPanel( SystemFrame parent, PhaseOscillatorSystem system ) {
		systemFrame = parent;
		phaseOscillatorSystem = system;
		system.AddListener(this);

		setLayout(new GridLayout(0, 1));
		
		createUI();
	}
	
	private void createUI(){
		removeAll();
		int pc = phaseOscillatorSystem.oscillators[0].function.getParameterCount(0);
		c = new ArrayList<JSpinner>();
		s = new ArrayList<JSpinner>();
		a = new ArrayList<JSpinner>();
		p = new ArrayList<JSpinner>();
		
		JPanel container = new JPanel(new GridLayout(0,2));
		
		for( Integer i = 0; i < pc; i++ ) {
			c.add(createSpinner("c" + (i + 1), container));
			
			a.add(createSpinner("A" + (i + 1), container));
		}
		for( Integer i = 0; i < pc; i++ ) {
			s.add(createSpinner("s" + (i + 1), container));
			
			p.add(createSpinner("P" + (i + 1), container));
		}

		JScrollPane jsp = new JScrollPane(container);
		add(jsp);
		df0=new DecimalFormat("0.");
		df5=new DecimalFormat("0.######");
		
		listen = true;
		paramsChanged(phaseOscillatorSystem);
		validate();
	}
	
	private JSpinner createSpinner(String name, JPanel container){
		JPanel jp = new JPanel(new BorderLayout());
		JLabel jl = new JLabel(name + ": ");
		jp.add(jl, BorderLayout.WEST);
		SpinnerModel model  = new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.01);
		JSpinner js = new JSpinner(model);
		js.setEditor(new JSpinner.NumberEditor(js, "0.#####"));
		js.setName(name);
		js.addChangeListener(this);
		jp.add(js);
		container.add(jp);
		return js;
	}
	
	public void paramsChanged(PhaseOscillatorSystem src){
		if ( listen && phaseOscillatorSystem != null ) {
			listen = false;
			ArrayList<Double> l = phaseOscillatorSystem.oscillators[0].function.getParameters(0);
			if(l.size() != c.size()){
				createUI();
				return;
			}
			for (int i = 0 ; i < l.size(); i++) {
				c.get(i).setValue(l.get(i));
			}
			l = phaseOscillatorSystem.oscillators[0].function.getParameters(1);
			for (int i = 0 ; i< l.size(); i++) {
				s.get(i).setValue(l.get(i));
			}
			for(int i = 0; i < a.size(); i++){
				a.get(i).setValue(Math.sqrt((Double)c.get(i).getValue() * (Double)c.get(i).getValue() + (Double)s.get(i).getValue() * (Double)s.get(i).getValue()));
			}
			for(int i = 0; i < p.size(); i++){
				p.get(i).setValue(Math.atan2((Double)s.get(i).getValue(),(Double)c.get(i).getValue()));
			}
			listen = true;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(listen && e.getSource() instanceof JSpinner){
			JSpinner j = (JSpinner)e.getSource();
			String name = j.getName();
			if(name.length() == 2){
				if(name.charAt(0) == 'P' || name.charAt(0) == 'A'){
					int i = Character.digit(name.charAt(1), 10) - 1;
					listen = false;
					c.get(i).setValue((Double)a.get(i).getValue() * Math.cos((Double)p.get(i).getValue()));
					s.get(i).setValue((Double)a.get(i).getValue() * Math.sin((Double)p.get(i).getValue()));
					p.get(i).setValue(Math.atan2((Double)s.get(i).getValue(), (Double)c.get(i).getValue()));
					a.get(i).setValue(Math.sqrt((Double)c.get(i).getValue() * (Double)c.get(i).getValue() + 
							(Double)s.get(i).getValue() * (Double)s.get(i).getValue()));
					phaseOscillatorSystem.setCParam(i, (Double)c.get(i).getValue());
					phaseOscillatorSystem.setSParam(i, (Double)s.get(i).getValue());
					listen = true;
				} else if(name.charAt(0) == 'c' || name.charAt(0) == 's'){
					int i = Character.digit(name.charAt(1), 10) - 1;
					listen = false;
					p.get(i).setValue(Math.atan2((Double)s.get(i).getValue(), (Double)c.get(i).getValue()));
					a.get(i).setValue(Math.sqrt((Double)c.get(i).getValue() * (Double)c.get(i).getValue() + 
							(Double)s.get(i).getValue() * (Double)s.get(i).getValue()));
					phaseOscillatorSystem.setCParam(i, (Double)c.get(i).getValue());
					phaseOscillatorSystem.setSParam(i, (Double)s.get(i).getValue());
					listen = true;
				}
			}
		}
	}
}

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.DecimalFormat;

public class IntegratorControlFrame extends JInternalFrame implements ActionListener, ChangeListener, ParamsChangedListener {
	PhaseOscillatorSystemController phaseOscillatorSystemController;
	PhaseOscillatorSystem phaseOscillatorSystem;
	JSlider		speedSlider;
	JSlider		noiseSlider;
	JSpinner	nSpinner;
	JSpinner	rSpinner;
	JLabel		timeLabel;
	JLabel 		noiseLabel;
	DecimalFormat	df;

	public IntegratorControlFrame(PhaseOscillatorSystemController posc) {
		super("Control Panel", //title
				true, //resizeable
				false, //closable
				true, //maximizeable
				false); //iconifiable
		phaseOscillatorSystemController = posc;
		phaseOscillatorSystem = phaseOscillatorSystemController.getPhaseOscillatorSystem();
		phaseOscillatorSystem.AddListener(this);
		setBorder(BorderFactory.createLineBorder(new Color(0.1f, 0.2f, 0.4f)));
		setSize(150,650);
		setVisible(true);
		setOpaque(false);

		getContentPane().setLayout(new GridLayout(0,1));

		JButton button = new JButton("Start");
		button.setActionCommand("start");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Stop");
		button.setActionCommand("stop");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("New trajectory");
		button.setActionCommand("newtraj");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Clear");
		button.setActionCommand("clear");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Switch view");
		button.setActionCommand("switch");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Params 1 (try N=6)");
		button.setActionCommand("params 1");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Params 2 (try N=5)");
		button.setActionCommand("params 2");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Params 3 (try N=5)");
		button.setActionCommand("params 3");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Params 4 (try N=4)");
		button.setActionCommand("params 4");
		button.addActionListener(this);
		getContentPane().add(button);

		button = new JButton("Nudge");
		button.setActionCommand("perturb");
		button.addActionListener(this);
		getContentPane().add(button);

		JToggleButton tbutton = new JToggleButton("Toggle Noise");
		tbutton.setActionCommand("setnoise");
		tbutton.addActionListener(this);
		getContentPane().add(tbutton);
		
		button = new JButton("Add Parameter");
		button.setActionCommand("addparam");
		button.addActionListener(this);
		getContentPane().add(button);
		
		button = new JButton("Remove Parameter");
		button.setActionCommand("rmparam");
		button.addActionListener(this);
		getContentPane().add(button);

		speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, 50);
		speedSlider.addChangeListener(this);
		speedSlider.setPaintTicks(false);

		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		p.add(new JLabel("Frame Speed"), BorderLayout.NORTH);
		p.add(speedSlider);
		getContentPane().add(p);

		noiseSlider= new JSlider(JSlider.HORIZONTAL, -20, 1, -10);
		noiseSlider.addChangeListener(this);
		noiseSlider.setPaintTicks(false);
		p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		noiseLabel = new JLabel("Noise Level: 1E" + noiseSlider.getValue() + " Off"); 
		p.add(noiseLabel, BorderLayout.NORTH);
		p.add(noiseSlider);
		getContentPane().add(p);
		
		p = new JPanel(new BorderLayout());
		//p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		p.add(new JLabel("N:"), BorderLayout.WEST);
		SpinnerModel model  = new SpinnerNumberModel(0, 0, 150, 1);
		nSpinner = new JSpinner(model);
		nSpinner.setValue(5);
		nSpinner.setName("N");
		nSpinner.setToolTipText("Sets the number of oscillators");
		nSpinner.setEditor(new JSpinner.NumberEditor(nSpinner, "0"));
		nSpinner.addChangeListener(this);
		p.add(nSpinner);
		getContentPane().add(p);

		p = new JPanel(new BorderLayout());
		//p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		p.add(new JLabel("R:"), BorderLayout.WEST);
		SpinnerModel model2  = new SpinnerNumberModel(0, 0, 150, 1);
		rSpinner = new JSpinner(model2);
		rSpinner.setValue(2);
		rSpinner.setName("R");
		rSpinner.setToolTipText("Sets the number of neighbours");
		rSpinner.setEditor(new JSpinner.NumberEditor(rSpinner, "0"));
		rSpinner.addChangeListener(this);
		p.add(rSpinner);
		getContentPane().add(p);
		
		p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		p.add(new JLabel("Time:"), BorderLayout.NORTH);
		timeLabel = new JLabel();
		p.add(timeLabel);
		getContentPane().add(p);

		Timer refreshTimer = new Timer(200, this);
		refreshTimer.start();
		df = new DecimalFormat("0.#");
	}


	public void stateChanged(ChangeEvent ev) {
		if(ev.getSource() instanceof JSpinner){
			JSpinner src = (JSpinner)ev.getSource();
			if(src.getName().compareTo("N") == 0){
				phaseOscillatorSystemController.processCommand((String)("loadsystem " + src.getValue()));
			}
			if(src.getName().compareTo("R") == 0){
				phaseOscillatorSystemController.processCommand((String)("setradius " + src.getValue()));
			}
		} else if(ev.getSource() instanceof JSlider){
			JSlider src = (JSlider)ev.getSource();
			if(src.equals(noiseSlider)){
				if(phaseOscillatorSystem.isNoiseEnabled()){
					noiseLabel.setText("Noise Level: 1E" + noiseSlider.getValue() + " On");
				} else {
					noiseLabel.setText("Noise Level: 1E" + noiseSlider.getValue() + " Off");
				}
				phaseOscillatorSystem.setNoiseStrength(Math.pow(10, noiseSlider.getValue()));
			}
		}
	}



	public void actionPerformed(ActionEvent evt) {
		String ac = evt.getActionCommand();
		if(ac != null && ac.length() > 0) {
			phaseOscillatorSystemController.processCommand( ac );
		}
		if(ac == "setnoise"){
			if(phaseOscillatorSystem.isNoiseEnabled()){
				noiseLabel.setText("Noise Level: 1E" + noiseSlider.getValue() + " On");
			} else {
				noiseLabel.setText("Noise Level: 1E" + noiseSlider.getValue() + " Off");
			}
		}
		update();
		repaint();
	}


	public void update() { 
		timeLabel.setText(""+ df.format(phaseOscillatorSystem.simulationTime));
	}


	@Override
	public void paramsChanged(PhaseOscillatorSystem src) {
		nSpinner.setValue(phaseOscillatorSystem.oscillators.length);
		rSpinner.setValue(phaseOscillatorSystem.getCouplingRadius());
	}

}

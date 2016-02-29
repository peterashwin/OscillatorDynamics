import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/* New version to do local/global coupling - PA, 23rd Nov 2014 */
/*                                                             */
public class ODChim extends JFrame implements ActionListener {
	static String			progName="Exeter Oscillator Dynamics Simulator";
	OscillatorDesktopPane		oscillatorDesktopPane;
	IntegratorControlFrame		integratorControlFrame;
	PhaseOscillatorSystem		phaseOscillatorSystem;
	PhaseOscillatorSystemController phaseOscillatorSystemController;
	OscillatorSimulationFrame	oscillatorSimulationFrame;
	NetworkFrame			networkFrame;
	SystemFrame 			systemFrame;
	GraphFrame 				graphFrame;
	ScriptRunner			scriptRunner;

	/**
	 * This constructor builds a new instance of the Oscillator Dynamics Application Framework.
	 * Note that this function calls the createGUI function in order to create the interface.
	 */

	public ODChim() {
		super(progName);
		phaseOscillatorSystem = new PhaseOscillatorSystem();
		phaseOscillatorSystemController = new PhaseOscillatorSystemController(phaseOscillatorSystem,progName);
		createUserInterface();
		scriptRunner = null;
	}


	/**
	 * This generates the complete user interface for the application, generating instances of the
	 * special panels that we have made for our application.
	 *
	 * @return true if we have successfully generated the GUI, false otherwise.
	 */

	protected boolean createUserInterface() {
		setJMenuBar(createMenu());
		setContentPane(oscillatorDesktopPane = new OscillatorDesktopPane());
		getContentPane().add(integratorControlFrame = new IntegratorControlFrame(phaseOscillatorSystemController));
		getContentPane().add(oscillatorSimulationFrame = new OscillatorSimulationFrame(phaseOscillatorSystem));
		getContentPane().add(networkFrame = new NetworkFrame(phaseOscillatorSystem));
		getContentPane().add(systemFrame = new SystemFrame(phaseOscillatorSystem));
		getContentPane().add(graphFrame = new GraphFrame(phaseOscillatorSystem));
		
		oscillatorSimulationFrame.reshape(150, 0, 400, 400);
		networkFrame.reshape(400, 400, 250, 250);
		systemFrame.reshape(150, 400, 250, 250);
		graphFrame.reshape(550, 0, 450, 400);
		return true;
	}


	/**
	 * Generates and returns the default menu bar for the application. This is where you should
	 * edit the applications menu setup.
	 *
	 * @return the {@link JMenuBar} created for the application.
	 */

    protected JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
		JMenuItem menuItem;

		/*
		 * File Menu
		 */

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menuItem = new JMenuItem("Open Simulation", KeyEvent.VK_O);
        menuItem.setActionCommand("open");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.setActionCommand("exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);


		/*
		 * View Menu
		 */

        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menu);

        menuItem = new JMenuItem("Toggle Integrator Controls", KeyEvent.VK_I);
        menuItem.setActionCommand("toggleIntegratorControlFrame");
        menuItem.addActionListener(this);
        menu.add(menuItem);


	/*
	 * Data Output Menu
	 */

        menu = new JMenu("Data Output");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        menuItem = new JMenuItem("Trace Order Parameter...", KeyEvent.VK_I);
        menuItem.setActionCommand("startOrderParameterTrace");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Stop!", KeyEvent.VK_I);
        menuItem.setActionCommand("stopOrderParameterTrace");
        menuItem.addActionListener(this);
        menu.add(menuItem);
    
        menuItem = new JMenuItem("Save current parameters", KeyEvent.VK_S);
        menuItem.setActionCommand("saveparams");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Load parameters", KeyEvent.VK_S);
        menuItem.setActionCommand("loadparams");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());
        
    /*
     * About Menu
     */
        menu = new JMenu("About");
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        menuItem = new JMenuItem("Credits", KeyEvent.VK_C);
        menuItem.setActionCommand("credits");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        return menuBar;
	}


	/**
	 * Here we handle all of the Graphical Interface actions. Please note that most
	 * functions that actually affect the running of the system itself, simply pass
	 * a text command on to the Oscillator System underneath.
	 */

	public void actionPerformed( ActionEvent event ) {
		System.out.println("Action performed");
		System.out.println(event.getActionCommand());

		if ( event.getActionCommand() == "exit" ) {
			System.exit(0);
		}

		if ( event.getActionCommand() == "toggleIntegratorControlFrame" ) {
			integratorControlFrame.setVisible( !integratorControlFrame.isVisible() );
		}

		if ( event.getActionCommand() == "startOrderParameterTrace" ) {
			FileDialog fd = new FileDialog(this, "Select File to Save Order Parameter Trace...", FileDialog.SAVE);
			fd.setVisible(true);

			if ( fd.getFile() != null ) {
				phaseOscillatorSystemController.processCommand("startorderparametertrace " + fd.getDirectory() + fd.getFile());
			}
		}

		if ( event.getActionCommand() == "stopOrderParameterTrace" ) {
			phaseOscillatorSystemController.processCommand("stoporderparametertrace");
		}
		
		if ( event.getActionCommand() == "saveparams" ) {
			FileDialog fd = new FileDialog(this, "Select File to Save Parameters...", FileDialog.SAVE);
			fd.setVisible(true);

			if ( fd.getFile() != null ) {
				phaseOscillatorSystemController.processCommand("saveparams " + fd.getDirectory() + fd.getFile());
			}
		}
		
		if ( event.getActionCommand() == "loadparams" ) {
			FileDialog fd = new FileDialog(this, "Select File to Load Parameters...", FileDialog.LOAD);
			fd.setVisible(true);

			if ( fd.getFile() != null ) {
				phaseOscillatorSystemController.processCommand("loadparams " + fd.getDirectory() + fd.getFile());
			}
		}
		
		if(event.getActionCommand() == "credits"){
			CreditsFrame credits = new CreditsFrame(progName);
			credits.setVisible(true);
		}
	}


	/**
	 * Our beloved main function that we use to start the program.
	 */

	public static void main( String[] arg ) {
		ODChim od = new ODChim();
		boolean running = true;

	        WindowListener listen = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
       				System.exit(0);
            		}
        };

        od.addWindowListener(listen);
        od.setSize(1000, 700);
       	od.setLocationRelativeTo(null);
        od.setVisible(true);
		
		if ( arg.length == 1 ) {
			od.scriptRunner = new ScriptRunner(od.phaseOscillatorSystem, od.phaseOscillatorSystemController, arg[0]);
		}

        BufferedReader stdin = new BufferedReader(new java.io.InputStreamReader(System.in));

    	while( running == true ) {
			try{
				System.out.print("\n> ");
				String line = stdin.readLine().trim().toLowerCase();
				od.phaseOscillatorSystemController.processCommand(line);
			} catch( IOException e ) {
				System.exit(1);
			}
		}
	}
}

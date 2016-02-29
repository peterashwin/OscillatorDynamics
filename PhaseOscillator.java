import java.awt.*;

public class PhaseOscillator {
	public double 			phase;
	public IntegratorFunction	function;
	public Color			color;
	public int				group;


	/**
	 * Initialises a new Phase Oscillator that can plug directly into a PhaseOscillatorSystem.
	 *
	 * @param func an integrator function that has previously been initialised.
	 * @param initialPhase the starting phase for this oscillator
	 */

	public PhaseOscillator( IntegratorFunction func, double initialPhase ) {
		phase = initialPhase;
		function = func;
		color = Color.YELLOW;
		group = 0;
	}
}

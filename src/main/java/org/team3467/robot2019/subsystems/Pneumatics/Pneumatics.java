
package org.team3467.robot2019.subsystems.Pneumatics;

import edu.wpi.first.wpilibj.command.Subsystem;

import org.team3467.robot2019.subsystems.Pneumatics.Compressor_ReportStatus;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Pneumatics extends Subsystem {

	private Compressor scorpionCompressor;
	private static boolean  handPistonState = false;
	
	// Solenoids
	public DoubleSolenoid cataPiston;
	
	// Pneumatics is a singleton
	private static Pneumatics instance = new Pneumatics();

	public static Pneumatics getInstance() {
		return Pneumatics.instance;
	}

	/*
	 * Pneumatics Class Constructor
	 *
	 * The singleton instance is created statically with
	 * the instance static member variable.
	 */
	protected Pneumatics() {
				
		scorpionCompressor = new Compressor();

		initSolenoids();
		
		scorpionCompressor.start();
		
	}
	
	private void initSolenoids() {
		cataPiston = new DoubleSolenoid(0,1);
		
		cataPiston.set(DoubleSolenoid.Value.kForward);
	}
	
	/*
	 * Custom Pneumatics Helper methods
	 */
		

	public void cataLatch() {
		cataPiston.set(DoubleSolenoid.Value.kReverse);
	}
	public void cataShoot() {
		cataPiston.set(DoubleSolenoid.Value.kForward);
	}
	
	public void operateHands() {
		if (handPistonState) {
			cataLatch();
			handPistonState = false;
		}
		else {
			cataShoot();
			handPistonState = true;
		}
	}
	
	/*
	 * Standard Pneumatics methods	
	 */
	
	public void compressorStop() {
		scorpionCompressor.stop();
	}
	
	public void compressorStart() {
		scorpionCompressor.start();
	}

	// Set up a default command to regularly call reportPressure()
	protected void initDefaultCommand() {
		this.setDefaultCommand(new Compressor_ReportStatus());
	}
}

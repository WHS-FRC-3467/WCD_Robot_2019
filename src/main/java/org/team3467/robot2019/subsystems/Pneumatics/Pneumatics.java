
package org.team3467.robot2019.subsystems.Pneumatics;

import edu.wpi.first.wpilibj.command.Subsystem;

import org.team3467.robot2019.subsystems.Pneumatics.Compressor_ReportStatus;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Pneumatics extends Subsystem {

	private Compressor scorpionCompressor;
	private static boolean  gearShiftPistonState = true;
	private static boolean  handPistonState = false;
	
	// Solenoids
	public DoubleSolenoid gearShift;
	public DoubleSolenoid handsOpen;
	
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
		gearShift = new DoubleSolenoid(0, 1);
		handsOpen = new DoubleSolenoid(2,3);
		
		gearShift.set(DoubleSolenoid.Value.kForward);
		handsOpen.set(DoubleSolenoid.Value.kForward);
	}
	
	/*
	 * Custom Pneumatics Helper methods
	 */
		
	public void shiftUp() {
		gearShift.set(DoubleSolenoid.Value.kForward);
	}
	public void shiftDown() {
		gearShift.set(DoubleSolenoid.Value.kReverse);
	}
	
	public void shiftGears() {
		if (gearShiftPistonState) {
			shiftUp();
			gearShiftPistonState = false;
		}
		else {
			shiftDown();
			gearShiftPistonState = true;
		}
	}

	public void openHands() {
		handsOpen.set(DoubleSolenoid.Value.kReverse);
	}
	public void closeHands() {
		handsOpen.set(DoubleSolenoid.Value.kForward);
	}
	
	public void operateHands() {
		if (handPistonState) {
			openHands();
			handPistonState = false;
		}
		else {
			closeHands();
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

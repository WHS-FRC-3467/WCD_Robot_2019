package org.team3467.robot2019.subsystems.Pneumatics;

import org.team3467.robot2019.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;

public class Compressor_ReportStatus extends Command {

	private int counter;
	
	public Compressor_ReportStatus() {
		requires(Robot.pneumatics);
		this.setInterruptible(true);
	}
	
	protected void initialize() {
		counter = 0;
	}

	protected void execute() {
		if (counter < 50) {
			counter++;
		}
		else {
//			Robot.pneumatics.reportPressure();
			counter = 0;
		}
	}

	protected boolean isFinished() {
		return false;
	}

	protected void end() {
		
	}

	protected void interrupted() {
		end();
	}
}

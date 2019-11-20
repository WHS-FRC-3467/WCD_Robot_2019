package org.team3467.robot2019.subsystems.Pneumatics;

import org.team3467.robot2019.robot.Robot;

import edu.wpi.first.wpilibj.command.InstantCommand;


/**
 *
 */
public class GearShiftToggle extends InstantCommand {

	public GearShiftToggle() {
	}
	
    protected void execute() {
    	Robot.pneumatics.shiftGears();
    }

}

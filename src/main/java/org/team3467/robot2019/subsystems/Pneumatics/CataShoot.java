package org.team3467.robot2019.subsystems.Pneumatics;

import org.team3467.robot2019.robot.Robot;

import edu.wpi.first.wpilibj.command.InstantCommand;


/**
 *
 */
public class CataShoot extends InstantCommand {

	public CataShoot() {
	}
	
    protected void execute() {
    	Robot.pneumatics.cataShoot();
    }
    
}

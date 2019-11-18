package org.team3467.robot2019.robot;

import org.team3467.robot2019.subsystems.Drivetrain.Drivetrain;
import org.team3467.robot2019.subsystems.FieldCamera.FieldCamera;
import org.team3467.robot2019.subsystems.LED.LEDSerial;
import org.team3467.robot2019.subsystems.Limelight.Limelight;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot
{
    public static FieldCamera fieldCamera;

    public static Drivetrain sub_drivetrain;

    // public static Gyro sub_gyro;
    public static LEDSerial sub_led;

    public static OI robot_oi;

    @Override
    public void robotInit()
    {
        sub_led = LEDSerial.getInstance();

        // Start the FieldCamera(s)
        fieldCamera = new FieldCamera();

        sub_drivetrain = Drivetrain.getInstance();
        //sub_gyro = Gyro.getInstance();

        robot_oi = new OI();
    }

    @Override
    public void robotPeriodic()
    {
        publishMatchTime();
    }

    /**
	 * This function is called once each time the robot enters Disabled mode. You
	 * can use it to reset any subsystem information you want to clear when the
	 * robot is disabled.
     */
    @Override
    public void disabledInit()
    {
    
    }

    @Override
    public void disabledPeriodic()
    {

        Scheduler.getInstance().run();
    }

    @Override
    public void autonomousInit()
    {
        // set the limelight vision mode to normal vision
        Limelight.setDriverMode();
    }

    @Override
    public void autonomousPeriodic()
    {
        Scheduler.getInstance().run();
    }

    @Override
    public void teleopInit()
    {
        // Remove any commands letover from prior runs
        Scheduler.getInstance().removeAll();

        // set the limelight vision mode to normal vision
        Limelight.setDriverMode();
    }

    @Override
    public void teleopPeriodic()
    {
        Scheduler.getInstance().run();

    }

    @Override
    public void testPeriodic()
    {
    }

    public void publishMatchTime()
    {
        try
        {
            SmartDashboard.putNumber("Match Time", DriverStation.getInstance().getMatchTime());

        } catch (Exception e)
        {
            SmartDashboard.putNumber("Match Time", -99999);
        }
    }
}

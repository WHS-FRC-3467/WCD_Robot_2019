package org.team3467.robot2019.robot;

import org.team3467.robot2019.robot.Control.XBoxControllerDPad;
import org.team3467.robot2019.robot.Control.XboxController;
import org.team3467.robot2019.robot.Control.XboxControllerButton;
import org.team3467.robot2019.subsystems.Drivetrain.AutoLineup;
import org.team3467.robot2019.subsystems.Drivetrain.DriveBot;
import org.team3467.robot2019.subsystems.Pneumatics.CataLatch;
import org.team3467.robot2019.subsystems.Pneumatics.CataShoot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;

public class OI
{

    private static XboxController driverController;
    private static XboxController operatorController;

    public OI()
    {
        init();
    }

    public void init()
    {
        driverController = new XboxController(0);
        operatorController = new XboxController(1);

        bindControllerCommands();
    }

    @SuppressWarnings("resource")
    public void bindControllerCommands()
    {

        /*
         *
         * Drive Controller
         * 
         */

        // DPad will change drive control mode
        new XBoxControllerDPad(driverController, XboxController.DPad.kDPadUp).whenActive(new DriveBot(DriveBot.driveMode_Tank, false));
        new XBoxControllerDPad(driverController, XboxController.DPad.kDPadDown).whenActive(new DriveBot(DriveBot.driveMode_SplitArcade, false));

        // The "A" Button will shift any drive mode to Precision mode
        new XboxControllerButton(driverController, XboxController.Button.kA).whenActive(new DriveBot(DriveBot.driveMode_SplitArcade, true));

        // The "X" button activates "turn in place" while held down
        new XboxControllerButton(driverController, XboxController.Button.kX).whileActive(new DriveBot(DriveBot.driveMode_RocketSpin, false));

        // Do Auto Lineup and move toward hatch or cargo markings using LimeLight tracking
        // new XBoxControllerDPad(driverController,XboxController.DPad.kDPadLeft).whileActive(new AutoLineup());
        new XboxControllerButton(driverController, XboxController.Button.kStickLeft).whileActive(new AutoLineup());

        /*
		 * 
		 * Operator Controller
		 * 
		 */

         // Dpad up shoots ball with catapult
         new XBoxControllerDPad(operatorController, XboxController.DPad.kDPadUp).whenActive(new CataShoot());

         // Dpad down latches catapult
         new XBoxControllerDPad(operatorController, XboxController.DPad.kDPadDown).whenActive(new CataLatch());
    }

    //
    // Easier access to XBox controllers
    //
    public static XboxController getDriverController()
    {
        return driverController;
    }

    public static XboxController getOperatorController()
    {
        return operatorController;
    }

    public static double getDriverLeftX()
    {
        return driverController.getX(Hand.kLeft);
    }

    public static double getDriverLeftY()
    {
        return driverController.getY(Hand.kLeft);
    }

    public static double getDriverRightY()
    {
        return driverController.getY(Hand.kRight);
    }

    public static double getDriverLeftTrigger()
    {
        return driverController.getTriggerAxis(Hand.kRight);
    }

    public static double getDriverRightTrigger()
    {
        return driverController.getTriggerAxis(Hand.kLeft);
    }

    public static double getOperatorLeftX()
    {
        return operatorController.getX(Hand.kLeft);
    }

    public static double getOperatorLeftY()
    {
        return operatorController.getY(Hand.kLeft);
    }

    public static double getOperatorRightX()
    {
        return operatorController.getX(Hand.kRight);
    }

    public static double getOperatorRightY()
    {
        return operatorController.getY(Hand.kRight);
    }

    public static boolean getOperatorButtonA()
    {
        return operatorController.getAButtonPressed();
    }

    public static boolean getOperatorButtonBack()
    {
        return operatorController.getBackButtonPressed();
    }

    public static double getOperatorLeftTrigger()
    {
        return operatorController.getTriggerAxis(Hand.kRight);
    }

    public static double getOperatorRightTrigger()
    {
        return operatorController.getTriggerAxis(Hand.kLeft);
    }

    public static void setDriverRumble(final boolean rumbleOn)
    {
        driverController.setRumble(GenericHID.RumbleType.kLeftRumble, rumbleOn ? 1 : 0);
        driverController.setRumble(GenericHID.RumbleType.kRightRumble, rumbleOn ? 1 : 0);
    }

    public static void setOperatorRumble(final boolean rumbleOn)
    {
        operatorController.setRumble(GenericHID.RumbleType.kLeftRumble, rumbleOn ? 1 : 0);
        operatorController.setRumble(GenericHID.RumbleType.kRightRumble, rumbleOn ? 1 : 0);
    }

    public static void setOperatorRumble(final double rumbleValue)
    {
        operatorController.setRumble(GenericHID.RumbleType.kLeftRumble, rumbleValue);
        operatorController.setRumble(GenericHID.RumbleType.kRightRumble, rumbleValue);
    }

}

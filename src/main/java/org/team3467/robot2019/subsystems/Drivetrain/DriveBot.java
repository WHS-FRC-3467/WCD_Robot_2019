package org.team3467.robot2019.subsystems.Drivetrain;

import org.team3467.robot2019.robot.OI;
import org.team3467.robot2019.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;




public class DriveBot extends Command
{
    /**
     * Input adjustment switches
     */
    // Don't adjust the sticks at all
    static final boolean NO_STICK_ADJUSTMENT = false;
    // square the inputs (while preserving the sign) to increase fine control while permitting full power
    static final boolean SQUARE_INPUTS = true;
    // Limit the slew rate (rate of control signal change)
    static final boolean SLEW_LIMITING = true;
    // Set up "deadband" around center (off) stick position
    static final boolean USE_DEADBAND = true;

    // Deadband limit
    static final double deadband_limit = 0.02;

    // Scale factor for reducing inputs during Precision Mode
    static final double precision_scaleFactor = 0.5;

    // Drive interface mode
    public static final int driveMode_Tank = 0;
    public static final int driveMode_SplitArcade = 1;
    public static final int driveMode_RocketSpin = 2;

    private static final String[] driveModeNames =
    {
            // Driving control configuration modes
            "Tank", //
            "Rocket League", //
            "Rocket League Spin" //
    };

    // Default drive mode to Rocket drive
    int m_driveMode = driveMode_SplitArcade;

    // Precision mode scales control inputs to allow for finer control
    // Off by default
    boolean m_precision = false;

    // Save last inputs (for slew rate limiting)
    double m_lastCtrlLeftX = 0.0;
    double m_lastCtrlLeftY = 0.0;
    double m_lastCtrlRightX = 0.0;
    double m_lastCtrlRightY = 0.0;
    double m_lastCtrlLeftTrig = 0.0;
    double m_lastCtrlRightTrig = 0.0;

    /**
     * Constructor - no args
     */
    public DriveBot()
    {
        super(Robot.sub_drivetrain);

        m_driveMode = driveMode_SplitArcade; // Default mode
        m_precision = false;
    }

    /**
     * Constructor
     * 
     * @param driveMode     - (int) drive control mode
     * @param precisionMode - (boolean) Scale inputs down for finer precision
     */
    public DriveBot(int driveMode, boolean precisionMode)
    {
        super(Robot.sub_drivetrain);

        m_precision = precisionMode;
        if (m_precision == false)
        {
            // Only change driveMode if not in Precision mode
            // (i.e. Precision mode inherits the current driveMode)
            m_driveMode = driveMode;
        }
    }

    protected void initialize()
    {

        // Wait until here (when this command is actually underway) to display drive mode or update parameters
        // Otherwise, all the instances of this command instantiated in OI will change the values prematurely

        // Check to see if we are in "Demo Mode" (slow driving)
        if (Robot.sub_drivetrain.isDemoMode())
        {

            // Default to Tank Drive
            m_driveMode = driveMode_Tank;

            // Lower Max Speed to a maximum of 0.5 (if it is already lower, it will leave it at that)
            if (Robot.sub_drivetrain.getMaxSpeed() > 0.5)
                Robot.sub_drivetrain.setMaxSpeed(0.5);
        }

        // update max speed scale factor for drivetrain from value on Shuffleboard
        Robot.sub_drivetrain.setMaxSpeedScaleFactor();

        // Update Drive Mode Shuffleboard widget
        Robot.sub_drivetrain.reportDriveControlMode(driveModeNames[m_driveMode]);
    }

    protected void execute()
    {
        switch (m_driveMode)
        {

        default:
        case driveMode_Tank:
            Robot.sub_drivetrain.driveTank((-1.0) * getControllerLeftStickY(), (-1.0) * getControllerRightStickY());
            break;

        case driveMode_SplitArcade:
            //split arcade drive mode
            double speed2 = -OI.getDriverController().getRawAxis(1) * 0.6;
            double curve2 = OI.getDriverController().getRawAxis(5) * 0.3;
            double left = speed2 + curve2;
            double right = -(speed2 - curve2);
            Robot.sub_drivetrain.getLeftTalon().set(left);
            Robot.sub_drivetrain.getRightTalon().set(right);
            if (speed2 == 0.0){
                Robot.sub_drivetrain.getLeftTalon().set(curve2);
                Robot.sub_drivetrain.getRightTalon().set(curve2);
            }
            Robot.sub_drivetrain.drive(speed2, curve2, (m_driveMode == driveMode_SplitArcade));

            Robot.sub_drivetrain.reportEncoders();
            break;
        
        case driveMode_RocketSpin:

            double speed = 0.0;

            double backSpeed = getControllerLeftTrigger();
            double fwdSpeed = getControllerRightTrigger();
            double curve = getControllerLeftStickX();

            curve = (-1.0) * curve;

            if (backSpeed != 0.0 && fwdSpeed != 0.0)
            {
                speed = 0.0;
            }
            else if (fwdSpeed > 0.0)
            {
                speed = fwdSpeed;
            }
            else if (backSpeed > 0.0)
            {
                speed = (-1.0) * backSpeed;
            }

            Robot.sub_drivetrain.drive(speed, curve, (m_driveMode == driveMode_RocketSpin));

            Robot.sub_drivetrain.reportEncoders();
            break;
        }
    }

    protected boolean isFinished()
    {
        return false;
    }

    protected void end()
    {
    }

    protected void interrupted()
    {
        end();
    }

    private double getControllerLeftStickX()
    {
        return (m_lastCtrlLeftX = adjustStick(OI.getDriverLeftX(), m_lastCtrlLeftX, 0.20));
    }

    private double getControllerLeftStickY()
    {
        return (m_lastCtrlLeftY = adjustStick(OI.getDriverLeftY() * (-1.0), m_lastCtrlLeftY, 0.20));
    }

    private double getControllerRightStickY()
    {
        return (m_lastCtrlRightY = adjustStick(OI.getDriverRightY() * (-1.0), m_lastCtrlRightY, 0.20));
    }

    private double getControllerLeftTrigger()
    {
        return (m_lastCtrlLeftTrig = adjustStick(OI.getDriverLeftTrigger(), m_lastCtrlLeftTrig, 0.20));
    }

    private double getControllerRightTrigger()
    {
        return (m_lastCtrlRightTrig = adjustStick(OI.getDriverRightTrigger(), m_lastCtrlRightTrig, 0.20));
    }

    private double adjustStick(double input, double lastVal, double changeLimit)
    {
        double val = input;
        double change;

        if (NO_STICK_ADJUSTMENT)
        {
            return input;
        }

        /*
         * Deadband limit
         */
        if (USE_DEADBAND)
        {
            if ((val > (-1.0 * deadband_limit)) && (val < deadband_limit))
            {
                // Just return from here
                return (0.0);
            }
        }

        /*
         * Square the inputs (while preserving the sign) to increase fine control while permitting full power
         */
        if (SQUARE_INPUTS)
        {
            if (val > 0.0)
                val = (val * val);
            else
                val = -(val * val);
        }

        /*
         * Slew rate limiter - limit rate of change
         */
        if (SLEW_LIMITING)
        {
            change = val - lastVal;

            if (change > changeLimit)
                change = changeLimit;
            else if (change < -changeLimit)
                change = -changeLimit;

            val = lastVal += change;
        }

        /*
         * Precision mode scaling - do this last
         */
        if (m_precision)
        {
            val = val * precision_scaleFactor;
        }

        return val;
    }
}

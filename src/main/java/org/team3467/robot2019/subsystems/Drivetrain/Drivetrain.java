package org.team3467.robot2019.subsystems.Drivetrain;

import java.util.Map;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import org.team3467.robot2019.robot.RobotGlobal;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;

public class Drivetrain extends Subsystem
{

    // Motor controller objects and RobotDrive object
    private final WPI_TalonSRX left_talon, right_talon;
    private final WPI_VictorSPX left_victor_1, right_victor_1;

    private final DifferentialDrive m_drive;
    private ControlMode m_talonControlMode;

    // Static subsystem reference
    private static Drivetrain dTInstance = new Drivetrain();

    public static Drivetrain getInstance()
    {
        return Drivetrain.dTInstance;
    }

    // Drivetrain class constructor
    protected Drivetrain()
    {

        super();

        // Setup tabs and data on the Shuffleboard
        setupShuffleboard();

        // Three motors per side -> three speed controllers per side
        left_victor_1 = new WPI_VictorSPX(RobotGlobal.DRIVEBASE_VICTOR_L1);
        left_talon = new WPI_TalonSRX(RobotGlobal.DRIVEBASE_TALON_L);
        right_victor_1 = new WPI_VictorSPX(RobotGlobal.DRIVEBASE_VICTOR_R1);
        right_talon = new WPI_TalonSRX(RobotGlobal.DRIVEBASE_TALON_R);

        left_victor_1.configFactoryDefault();
        left_talon.configFactoryDefault();
        right_victor_1.configFactoryDefault();
        right_talon.configFactoryDefault();

        // Slave the extra Talons on each side
        left_victor_1.follow(left_talon);
        right_victor_1.follow(right_talon);
		
		// Flip any sensors?
		left_talon.setSensorPhase(true);
		
		// Invert all motors? (invert for driving backward)
		boolean _inverted = false; 
		left_talon.setInverted(_inverted);
		left_victor_1.setInverted(_inverted);
		right_talon.setInverted(_inverted);
		right_victor_1.setInverted(_inverted);

		// Turn off Brake mode
		setTalonBrakes(false);
		
		// Set default control Modes for Master Talons
		setControlMode(ControlMode.PercentOutput);
		
 		// Set encoders as feedback device
		left_talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		right_talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		
		// Instantiate DifferentialDrive
		m_drive = new DifferentialDrive(left_talon, right_talon);
		
        // DifferentialDrive Parameters
        m_drive.setDeadband(0.0); // we will add our own deadband as needed
		m_drive.setSafetyEnabled(true);
		m_drive.setExpiration(1.0);
		m_drive.setMaxOutput(1.0);

    }

    /* Setup Shuffleboard and NetworkTableEntries for displaying and changing data */
    /* NTE's are used to reference Shuffleboard tiles when you need to read or update them */
    private NetworkTableEntry nte_maxSpeed;
    private NetworkTableEntry nte_demoMode;
    private NetworkTableEntry nte_leftEncoderVal;
    private NetworkTableEntry nte_rightEncoderVal;
    private NetworkTableEntry nte_talonBrakes;
    private NetworkTableEntry nte_driveMode;
    private NetworkTableEntry nte_talonMode;

    private void setupShuffleboard()
    {

        // Add a 'Max Speed' widget to a tab named 'DriveConfig', using a number slider
        // The widget will be placed in the first column and second row and will be two columns wide
        // It will be a Number Slider control with range 0.0 -> 1.0
        SimpleWidget speedWidget = Shuffleboard.getTab(RobotGlobal.SBT_DRIVECONFIG_TAB).add("Max Speed", 1).withWidget(BuiltInWidgets.kNumberSlider)
                .withProperties(Map.of("min", 0, "max", 1)).withPosition(1, 0).withSize(2, 1);
        nte_maxSpeed = speedWidget.getEntry();

        // Add a 'Demo Mode' widget to a tab named 'DriveConfig', using a toggle switch
        // The widget will be placed in the first column and third row and will be two columns wide
        SimpleWidget demoModeWidget = Shuffleboard.getTab(RobotGlobal.SBT_DRIVECONFIG_TAB).add("Demo Mode", false).withWidget(BuiltInWidgets.kToggleSwitch)
                .withPosition(1, 1).withSize(2, 1);
        nte_demoMode = demoModeWidget.getEntry();

        // Make a 'Drivetrain' list on the "Sensors" tab
        ShuffleboardTab sensorsTab = Shuffleboard.getTab(RobotGlobal.SBT_SENSORS_TAB);
        ShuffleboardLayout driveTrainList = sensorsTab.getLayout("Drivetrain", BuiltInLayouts.kList).withPosition(0, 0).withSize(1, 4);

        // Add the encoder values to the "Drivetrain" list
        nte_leftEncoderVal = driveTrainList.add("Left Encoder", 0).getEntry();
        nte_rightEncoderVal = driveTrainList.add("Right Encoder", 0).getEntry();

        // Add the Talon Brakes indicator to the "Drivetrain" list
        nte_talonBrakes = driveTrainList.add("Talon Brakes", false).getEntry();

        // Add the DriveMode indicator to the "Drivetrain" list
        nte_driveMode = driveTrainList.add("Drive Mode", "---").getEntry();

        // Add the Talon ControlMode indicator to the "Drivetrain" list
        nte_talonMode = driveTrainList.add("Talon Mode", "---").getEntry();
    }

    protected void setMaxSpeed(double factor)
    {
        // Just set the NTE for Max Speed; the widget will update on its own
        nte_maxSpeed.setDouble(factor);
    }

    protected double getMaxSpeed()
    {
        // Return the Max speed as indicated in the Shuffleboard widget
        return (nte_maxSpeed.getDouble(1.0));
    }

    protected void setMaxSpeedScaleFactor()
    {

        // Read the value of the 'Max Speed' widget from the Shuffleboard
        // and set the max speed multiplier in the DifferentialDrive object
        m_drive.setMaxOutput(getMaxSpeed());
    }

    protected boolean isDemoMode()
    {
        // Return the setting for the "Demo Mode" switch widget
        return (nte_demoMode.getBoolean(false));
    }

    protected void initDefaultCommand()
    {

        // Set the default command for a subsystem here.
        setDefaultCommand(new DriveBot());
    }

    public WPI_TalonSRX getLeftTalon()
    {
        return left_talon;
    }

    public WPI_TalonSRX getRightTalon()
    {
        return right_talon;
    }

    // Use standard Tank Drive method
    public void driveTank(double leftSpeed, double rightSpeed)
    {
        m_drive.tankDrive(leftSpeed, rightSpeed, false);
    }

    // Use single-stick Arcade Drive method
    public void driveArcade(double move, double rotate)
    {
        m_drive.arcadeDrive(move, rotate, false);
    }

    // Use DifferentialDrive curvatureDrive() method
    public void drive(double outputMagnitude, double curve, boolean spin)
    {
        m_drive.curvatureDrive(outputMagnitude, curve, spin);
    }

    /**
     * @param controlMode Set the control mode of the left and right master WPI_TalonSRXs
     */
    public void setControlMode(ControlMode controlMode)
    {
        left_talon.set(controlMode, 0.0);
        right_talon.set(controlMode, 0.0);

        // Save control mode so we will know if we have to set it back later
        m_talonControlMode = controlMode;

        // Update the name of the current ControlMode on the Shuffleboard
        if (m_talonControlMode == ControlMode.PercentOutput)
        {
            nte_talonMode.setString("PercentVbus");
        }
        else if (m_talonControlMode == ControlMode.Position)
        {
            nte_talonMode.setString("Position");
        }
        else
            nte_talonMode.setString("Problem");
    }

    /**
     * @return The current WPI_TalonSRX control mode
     */
    public ControlMode getTalonControlMode()
    {
        return m_talonControlMode;
    }

    /**
     * Update the drive control mode on the Shuffleboard
     * 
     * @param driveMode - String defined by caller
     */
    public void reportDriveControlMode(String driveMode)
    {
        nte_driveMode.setString(driveMode);
    }

    /**
     * Sets the brake mode for ALL WPI_TalonSRXs
     * 
     * @param setBrake Enable brake mode?
     */
    public void setTalonBrakes(boolean setBrake)
    {

        NeutralMode nm = setBrake ? NeutralMode.Brake : NeutralMode.Coast;

        left_talon.setNeutralMode(nm);
        left_victor_1.setNeutralMode(nm);
        right_talon.setNeutralMode(nm);
        right_victor_1.setNeutralMode(nm);

        // On the Shuffleboard, show Red if brakes are set, Green if they are off
        nte_talonBrakes.setBoolean(!setBrake);
    }

    /**
     * @return Average of the encoder values from the left and right encoders
     */
    public double getDistance()
    {
        return (left_talon.getSelectedSensorPosition(0) +
                right_talon.getSelectedSensorPosition(0)) / 2;
    }

    /**
     * Update encoder values on the Shuffleboard
     */
    public void reportEncoders()
    {
        nte_leftEncoderVal.setDouble(left_talon.getSelectedSensorPosition(0));
        nte_rightEncoderVal.setDouble(right_talon.getSelectedSensorPosition(0));
    }

    /**
     * Reset drivetrain encoder values to zero
     */
    public void resetEncoders()
    {
        left_talon.setSelectedSensorPosition(0, 0, 0);
        right_talon.setSelectedSensorPosition(0, 0, 0);
    }

}
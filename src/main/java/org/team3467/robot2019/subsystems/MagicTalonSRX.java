/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.team3467.robot2019.subsystems;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import org.team3467.robot2019.robot.RobotGlobal;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

/**
 * Class MagicTalonSRX
 * 
 * An extension of the TalonSRX class that adds functionality for managing control loops
 * 
 * Manages PIDF constants and updates Shuffleboard with information useful for running a TalonSRX in closed-loop mode.
 * 
 */
public class MagicTalonSRX extends TalonSRX
{

    // Instance number counter
    private static int g_instanceNumber = 0;

    // Use this to slow down PID loops for initial tuning
    private static double PEAK_OUTPUT = 1.0;

    // ID of this Talon on Shuffleboard
    private String m_name;

    // Instance number of this object
    private int m_instance;

    // Profile slot number (also known as "ordinal Id")
    private int m_slotNum = 0;

    // Tolerance is expressed in raw encoder units
    // Calculate Tolerance as a percentage of overall range of encoder motion
    private int m_tolerance = 0;

    // Default setpoint
    private double m_setpoint = 0.0;

    // Default PIDF constants - see CTRE documentation for tuning procedure
    private double m_P = 0.0; // factor for "proportional" control
    private double m_I = 0.0; // factor for "integral" control
    private double m_D = 0.0; // factor for "derivative" control
    private double m_F = 0.0; // factor for feedforward term

    private int m_iZone = 0; // Integral Zone

    // Default cruise velocity and acceleration - see CTRE documentation for tuning procedure
    private int m_cruiseVel = 0;
    private int m_accel = 0;

    // Controls display to Shuffleboard - turn this off after system is tuned
    private boolean m_debugging = false;

    /**
     * 
     * @param name
     * @param deviceID
     * @param slotNum
     * @param debugging
     */
    public MagicTalonSRX(String name, int deviceID, int slotNum, boolean debugging)
    {
        super(deviceID);

        // Assign a number to this instance and increment the global counter
        // This is used to determine which Shuffleboard column to write to
        m_instance = g_instanceNumber++;

        m_name = name;
        m_slotNum = slotNum;
        m_debugging = debugging;

        setupShuffleboard();

        // Set all configuration parameters to factory defaults
        configFactoryDefault();

        // Turn on Brake mode
        setNeutralMode(NeutralMode.Brake);

        // Set Grayhill encoder as default feedback device
        // final int TICKS_PER_ROTATION = 256;
        configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);

        /* Set relevant frame periods to be at least as fast as periodic rate */
        setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, 10);
        setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 10);

        /* set the peak and nominal outputs, 1.0 means full */
        configNominalOutputForward(0, 0);
        configNominalOutputReverse(0, 0);
        configPeakOutputForward(PEAK_OUTPUT, 0);
        configPeakOutputReverse(-PEAK_OUTPUT, 0);

        /* config closed loop gains */
        configPIDF(m_slotNum, m_P, m_I, m_D, m_F);

        /* config motion parameters */
        configMotion(m_cruiseVel, m_accel, m_tolerance);

        /* zero the sensor (May not want to do this on startup?) */
        setSelectedSensorPosition(0, 0, 10);

    }

    /* Setup Shuffleboard and NetworkTableEntries for displaying and changing data */
    /* NTE's are used to reference Shuffleboard tiles when you need to read or update them */
    private NetworkTableEntry nte_P;
    private NetworkTableEntry nte_I;
    private NetworkTableEntry nte_D;
    private NetworkTableEntry nte_F;
    private NetworkTableEntry nte_I100;
    private NetworkTableEntry nte_setPoint;
    private NetworkTableEntry nte_tolerance;
    private NetworkTableEntry nte_cruiseVel;
    private NetworkTableEntry nte_accel;
    private NetworkTableEntry nte_controlMode;
    private NetworkTableEntry nte_position;
    private NetworkTableEntry nte_velocity;
    private NetworkTableEntry nte_percentOutput;
    private NetworkTableEntry nte_closedLoopTarget;
    private NetworkTableEntry nte_closedLoopError;

    private void setupShuffleboard()
    {

        // Make a 'm_name' list on the "Calibration" tab
        ShuffleboardTab sensorsTab = Shuffleboard.getTab(RobotGlobal.SBT_CALIBRATION_TAB);
        ShuffleboardLayout magicTalonList = sensorsTab.getLayout(m_name, BuiltInLayouts.kList).withPosition(m_instance, 0).withSize(1, 6);

        if (m_debugging)
        {
            // Add the MagicTalon configs and stats to the "m_name" list
            nte_P = magicTalonList.add("P", 0.0).getEntry();
            nte_I = magicTalonList.add("I", 0.0).getEntry();
            nte_D = magicTalonList.add("D", 0.0).getEntry();
            nte_F = magicTalonList.add("F", 0.0).getEntry();
            nte_I100 = magicTalonList.add("I*100", 0.0).getEntry();
            nte_setPoint = magicTalonList.add("Setpoint", 0.0).getEntry();
            nte_tolerance = magicTalonList.add("Tolerance", 0.0).getEntry();
            nte_cruiseVel = magicTalonList.add("Cruise Vel", 0.0).getEntry();
            nte_accel = magicTalonList.add("Acceleration", 0.0).getEntry();
            nte_controlMode = magicTalonList.add("ControlMode", 0.0).getEntry();
            nte_position = magicTalonList.add("Position", 0.0).getEntry();
            nte_velocity = magicTalonList.add("Velocity", 0.0).getEntry();
            nte_percentOutput = magicTalonList.add("Percent Output", 0.0).getEntry();
            nte_closedLoopTarget = magicTalonList.add("Closed Loop Target", 0.0).getEntry();
            nte_closedLoopError = magicTalonList.add("Closed Loop Error", 0.0).getEntry();
        }
    }

    /**
     * Configure a feedback sensor (e.g. encoder) other than the default
     * 
     * @param fbd - The feedback sensor object
     */
    public void configFeedbackSensor(FeedbackDevice fbd)
    {
        // Set alternative feedback device
        configSelectedFeedbackSensor(fbd, 0, 10);
    }

    /**
     * Run the Talon with Motion Magic, taking the setpoint from the Shuffleboard
     */
    public void runMotionMagic()
    {
        // Tell Shuffleboard to update m_setpoint
        updateSetpointFromShuffleboard();

        this.set(ControlMode.MotionMagic, m_setpoint);

        if (m_debugging)
        {
            reportMotionToShuffleboard();
        }
    }

    /**
     * Run the Talon with Motion Magic, using passed setpoint
     * 
     * @param setpoint - the encoder reading to move to
     */
    public void runMotionMagic(int setpoint)
    {
        this.set(ControlMode.MotionMagic, setpoint);
        m_setpoint = setpoint;

        if (m_debugging)
        {
            writeMotionToShuffleboard();
            reportMotionToShuffleboard();
        }
    }

    /**
     * Set the PID Controller gain parameters. Set the proportional, integral, and differential coefficients.
     * 
     * @param p - Proportional coefficient
     * @param i - Integral coefficient
     * @param d - Differential coefficient
     * @param f - Feed forward coefficient
     */
    public synchronized void configPIDF(int slotnum, double p, double i, double d, double f)
    {
        m_P = p;
        m_I = i;
        m_D = d;
        m_F = f;
        m_slotNum = slotnum;

        selectProfileSlot(m_slotNum, 0);
        config_kP(m_slotNum, p, 0);
        config_kI(m_slotNum, i, 0);
        config_kD(m_slotNum, d, 0);
        config_kF(m_slotNum, f, 0);
        config_IntegralZone(0, m_iZone, 0);

        if (m_debugging)
        {
            writePIDFToShuffleboard();
        }
    }

    /**
     * Write the PIDF parameters to the Shuffleboard
     */
    private void writePIDFToShuffleboard()
    {
        if (m_debugging)
        {
            nte_P.setDouble(configGetParameter(ParamEnum.eProfileParamSlot_P, m_slotNum, 0));
            nte_I.setDouble(configGetParameter(ParamEnum.eProfileParamSlot_I, m_slotNum, 0));
            nte_D.setDouble(configGetParameter(ParamEnum.eProfileParamSlot_D, m_slotNum, 0));
            nte_F.setDouble(configGetParameter(ParamEnum.eProfileParamSlot_F, m_slotNum, 0));
            nte_I100.setDouble((configGetParameter(ParamEnum.eProfileParamSlot_I, m_slotNum, 0)) * 100);
            
        }
    }

    /**
     * Update the working PIDF parameters from the values currently on the Shuffleboard
     */
    private void updatePIDFFromShuffleboard()
    {
        if (m_debugging)
        {
            // Assign
            double p = nte_P.getDouble(0.0);
            double i = nte_I.getDouble(0.0);
            double d = nte_D.getDouble(0.0);
            double f = nte_F.getDouble(0.0);
            configPIDF(0, p, i, d, f);

            writePIDFToShuffleboard();
        }
    }

    /**
     * Configure the Talon Motion Magic parameters
     *
     * @param cruiseV   - cruise velocity
     * @param accel     - acceleration
     * @param tolerance - setpoint error tolerance
     */
    public synchronized void configMotion(int cruiseV, int accel, int tolerance)
    {
        m_cruiseVel = cruiseV;
        m_accel = accel;
        m_tolerance = tolerance;

        /*
         * set acceleration and cruise velocity - see CTRE documentation for how to tune
         */
        configMotionCruiseVelocity(m_cruiseVel, 10);
        configMotionAcceleration(m_accel, 10);

        /* Set curve smoothing (0 - 8) */
        configMotionSCurveStrength(1);

        /* Use the specified tolerance to set the allowable Closed-Loop error */
        configAllowableClosedloopError(m_slotNum, m_tolerance, 10);

        if (m_debugging)
        {
            writeMotionToShuffleboard();
        }
    }

    /**
     * Write the Motion Magic parameters to the Shuffleboard
     */
    private void writeMotionToShuffleboard()
    {
        // These are things that are set infrequently, and may be updated from SDB
        if (m_debugging)
        {
            nte_setPoint.setNumber(m_setpoint);
            nte_tolerance.setNumber(m_tolerance);
            nte_cruiseVel.setNumber(m_cruiseVel);
            nte_accel.setNumber(m_accel);
        }
    }

    /**
     * @return - setpoint error tolerance
     */
    public int getTolerance()
    {
        return m_tolerance;
    }

    /**
     * Set the allowable setpoint error tolerance
     * 
     * @param allowable - allowable setpoint error tolerance
     */
    public void setTolerance(int allowable)
    {
        m_tolerance = allowable;
    }

    /**
     * Report the active Motion Magic statistics to the Shuffleboard
     */
    public void reportMotionToShuffleboard()
    {
        // These are things that we cannot change on SDB; just report their current
        // values
        if (m_debugging)
        {
            nte_controlMode.setString(getTalonControlMode());
            nte_position.setNumber(getSelectedSensorPosition(0));
            nte_velocity.setNumber(getSelectedSensorVelocity(0));
            nte_percentOutput.setNumber(getMotorOutputPercent());

            /* check if we are motion-magic-ing */
            if (getControlMode() == ControlMode.MotionMagic)
            {
                nte_closedLoopTarget.setNumber(getClosedLoopTarget(0));
                nte_closedLoopError.setNumber(getClosedLoopError(0));
            }
        }
    }

    /**
     * Update the working Motion Magic parameters from the values currently on the Shuffleboard
     */
    private void updateMotionFromShuffleboard()
    {
        if (m_debugging)
        {
            // Assign
            m_cruiseVel = (int) nte_cruiseVel.getNumber(0);
            m_accel = (int) nte_accel.getNumber(0);
            m_tolerance = (int) nte_tolerance.getNumber(0);

            // Update
            configMotionCruiseVelocity(m_cruiseVel, 0);
            configMotionAcceleration(m_accel, 0);
            configAllowableClosedloopError(m_slotNum, m_tolerance, 0);

            writeMotionToShuffleboard();
        }
    }

    /**
     * Update the Talon setpoint from the value currently on the Shuffleboard
     */
    public void updateSetpointFromShuffleboard()
    {
        if (m_debugging)
        {
            m_setpoint = nte_setPoint.getDouble(0.0);
        }
    }

    /**
     * Convenience method to update all working parameters using values currently on Shuffleboard
     */
    public void updateStats()
    {
        if (m_debugging)
        {
            updatePIDFFromShuffleboard();
            updateMotionFromShuffleboard();
        }
    }

    /**
     * @return The current TalonSRX control mode
     */
    public String getTalonControlMode()
    {

        ControlMode tcm = getControlMode();

        if (tcm == ControlMode.PercentOutput)
        {
            return "PercentOutput";
        }
        else if (tcm == ControlMode.Position)
        {
            return "Position";
        }
        else if (tcm == ControlMode.MotionMagic)
        {
            return "MotionMagic";
        }
        else if (tcm == ControlMode.Velocity)
        {
            return "Velocity";
        }
        else
            return "Problem";
    }

}
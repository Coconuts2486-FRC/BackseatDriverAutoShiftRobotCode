/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
//import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Joystick.AxisType;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot 
{
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // these are the motor controllers
  private TalonSRX Right = new TalonSRX(2);
  private TalonSRX RightFollower = new TalonSRX(3);
  private TalonSRX Left = new TalonSRX(4);
  private TalonSRX LeftFollower = new TalonSRX(5);

  //this is the joystick
  private Joystick Joystick1 = new Joystick(0);

  //these are the pnuematic solenoids
  private Solenoid Shifters = new Solenoid(21, 0);

  //this is a boolean to track what gear we're in high/low
  private boolean InLowGear;
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() 
  {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    Left.setInverted(false);
    Right.setInverted(true);
    RightFollower.setInverted(true);
    LeftFollower.set(ControlMode.Follower, Left.getDeviceID());
    RightFollower.set(ControlMode.Follower, Right.getDeviceID());
    //LeftFollower.setInverted(true);

    //this code helps to keep track of what gear were in
    Shifters.set(true);
    
    InLowGear = Shifters.get();

    //This is configuring the encoders
    Right.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute);
    Left.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute);

    //This is an advanced camera server program
    //it should help us to control the framerate and resolution
    new Thread(() -> {
      UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
      camera.setResolution(640, 480);
      //camera.setFPS(24);
      
      CvSink cvSink = CameraServer.getInstance().getVideo();
      CvSource outputStream = CameraServer.getInstance().putVideo("CoconutsVideo", 640, 480);
      
      Mat source = new Mat();
      Mat output = new Mat();
      
      while(!Thread.interrupted()) {
        cvSink.grabFrame(source);
        Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
        outputStream.putFrame(output);
      }
    }).start();

  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() 
  {

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() 
  {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() 
  {
    switch (m_autoSelected) 
    {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() 
  {
    while(DriverStation.getInstance().isEnabled() && DriverStation.getInstance().isOperatorControl()) 
    {
      //This is the code to use arcade mode tank drive
      Right.set(ControlMode.PercentOutput, (Joystick1.getAxis(AxisType.kY) - -Joystick1.getAxis(AxisType.kX)));
      Left.set(ControlMode.PercentOutput, (Joystick1.getAxis(AxisType.kY) + -Joystick1.getAxis(AxisType.kX)));
      //This code allows shifting
      if (Joystick1.getTriggerPressed()) 
      {
        Shifters.set(!Shifters.get());
      } 
      if (Joystick1.getRawButton(11)) 
      {
        //if ((Right.getSelectedSensorVelocity() < - 100) && (Left.getSelectedSensorVelocity() < - 100))
        Robot.this.BackseatDriver();
      }
      //System.out.println(Left.getSelectedSensorVelocity());
      //System.out.println(Right.getSelectedSensorVelocity());

    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() 
  {

  }

  public void BackseatDriver()
  {
    if ((((Left.getSelectedSensorVelocity() + Right.getSelectedSensorVelocity()) / 2) < -10000) /*&& InLowGear*/)
      {
        Shifters.set(false);
      }
      else
      {
        Shifters.set(true);
      }
  }
}

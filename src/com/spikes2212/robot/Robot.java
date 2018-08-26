/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.spikes2212.robot;

import java.util.function.Supplier;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.spikes2212.dashboard.DashBoardController;
import com.spikes2212.genericsubsystems.drivetrains.TankDrivetrain;
import com.spikes2212.genericsubsystems.drivetrains.commands.DriveArcade;
import com.spikes2212.utils.DoubleSpeedcontroller;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Scheduler;
import odometry.OdometryHandler;
import orientationUtils.preferences.OdometryUnit;
import routes.utils.Position2D;
import utils.Point;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends TimedRobot {

	public static double ENCODERS_DISTANCE_PER_PULSE = Math.PI * 6.2 / 360;

	public static double ROBOT_WIDTH_INCHES = 60 / 2.54;

	public static OI oi;
	public static TankDrivetrain drivetrain;
	public static ADIS16448_IMU imu = new ADIS16448_IMU();

	public static DashBoardController dbc = new DashBoardController();

	public static Encoder leftEncoder;
	public static Encoder rightEncoder;

	public static OdometryHandler handler;

	public static Supplier<Double> yawSupplier;

	public static Position2D position = new Position2D(0, 0, 0);

	public static Point displacement;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {

		leftEncoder = new Encoder(RobotMap.DIO.DRIVE_LEFT_ENCODER_A, RobotMap.DIO.DRIVE_LEFT_ENCODER_B);
		rightEncoder = new Encoder(RobotMap.DIO.DRIVE_RIGHT_ENCODER_A, RobotMap.DIO.DRIVE_RIGHT_ENCODER_B);

		leftEncoder.setDistancePerPulse(ENCODERS_DISTANCE_PER_PULSE);
		rightEncoder.setDistancePerPulse(ENCODERS_DISTANCE_PER_PULSE);

		SpeedController left = new DoubleSpeedcontroller(new WPI_TalonSRX(RobotMap.CAN.DRIVE_LEFT1),
				new WPI_TalonSRX(RobotMap.CAN.DRIVE_LEFT2));
		SpeedController right = new DoubleSpeedcontroller(new WPI_TalonSRX(RobotMap.CAN.DRIVE_RIGHT1),
				new WPI_TalonSRX(RobotMap.CAN.DRIVE_RIGHT2));

		drivetrain = new TankDrivetrain(left::set, right::set);

		oi = new OI();

		drivetrain.setDefaultCommand(new DriveArcade(drivetrain, oi::getRotation, oi::getForward));

		imu.reset();

		yawSupplier = () -> imu.getAngleY();

		displacement = new Point(0, 0);

		position = new Position2D(0, 0, 0);
		OdometryUnit odometryUnit = new OdometryUnit(leftEncoder::getDistance, rightEncoder::getDistance,
				ROBOT_WIDTH_INCHES, yawSupplier);

		handler = new OdometryHandler(odometryUnit);

		// dbc.addDouble("right encoder value", () -> (double)
		// rightEncoder.get/* Distance */());
		// dbc.addDouble("left encoder value", () -> (double) leftEncoder.get/*
		// Distance */());
		//
		// dbc.addDouble("right encoder distance", () -> (double)
		// rightEncoder.getDistance());
		// dbc.addDouble("left encoder distance", () -> (double)
		// leftEncoder.getDistance());

		// dbc.addDouble("displacement x", displacement::getX);
		// dbc.addDouble("displacement y", displacement::getY);

		dbc.addDouble("position x", position::getX);
		dbc.addDouble("position y", position::getY);

		dbc.addDouble("yaw angle", yawSupplier);

		rightEncoder.reset();
		leftEncoder.reset();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
	 */
	@Override
	public void disabledInit() {
	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
		dbc.update();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * <p>
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {

		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		// schedule the autonomous command (example)

	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();
	}

	@Override
	public void teleopInit() {
		imu.reset();
		rightEncoder.reset();
		leftEncoder.reset();

		position.setXAndY(0, 0);
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		Point newDisplacement = handler.getDifference();
		displacement.setXAndY(newDisplacement.getX(), newDisplacement.getY());

		position.move(displacement.getX(), displacement.getY());
		position.setAngle(imu.getAngleY());

		dbc.update();
		Scheduler.getInstance().run();
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
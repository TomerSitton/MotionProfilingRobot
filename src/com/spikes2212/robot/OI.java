/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.spikes2212.robot;

import java.util.function.Supplier;

import com.spikes2212.dashboard.ConstantHandler;
import com.spikes2212.genericsubsystems.drivetrains.commands.DriveArcade;
import com.spikes2212.genericsubsystems.drivetrains.commands.DriveTankWithPID;
import com.spikes2212.robot.commands.DriveByRoute;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import routes.utils.Position2D;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {

	private Joystick driver = new Joystick(0);
	private static final Supplier<Double> VOLAGE = ConstantHandler.addConstantDouble("VOLTAGE", 0.1);

	public OI() {

		JoystickButton routate = new JoystickButton(driver, 1);
		routate.whenPressed(new DriveByRoute(new Position2D(150, 100, Math.PI / 2), Preferences.ROTATE_PID_SETTINGS,
				Preferences.MOVE_PID_SETTINGS));

		JoystickButton check = new JoystickButton(driver, 2);
		check.whenPressed(new DriveTankWithPID(Robot.drivetrain, Robot.leftEncoder, Robot.rightEncoder, 1000,
				Preferences.MOVE_PID_SETTINGS));

		JoystickButton testVoltage = new JoystickButton(driver, 3);
		testVoltage.whenPressed(new DriveArcade(Robot.drivetrain, VOLAGE, () -> 0.0));

	}

	public double getForward() {
		return driver.getY();
	}

	public double getRotation() {
		return driver.getX();
	}
}

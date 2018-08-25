package com.spikes2212.robot.commands;

import com.spikes2212.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;

/**
 * this command drives the robot in a constant voltage and measures it's maximum
 * acceleration
 */
public class DriveByVoltage extends Command {

	private double maxAcc;
	private final double drivingVoltage;

	public DriveByVoltage(double drivingVoltage) {
		requires(Robot.drivetrain);
		this.drivingVoltage = drivingVoltage;
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		maxAcc = 0;
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
		Robot.drivetrain.arcadeDrive(drivingVoltage, 0);
		double currentAcc = Robot.imu.getAccelX();
		if (currentAcc > maxAcc)
			maxAcc = currentAcc;
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true
	protected void end() {
		Robot.drivetrain.stop();
		System.out.println("Maximum acc measured for " + drivingVoltage + "V was " + maxAcc);
	}

}

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.spikes2212.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
	public static interface CAN {
		public static final int DRIVE_RIGHT1 = 2;
		public static final int DRIVE_RIGHT2 = 0/* 3 */;
		public static final int DRIVE_LEFT1 = 4;
		public static final int DRIVE_LEFT2 = 5;
	}

	public static interface DIO {
		public static final int DRIVE_RIGHT_ENCODER_A = /* 10 */6;
		public static final int DRIVE_RIGHT_ENCODER_B = /* 11 */7;
		public static final int DRIVE_LEFT_ENCODER_A = /* 12 */8;
		public static final int DRIVE_LEFT_ENCODER_B = /* 13 */9;
	}
}

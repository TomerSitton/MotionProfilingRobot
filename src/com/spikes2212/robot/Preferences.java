package com.spikes2212.robot;

import java.util.function.Supplier;

import com.spikes2212.dashboard.ConstantHandler;
import com.spikes2212.utils.PIDSettings;

public class Preferences {
	private static final Supplier<Double> ROUTE_MOVE_KP = ConstantHandler.addConstantDouble("route kp move", 0.4);
	private static final Supplier<Double> ROUTE_MOVE_KI = ConstantHandler.addConstantDouble("route ki move", 0);
	private static final Supplier<Double> ROUTE_MOVE_KD = ConstantHandler.addConstantDouble("route kd move", 0.1);

	private static final Supplier<Double> ROUTE_ROTATE_KP = ConstantHandler.addConstantDouble("route kp rotate", 0.035);
	private static final Supplier<Double> ROUTE_ROTATE_KI = ConstantHandler.addConstantDouble("route ki rotate",
			0.0005);
	private static final Supplier<Double> ROUTE_ROTATE_KD = ConstantHandler.addConstantDouble("route kd rotate", 0.005);

	public static final PIDSettings MOVE_PID_SETTINGS = new PIDSettings(ROUTE_MOVE_KP.get(), ROUTE_MOVE_KI.get(),
			ROUTE_MOVE_KD.get(), 0, 0);
	public static final PIDSettings ROTATE_PID_SETTINGS = new PIDSettings(ROUTE_ROTATE_KP.get(), ROUTE_ROTATE_KI.get(),
			ROUTE_ROTATE_KD.get(), 0, 0);
}
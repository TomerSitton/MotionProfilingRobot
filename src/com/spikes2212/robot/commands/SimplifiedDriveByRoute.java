package com.spikes2212.robot.commands;

import java.awt.geom.Point2D;

import com.spikes2212.robot.Robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import routes.routes.RouteFunctionsProvider;
import routes.routes.RouteProvider;
import routes.routes.SplineFunctionsProvider;
import routes.synchronizing.MaxSpeedsFactory;
import routes.synchronizing.RouteSynchronizer;
import routes.synchronizing.SpeedProviderFactory;
import routes.utils.Position2D;
import routes.utils.RoutePointInfo;

/**
 *
 */
public class SimplifiedDriveByRoute extends Command {
	public static final double K = 1000;
	public static final int NUM_POINTS = 100;

	private RouteSynchronizer sync;

	private final Point2D destination;
	private Point2D setPoint;
	private Point2D error;

	private Timer timer;

	public SimplifiedDriveByRoute(Position2D destination/* DEEP SPACE */) {

		this.destination = destination;

		setPoint = new Point2D.Double(0, 0);

		RouteFunctionsProvider desc = new SplineFunctionsProvider(Robot.position, destination, K);

		RouteProvider routeProvider = new RouteProvider(desc);
		RoutePointInfo[] routeInfo = routeProvider.getRoute(NUM_POINTS);

		SpeedProviderFactory factory = new MaxSpeedsFactory(Robot.ROBOT_WIDTH_INCHES, 1, 1);
		sync = new RouteSynchronizer(factory, routeInfo);

		timer = new Timer();

		Robot.dbc.addDouble("route set point x", () -> setPoint.getX());
		Robot.dbc.addDouble("route set point y", () -> setPoint.getY());
	}

	@Override
	protected void initialize() {
		timer.start();
	}

	private Point2D difference(Point2D p1, Point2D p2) {
		double x = p1.getX() - p2.getX();
		double y = p1.getY() - p2.getY();

		return new Point2D.Double(x, y);
	}

	@Override
	protected void execute() {

		Point2D newSetPoint = sync.getPosition(timer.get());
		setPoint.setLocation(newSetPoint.getX(), newSetPoint.getY());

		error = difference(setPoint, Robot.position);

	}

	@Override
	protected boolean isFinished() {
		return Robot.position.distance(destination) < 1;
	}

	@Override
	protected void end() {
		Robot.drivetrain.stop();
	}

	@Override
	protected void interrupted() {
		end();
	}
}

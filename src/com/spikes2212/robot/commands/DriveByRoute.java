package com.spikes2212.robot.commands;

import java.awt.geom.Point2D;

import com.spikes2212.robot.Robot;
import com.spikes2212.utils.PIDSettings;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
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
import utils.Utils;

/**
 *
 */
public class DriveByRoute extends Command {

	public static final double K = 100;
	public static final int NUM_POINTS = 200;

	private RouteSynchronizer sync;

	private final Point2D destination;
	private Point2D setPoint;
	private Point2D error = new Point2D.Double(0, 0);

	private Timer timer;

	private PIDSettings rotationPIDSettings;
	private PIDSettings movementPIDSettings;

	private PIDController rotationController;
	private PIDController movementController;

	private double moveValue = 0;
	private double rotateValue = 0;

	private PIDSource movementSource = new PIDSource() {

		@Override
		public void setPIDSourceType(PIDSourceType pidSource) {
		}

		@Override
		public double pidGet() {
			double yaw = Math.toRadians(Robot.yawSupplier.get());
			return Utils.rotateVector(error, -yaw).getX();
		}

		@Override
		public PIDSourceType getPIDSourceType() {
			return PIDSourceType.kDisplacement;
		}
	};

	private PIDSource rotationSource = new PIDSource() {

		@Override
		public void setPIDSourceType(PIDSourceType pidSource) {
		}

		@Override
		public double pidGet() {
			double yaw = Math.toRadians(Robot.yawSupplier.get());
			return Utils.rotateVector(error, -yaw).getY();
		}

		@Override
		public PIDSourceType getPIDSourceType() {
			return PIDSourceType.kDisplacement;
		}
	};

	public DriveByRoute(Position2D destination/* DEEP SPACE */, PIDSettings rotationSettings,
			PIDSettings movementSettings) {
		requires(Robot.drivetrain);
		this.destination = destination;

		setPoint = new Point2D.Double(0, 0);

		RouteFunctionsProvider desc = new SplineFunctionsProvider(Robot.position, destination, K);

		RouteProvider routeProvider = new RouteProvider(desc);
		RoutePointInfo[] routeInfo = routeProvider.getRoute(NUM_POINTS);

		SpeedProviderFactory factory = new MaxSpeedsFactory(Robot.ROBOT_WIDTH_INCHES, 100, 50);
		sync = new RouteSynchronizer(factory, routeInfo);

		timer = new Timer();

		this.rotationPIDSettings = rotationSettings;
		this.movementPIDSettings = movementSettings;

		Robot.dbc.addDouble("route set point x", () -> setPoint.getX());
		Robot.dbc.addDouble("route set point y", () -> setPoint.getY());

		Robot.dbc.addDouble("route error movement", () -> movementSource.pidGet());
		Robot.dbc.addDouble("route error rotation", () -> rotationSource.pidGet());
	}

	@Override
	protected void initialize() {

		rotationController = new PIDController(rotationPIDSettings.getKP(), rotationPIDSettings.getKI(),
				rotationPIDSettings.getKD(), rotationSource, (value) -> rotateValue = value);
		rotationController.setAbsoluteTolerance(rotationPIDSettings.getTolerance());
		rotationController.setSetpoint(0);
		rotationController.setOutputRange(-0.5, 0.5);

		movementController = new PIDController(movementPIDSettings.getKP(), movementPIDSettings.getKI(),
				movementPIDSettings.getKD(), movementSource, (value) -> moveValue = value);
		movementController.setAbsoluteTolerance(movementPIDSettings.getTolerance());
		movementController.setSetpoint(0);
		movementController.setOutputRange(-1, 1);

		rotationController.enable();
		movementController.enable();

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
		System.out.println(setPoint);
		Point2D newError = difference(setPoint, Robot.position);
		error.setLocation(newError.getX(), newError.getY());

		Robot.drivetrain.arcadeDrive(rotateValue, moveValue);
	}

	@Override

	protected boolean isFinished() {
		return Robot.position.distance(destination) < 1;
	}

	@Override
	protected void end() {
		System.out.println("ended");
		Robot.drivetrain.stop();
		rotationController.disable();
		movementController.disable();
	}

	@Override
	protected void interrupted() {
		end();
	}
}

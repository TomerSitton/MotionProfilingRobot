package com.spikes2212.robot.commands;

import java.util.function.Supplier;

import com.spikes2212.dashboard.ConstantHandler;
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
import utils.Point;
import utils.Utils;

/**
 *
 */
public class DriveByRoute extends Command {

	public static final double K = 100;
	public static final int NUM_POINTS = 200;

	private RouteSynchronizer sync;

	private final Point destination;
	private Point setPoint;
	private Point error = new Point(0, 0);

	private Timer timer;

	private PIDSettings rotationPIDSettings;
	private final Supplier<Double> MOVE_VALUE = ConstantHandler.addConstantDouble("MOVE_VALUE", 0.3);

	private PIDController rotationController;

	private double rotateValue = 0;

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

		setPoint = new Point(0, 0);

		RouteFunctionsProvider desc = new SplineFunctionsProvider(Robot.position, destination, K);

		RouteProvider routeProvider = new RouteProvider(desc);
		RoutePointInfo[] routeInfo = routeProvider.getRoute(NUM_POINTS);

		SpeedProviderFactory factory = new MaxSpeedsFactory(Robot.ROBOT_WIDTH_INCHES, 100, 50);
		sync = new RouteSynchronizer(factory, routeInfo);

		timer = new Timer();

		this.rotationPIDSettings = rotationSettings;

		Robot.dbc.addDouble("route set point x", () -> setPoint.getX());
		Robot.dbc.addDouble("route set point y", () -> setPoint.getY());

		Robot.dbc.addDouble("route error rotation", () -> rotationSource.pidGet());
	}

	@Override
	protected void initialize() {

		rotationController = new PIDController(rotationPIDSettings.getKP(), rotationPIDSettings.getKI(),
				rotationPIDSettings.getKD(), rotationSource, (value) -> rotateValue = value);
		rotationController.setAbsoluteTolerance(rotationPIDSettings.getTolerance());
		rotationController.setSetpoint(0);
		rotationController.setOutputRange(-0.5, 0.5);

		rotationController.enable();

		timer.start();
	}

	private Point difference(Point p1, Point p2) {
		double x = p1.getX() - p2.getX();
		double y = p1.getY() - p2.getY();

		return new Point(x, y);
	}

	@Override
	protected void execute() {
		Point newSetPoint = sync.getPosition(timer.get());
		setPoint.setXAndY(newSetPoint.getX(), newSetPoint.getY());
		System.out.println(setPoint);
		Point newError = difference(setPoint, Robot.position);
		error.setXAndY(newError.getX(), newError.getY());

		Robot.drivetrain.arcadeDrive(rotateValue, MOVE_VALUE.get());
	}

	@Override

	protected boolean isFinished() {
		return Point.distance(Robot.position, destination) < 1;
	}

	@Override
	protected void end() {
		System.out.println("ended");
		Robot.drivetrain.stop();
		rotationController.disable();
	}

	@Override
	protected void interrupted() {
		end();
	}
}

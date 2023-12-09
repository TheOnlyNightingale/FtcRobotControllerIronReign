package org.firstinspires.ftc.teamcode.robots.csbot.subsystem;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.robots.csbot.util.Joint;
import static org.firstinspires.ftc.teamcode.util.utilMethods.futureTime;
import org.firstinspires.ftc.teamcode.robots.csbot.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

//i still have no clue what the intake needs lmao

@Config(value = "CS_INTAKE")
public class Intake implements Subsystem {
    public static final int RIGHT_DIVERTER_OPEN_TICKS = 1000;
    public static final int LEFT_DIVERTER_OPEN_TICKS = 1490;
    public static final int LEFT_DIVERTER_CLOSED_TICKS = 1320;
    public static final int RIGHT_DIVERTER_CLOSED_TICKS = 1300;
    public static  int ANGLE_CONTROLLER_MAX = 2250;
    public static int ANGLE_CONTROLLER_MIN = 1400;
    //CONSTANTS
    HardwareMap hardwareMap;
    Robot robot;
    Servo diverterRight, diverterLeft;
    Servo angleController;
    DcMotorEx beaterBar;
    public static boolean precisionBeaterBar = false;
    public boolean manualBeaterBarEject = false;
    public boolean manualBeaterBarOn = false;
    public static double BEATER_BAR_INTAKE_VELOCITY = 2000;
    public static double BEATER_BAR_EJECT_VELOCITY = -700;

    public static int angleControllerTicks = ANGLE_CONTROLLER_MAX;
    public static int BEATER_BAR_FOLD_ANGLE = 2470;
    public static int BEATER_BAR_WING_ANGLE = 1701;
    public static int BEATER_BAR_EJECT_ANGLE = 1550;
    public static int BEATER_BAR_HANG_ANGLE = 1565;
    public static int SWALLOW_TICKS = 1950;


    public enum Articulation {
        WING_INTAKE_POSTION,
        STACK_INTAKE,
        EJECT,
        OFF,
        FOLD,
        MANUAL,
        HANG,
        SWALLOW,
        INIT
    }

    public enum DiverterState{
        DELIVER_LEFT,
        DELIVER_RIGHT,
        DELIVER_BOTH
    }

    //LIVE STATES
    public DiverterState diverterState;
    public Articulation articulation;
    public static int numPixelsInStack = 6;
    public static double angleToStack;
    public static double beaterBarTargetAngle;


    public Intake(HardwareMap hardwareMap, Robot robot) {
            this.hardwareMap = hardwareMap;
            this.robot = robot;
            articulation = Articulation.MANUAL;
            diverterState = DiverterState.DELIVER_BOTH;
            angleControllerTicks = ANGLE_CONTROLLER_MIN;

            diverterLeft = hardwareMap.get(Servo.class, "diverterRight");
            diverterRight = hardwareMap.get(Servo.class, "diverterLeft");
            beaterBar = hardwareMap.get(DcMotorEx.class, "beaterBar");
            beaterBar.setDirection(DcMotorSimple.Direction.REVERSE);
            angleController = hardwareMap.get(Servo.class, "beaterBarAngleController");

            beaterBar.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }
    public void update(){
        update(new Canvas());
    }

    @Override
    public void update(Canvas fieldOverlay) {
        switch (articulation) {
            case MANUAL:
                if(manualBeaterBarOn) {
                    if(!manualBeaterBarEject) {
                        beaterBar.setPower(1);
                        beaterBar.setVelocity(BEATER_BAR_INTAKE_VELOCITY);
                    }
                    else {
                        beaterBar.setPower(1);
                        beaterBar.setVelocity(BEATER_BAR_EJECT_VELOCITY);
                    }
                }

                else
                    beaterBar.setVelocity(0);
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                switch (diverterState) {
                    case DELIVER_BOTH:
                        diverterRight.setPosition(Utils.servoNormalize(RIGHT_DIVERTER_OPEN_TICKS));
                        diverterLeft.setPosition(Utils.servoNormalize(LEFT_DIVERTER_OPEN_TICKS));
                        break;
                    case DELIVER_LEFT:
                        diverterRight.setPosition(Utils.servoNormalize(RIGHT_DIVERTER_OPEN_TICKS));
                        diverterLeft.setPosition(Utils.servoNormalize(LEFT_DIVERTER_CLOSED_TICKS));
                        break;
                    case DELIVER_RIGHT:
                        diverterRight.setPosition(Utils.servoNormalize(RIGHT_DIVERTER_CLOSED_TICKS));
                        diverterRight.setPosition(Utils.servoNormalize(RIGHT_DIVERTER_OPEN_TICKS));
                        break;
                }
                break;
            case SWALLOW:
                angleControllerTicks = SWALLOW_TICKS;
                manualBeaterBarOn = true;
                manualBeaterBarEject = false;
                articulation = Articulation.MANUAL;
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                break;
            case HANG:
                angleControllerTicks = BEATER_BAR_HANG_ANGLE;
                manualBeaterBarEject = false;
                manualBeaterBarOn = false;
                articulation = Articulation.MANUAL;
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                break;
            case WING_INTAKE_POSTION:
                if(wingIntakePostion()) {
                    articulation = Articulation.MANUAL;
                }
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                break;
            case FOLD:
                beaterBar.setPower(0);
                angleControllerTicks = BEATER_BAR_FOLD_ANGLE;
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                break;
            case STACK_INTAKE:
                beaterBar.setVelocity(BEATER_BAR_INTAKE_VELOCITY);
                beaterBarTargetAngle = angleToStack;
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                break;
            case INIT:
                angleControllerTicks = ANGLE_CONTROLLER_MAX;
                angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
                articulation = Articulation.MANUAL;
                break;
        }
        angleController.setPosition(Utils.servoNormalize(angleControllerTicks));

    }

    public Articulation articulate(Articulation target) {
        articulation = target;
        return articulation;
    }
    public void setAngleControllerTicks (int ticks) {
        angleControllerTicks = ticks;
    }


    public boolean wingIntakePostion (){
        angleControllerTicks = BEATER_BAR_WING_ANGLE;
        beaterBar.setVelocity(BEATER_BAR_INTAKE_VELOCITY);
        return true;
    }

    public void ejectBeaterBar() {
        manualBeaterBarEject = true;
        manualBeaterBarOn = true;
    }
    public void beaterBarOff () {
        manualBeaterBarEject = false;
        manualBeaterBarOn = false;
    }

    public void togglePrecisionBeaterBar() {
        precisionBeaterBar = !precisionBeaterBar;
    }

    public double adjustBeaterBarAngle(double speed) {
        angleControllerTicks += speed * (precisionBeaterBar ? 10 : 100);
        if(angleControllerTicks < ANGLE_CONTROLLER_MIN) {
            angleControllerTicks = ANGLE_CONTROLLER_MIN;
        }
        if(angleControllerTicks > ANGLE_CONTROLLER_MAX) {
            angleControllerTicks = ANGLE_CONTROLLER_MAX;
        }

        angleController.setPosition(
        Utils.servoNormalize(angleControllerTicks));
        return angleControllerTicks;
    }


    public void toggleBeaterBar() {
        manualBeaterBarOn = !manualBeaterBarOn;
    }
    public void switchBeaterBarDirection (){
        manualBeaterBarEject = !manualBeaterBarEject;
    }

    @Override
    public void stop() {
        beaterBar.setVelocity(0);
    }

    @Override
    public Map<String, Object> getTelemetry(boolean debug) {
        Map<String, Object> telemetryMap = new LinkedHashMap<>();
        telemetryMap.put("articulation", articulation.name());
        telemetryMap.put("manual beater bar on?", manualBeaterBarOn);
        telemetryMap.put("beater bar amps", beaterBar.getPower());
        telemetryMap.put("beater bar velocity", beaterBar.getVelocity());
        telemetryMap.put("angle controller position", Utils.servoDenormalize(angleController.getPosition()));
        telemetryMap.put("diverter state", diverterState.name());
//        telemetryMap.put("beaterBarAngle", beaterBarAngleController.getCurrentAngle());
        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "INTAKE";
    }
}

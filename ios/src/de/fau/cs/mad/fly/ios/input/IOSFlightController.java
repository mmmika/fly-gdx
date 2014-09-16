package de.fau.cs.mad.fly.ios.input;

import org.robovm.apple.coremotion.CMAttitude;
import org.robovm.apple.coremotion.CMMotionManager;

import de.fau.cs.mad.fly.game.FlightController;
import de.fau.cs.mad.fly.player.Player;
import de.fau.cs.mad.fly.profile.PlayerProfile;

/**
 * Created by tschaei on 15.09.14.
 */
public class IOSFlightController extends FlightController{
    private CMMotionManager motionManager;
    private CMAttitude currentAttitude;

    public IOSFlightController(Player player, PlayerProfile playerProfile) {
        super(player, playerProfile);
        motionManager = new CMMotionManager();
        motionManager.setDeviceMotionUpdateInterval(1.f/60.f);
        motionManager.startDeviceMotionUpdates();
    }

    @Override
    public void resetSteering() {
        currentAttitude = motionManager.getDeviceMotion().getAttitude();
        startRoll = (float) currentAttitude.getRoll();
        startPitch = (float) currentAttitude.getPitch();
    }

    @Override
    protected void interpretSensorInput() {
        currentAttitude = motionManager.getDeviceMotion().getAttitude();
        roll = (float) currentAttitude.getRoll();
        pitch = (float) currentAttitude.getPitch();

        // removing oldest element in buffers
        if (rollInput.size() >= bufferSize) {
            rollInput.remove(0);
            pitchInput.remove(0);
        }

        // adding newest sensor-data to buffers
        rollInput.add(roll);
        pitchInput.add(pitch);

        roll = average(rollInput);
        pitch = average(pitchInput);

        // azimuth = computeAzimuth(roll, pitch, azimuth);

        float difRoll = roll - startRoll;
        if (Math.abs(difRoll) > 180) {
            difRoll -= Math.signum(difRoll) * 360;
        }

        float difPitch = pitch - startPitch;
        if (Math.abs(difPitch) > 180) {
            difPitch -= Math.signum(difPitch) * 360;
        }

        // capping the rotation to a maximum of 90 degrees
        if (Math.abs(difRoll) > maxRotate) {
            difRoll = maxRotate * Math.signum(difRoll);
        }

        if (Math.abs(difPitch) > maxRotate) {
            difPitch = maxRotate * Math.signum(difPitch);
        }

        rollFactor = 0.0f;
        azimuthFactor = 0.0f;

        // camera rotation according to smartphone rotation
        setAzimuthFactor(difPitch / maxRotate);
        setRollFactor(difRoll / -maxRotate);
    }
}
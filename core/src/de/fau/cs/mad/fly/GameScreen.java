package de.fau.cs.mad.fly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import de.fau.cs.mad.fly.levelLoader.Level;
import de.fau.cs.mad.fly.levelLoader.LevelManager;

/**
 * Displays the 3D-world.
 * 
 * @author Tobias Zangl
 */
public class GameScreen implements Screen, InputProcessor {
	private final Fly game;

	private float startRoll, startPitch;

	private PerspectiveCamera camera;
	private float cameraSpeed = 0.1f;

	private LevelManager levelManager = new LevelManager();
	private Level level;

	private int rollDir = 0;
	private int pitchDir = 0;
	private boolean useSensorData;

	private Environment environment;
	private ModelInstance space;

	public GameScreen(final Fly game) {
		this.game = game;

		useSensorData = true;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// rotating the camera according to UserInput
		if (useSensorData) {
			interpretSensorInput();
		}

		// rotating the camera
		rotateCamera(rollDir, pitchDir);
		camera.update();

		// move the camera (first person flight)
		Vector3 dir = new Vector3(camera.direction.x, camera.direction.y,
				camera.direction.z);
		camera.translate(dir.scl(cameraSpeed));
		camera.update();

		ModelBatch batch = new ModelBatch();

		batch.begin(camera);
		// rendering outer space
		if (space != null) {
			batch.render(space);
		}
		// render gates
		for (int i = 0; i < level.getGates().size(); i++) {
			batch.render(level.getGates().get(i).modelInstance, environment);
		}
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);

		try {
			level = levelManager.loadLevel("level2");
			setUpCamera();
			setUpEnvironment();
		} catch (Exception e) {
			Gdx.app.log("GameScreen.show():", e.getMessage(), e);
		}
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		Assets.dispose();
	}

	/**
	 * Sets up the camera for the initial view.
	 */
	private void setUpCamera() {

		// initializing Roll- and Pitch-Values for later comparison
		startRoll = Gdx.input.getRoll();
		startPitch = Gdx.input.getPitch();

		// setting up the camera
		float screenHeight = Gdx.graphics.getHeight();
		float screenWidth = Gdx.graphics.getWidth();
		camera = new PerspectiveCamera(67, screenWidth, screenHeight);

		camera.position.set(level.getCameraStartPosition());
		camera.lookAt(level.getCameraLookAt());
		camera.near = 1f;
		camera.far = 400f;
		camera.update();

	}

	/**
	 * Sets up the environment in which the camera should fly
	 */
	private void setUpEnvironment() {
		// setting up the environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f,
				0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f,
				-0.8f, -0.2f));

		space = new ModelInstance(Assets.manager.get(Assets.space));
		space.transform = new Matrix4().scale(2f, 2f, 2f);
	}

	/**
	 * Interprets the rotation of the smartphone and rotates the camera
	 * accordingly
	 */
	private void interpretSensorInput() {
		float roll = Gdx.input.getRoll();
		float pitch = Gdx.input.getPitch();

		float difRoll = roll - startRoll;
		float difPitch = pitch - startPitch;

		rollDir = 0;
		pitchDir = 0;

		// camera rotation according to smartphone rotation
		if (difRoll > 10.0f) {
			rollDir = -1;
		} else if (difRoll < -10.0f) {
			rollDir = 1;
		}

		if (difPitch > 10.0f) {
			pitchDir = -1;
		} else if (difPitch < -10.0f) {
			pitchDir = 1;
		}
	}

	/**
	 * Rotates the camera according to rollDir and pitchDir
	 * 
	 * @param rollDir
	 *            defines if the camera should be rotated up or down
	 * @param pitchDir
	 *            defines if the camera should be rotated left or right
	 */
	private void rotateCamera(int rollDir, int pitchDir) {
		// rotation up or down
		camera.rotate(camera.direction.cpy().crs(camera.up), 1.0f * rollDir);

		// rotation around camera.direction/viewDirection (roll)
		// camera.rotate(camera.direction, 1.0f * pitchDir);

		// rotation around camera.up (turning left/right)
		camera.rotate(camera.up, 1.0f * pitchDir);
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK || keycode == Keys.BACKSPACE) {
			game.setMainMenuScreen();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		if (button == Buttons.LEFT && !useSensorData) {

			float width = (float) Gdx.graphics.getWidth();
			float height = (float) Gdx.graphics.getHeight();

			float xPosition = ((float) screenX) / width;
			float yPosition = ((float) screenY) / height;

			if (xPosition < 0.4f) {
				pitchDir = -1;
			} else if (xPosition > 0.6f) {
				pitchDir = 1;
			}

			if (yPosition < 0.25f) {
				rollDir = -1;
			} else if (yPosition > 0.75f) {
				rollDir = 1;
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		if (button == Buttons.LEFT) {
			rollDir = 0;
			pitchDir = 0;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}

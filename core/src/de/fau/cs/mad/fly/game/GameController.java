package de.fau.cs.mad.fly.game;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.fau.cs.mad.fly.features.IFeatureDispose;
import de.fau.cs.mad.fly.features.IFeatureDraw;
import de.fau.cs.mad.fly.features.IFeatureFinish;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.IFeatureLoad;
import de.fau.cs.mad.fly.features.IFeatureRender;
import de.fau.cs.mad.fly.features.IFeatureUpdate;
import de.fau.cs.mad.fly.player.Player;
import de.fau.cs.mad.fly.res.Level;

/**
 * Manages the Player, the Level, the UI, the CameraController and all the
 * optional Features and calls the load(), init(), render(), finish() and
 * dispose() methods of those.
 * <p>
 * Optional Feature Interfaces:
 * <p>
 * load(): - called before the game starts while the loading screen is shown -
 * should be stuff like loading models, creating instances, which takes a while
 * <p>
 * init(): - called the moment the game starts after switching to the game
 * screen - should be stuff like setting values, resetting counter
 * <p>
 * update(): - called every frame while the game is running and not paused -
 * should be stuff like calculating and updating values
 * <p>
 * render(): - called every frame while the game is running or paused - should
 * be stuff like rendering models
 * <p>
 * draw(): - called every frame while the game is running or paused - should be
 * stuff like drawing overlays
 * <p>
 * finish(): - called at the moment the game is over, still in game screen -
 * should be stuff like showing points, saving the highscore
 * <p>
 * dispose(): - called when the game screen is left - should be stuff like
 * disposing models
 * 
 * @author Lukas Hahmann
 */
public class GameController implements TimeIsUpListener {
    public enum GameState {
        RUNNING, PAUSED, VICTORY, NO_LIVES, TIME_OVER
    }
    
    protected static GameController instance = null;
    
    protected Stage stage;
    protected List<IFeatureLoad> optionalFeaturesToLoad;
    protected List<IFeatureInit> optionalFeaturesToInit;
    protected List<IFeatureUpdate> optionalFeaturesToUpdate;
    protected List<IFeatureRender> optionalFeaturesToRender;
    protected List<IFeatureDraw> optionalFeaturesToDraw;
    protected List<IFeatureDispose> optionalFeaturesToDispose;
    protected List<IFeatureFinish> optionalFeaturesToFinish;
    protected FlightController flightController;
    protected CameraController cameraController;
    protected PerspectiveCamera camera;
    protected ModelBatch batch;
    protected Level level;
    protected Player player;
    protected ScoreController scoreController;
    
    private GameState gameState;
    
    private TimeController timeController;
    
    private InputMultiplexer inputProcessor;
    
    /** Use Builder to initiate GameController */
    protected GameController() {
    }
    
    /**
     * Getter for the singleton game controller.
     * 
     * @return {@link #instance}
     */
    public static GameController getInstance() {
        if(instance == null) {
            instance = new GameController();
        }
        return instance;
    }
    
    /**
     * Getter for the model batch used to draw the 3d game.
     * 
     * @return ModelBatch
     */
    public ModelBatch getBatch() {
        return batch;
    }
    
    /**
     * Getter for the stage.
     * 
     * @return {@link #stage}
     */
    public Stage getStage() {
        return stage;
    }
    
    /**
     * Getter for the flight controller.
     * 
     * @return {@link #flightController}
     */
    public FlightController getFlightController() {
        return flightController;
    }
    
    /**
     * Getter for the camera.
     * 
     * @return {@link #camera}
     */
    public PerspectiveCamera getCamera() {
        return camera;
    }
    
    /**
     * Setter for the input Processor.
     * 
     * @param inputProcessor
     */
    public void setInputProcessor(InputMultiplexer inputProcessor) {
        this.inputProcessor = inputProcessor;
    }
    
    /**
     * Getter for the level.
     * 
     * @return {@link #level}
     */
    public Level getLevel() {
        return level;
    }
    
    /**
     * Getter for the player.
     * 
     * @return {@link #player}
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * This method is called, while the level is loading. It loads everything
     * the default functions need. Furthermore all optional features in
     * {@link #optionalFeaturesToLoad} are loaded.
     */
    public void loadGame() {
        camera = cameraController.getCamera();
        ParticleController.getInstance().load(camera, batch);
        
        // load features
        for (IFeatureLoad optionalFeature : optionalFeaturesToLoad) {
            optionalFeature.load(this);
        }
    }
    
    /**
     * This method is called, when the level is initialized. It initializes all
     * default functions that are needed in all levels, like render the level.
     * Furthermore all optional features in {@link #optionalFeaturesToInit} are
     * initialized.
     */
    public void initGame() {
        // initialize features
        for (IFeatureInit optionalFeature : optionalFeaturesToInit) {
            optionalFeature.init(this);
        }
        
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(inputProcessor);
        startGame();
        Gdx.app.log("GameController.initGame", "OK HAVE FUN!");
    }
    
    /**
     * Sets the game state to running.
     */
    public void startGame() {
        gameState = GameState.RUNNING;
        timeController.initAndStartTimer(level.getLeftTime());
    }
    
    /**
     * Sets the game state to paused.
     */
    public void pauseGame() {
        gameState = GameState.PAUSED;
        timeController.pause();
    }
    
    /**
     * Sets the game state to finished and ends the game.
     * 
     * @param victory
     *            True, if the player was victorious, false if the time is over
     *            or he is dead.
     */
    public void finishGame(boolean victory) {
        if (victory) {
            gameState = GameState.VICTORY;
        } else {
            gameState = GameState.NO_LIVES;
        }
        
        endGame();
    }
    
    /**
     * Sets the game from paused to running.
     */
    public void resumeGame() {
        gameState = GameState.RUNNING;
        timeController.resume();
    }
    
    /**
     * Getter for the game state.
     * 
     * @return GameState
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Checks if the game is running.
     * 
     * @return true if the game is running, otherwise false.
     */
    public boolean isRunning() {
        return gameState == GameState.RUNNING;
    }
    
    /**
     * Checks if the game is paused.
     * 
     * @return true if the game is paused, otherwise false.
     */
    public boolean isPaused() {
        return gameState == GameState.PAUSED;
    }
    
    /**
     * Checks if the game is over and the player was victorious.
     * 
     * @return true if the game is over and the player was victorious, otherwise
     *         false.
     */
    public boolean isVictory() {
        return gameState == GameState.VICTORY;
    }
    
    /**
     * This method is called every frame. Furthermore all optional features in
     * {@link #optionalFeaturesToRender} are updated and rendered.
     * 
     * @param delta
     *            Time after the last call.
     */
    public void renderGame(float delta) {
        stage.act(delta);
        int len, i;
        
        if (gameState == GameState.RUNNING) {
            // update features if the game is not paused
            
            flightController.update(delta);
            camera = cameraController.updateCamera();
            level.update(delta, camera);
            
            if ((int) level.getLeftTime() <= 0) {
                finishGame(false);
            }
            
            len = optionalFeaturesToUpdate.size();
            for (i = 0; i < len; i++) {
                optionalFeaturesToUpdate.get(i).update(delta);
            }
            
            CollisionDetector.getInstance().perform(delta);
            timeController.checkTime();
        }
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // render features
        batch.begin(camera);
        level.render(delta, batch, camera);
        len = optionalFeaturesToRender.size();
        for (i = 0; i < len; i++) {
            optionalFeaturesToRender.get(i).render(delta);
        }
        ParticleController.getInstance().render();
        batch.end();
        
        // draw features
        len = optionalFeaturesToDraw.size();
        for (i = 0; i < len; i++) {
            optionalFeaturesToDraw.get(i).draw(delta);
        }
        stage.draw();
    }
    
    /**
     * This method is called when the game is over. Furthermore all optional
     * features in {@link #optionalFeaturesToFinish} are finished.
     */
    public void endGame() {
        timeController.pause();
        for (IFeatureFinish optionalFeature : optionalFeaturesToFinish) {
            optionalFeature.finish();
        }
    }
    
    /**
     * This method is called when the game is over. Furthermore all optional
     * features in {@link #optionalFeaturesToDispose} are disposed.
     */
    public void disposeGame() {
        // dispose features
        for (IFeatureDispose optionalFeature : optionalFeaturesToDispose) {
            // Gdx.app.log("GameController.disposeGame", "dispose: " +
            // optionalFeature.getClass().getSimpleName());
            optionalFeature.dispose();
        }
        CollisionDetector.getInstance().dispose();
        
        optionalFeaturesToLoad.clear();
        optionalFeaturesToInit.clear();
        optionalFeaturesToUpdate.clear();
        optionalFeaturesToRender.clear();
        optionalFeaturesToDraw.clear();
        optionalFeaturesToDispose.clear();
        optionalFeaturesToFinish.clear();
    }
    
    /**
     * Setter for the time controller.
     * 
     * @param timeController
     *            The time controller to use.
     */
    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
        timeController.registerTimeIsUpListener(this);
        
    }
    
    /**
     * Getter for the time controller.
     * 
     * @return {@link #timeController}
     */
    public TimeController getTimeController() {
        return timeController;
    }
    
    /**
     * Getter for the score controller.
     * 
     * @return {@link #scoreController}
     */
    public ScoreController getScoreController() {
        return scoreController;
    }
    
    @Override
    public boolean timeIsUp() {
        gameState = GameState.TIME_OVER;
        endGame();
        return true;
    }
}

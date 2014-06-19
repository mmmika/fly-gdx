package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.I18n;


/**
 * Displays the main menu with Start, Options, Help and Exit buttons.
 * 
 * @author Tobias Zangl
 */
public class MainMenuScreen extends BasicScreen {

	/**
	 * Adds the main menu to the main menu screen.
	 * <p>
	 * Includes buttons for Start, Options, Help, Exit.
	 */
	protected void generateContent() {
		Table table = new Table();
		table.pad(UI.Window.SINGLE_SPACE_HEIGHT, UI.Window.SINGLE_SPACE_WIDTH, UI.Window.SINGLE_SPACE_HEIGHT, UI.Window.SINGLE_SPACE_WIDTH).setFillParent(true);
		stage.addActor(table);

		final TextButton continueButton = new TextButton(I18n.t("continue"), skin, "default");
		final TextButton chooseLevelButton = new TextButton(I18n.t("choose.level"), skin, "default");
		final TextButton optionButton = new TextButton(I18n.t("settings"), skin, "default");
		final TextButton statsButton = new TextButton("Statistics", skin, "default");

		table.row().expand();
		table.add(continueButton).fill().pad(UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH, UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH).colspan(3);
		table.row().expand();
		table.add(chooseLevelButton).fill().pad(UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH, UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH).uniform();
		table.add(optionButton).fill().pad(UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH, UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH).uniform();
		table.add(statsButton).fill().pad(UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH, UI.SmallButtons.SPACE_HEIGHT, UI.SmallButtons.SPACE_WIDTH).uniform();
		table.row().expand();
		
		
		chooseLevelButton.addListener(new ClickListener() {
			@Override 
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).setLevelChoosingScreen();
			}
		});
		
		continueButton.addListener(new ClickListener() {
			@Override 
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).getLoader().continueLevel();
			}
		});
		
		optionButton.addListener(new ClickListener() {
			@Override 
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).setSettingScreen();
			}
		});
		
		statsButton.addListener(new ClickListener() {
			@Override 
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).setStatisticsScreen();
			}
		});
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
		Gdx.app.getApplicationListener().dispose();
		Gdx.app.exit();
	}
}

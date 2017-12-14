package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameScreen implements Screen {
	
	private OrthographicCamera camera;
	private int zoom = 0;
	private SpriteBatch batch;
	
	private Player mainPlayer;
	private int curLevel;
	
	public GameScreen(GameCore game) {
		batch = game.getBatch();
		game.setGameScreen(this);
		
		createWorld();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	private void createWorld() {
		Level.resetLevels();
		curLevel = 0;
		
		mainPlayer = new Player();
		
		Level level = Level.getLevel(curLevel);
		level.addEntity(mainPlayer);
		
		Tile spawnTile;
		do spawnTile = level.getTile(
			MathUtils.random(level.getWidth()-1),
			MathUtils.random(level.getHeight()-1)
		);
		while(spawnTile == null || !spawnTile.getType().maySpawn);
		
		mainPlayer.moveTo(spawnTile);
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public void render(float delta) {
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		mainPlayer.checkInput(delta);
		Level.getLevel(curLevel).update(delta);
		
		if(Gdx.input.isKeyJustPressed(Keys.MINUS))
			zoom(-1);
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS))
			zoom(1);
		
		Level level = Level.getLevel(curLevel);
		
		Vector2 playerPos = new Vector2();
		mainPlayer.getBounds().getCenter(playerPos);
		float viewWidth = camera.viewportWidth;
		float viewHeight = camera.viewportHeight;
		int lvlWidth = level.getWidth() * Tile.SIZE;
		int lvlHeight = level.getHeight() * Tile.SIZE;
		playerPos.x = MathUtils.clamp(playerPos.x, Math.min(viewWidth/2, lvlWidth/2), Math.max(lvlWidth/2, lvlWidth-viewWidth/2));
		playerPos.y = MathUtils.clamp(playerPos.y, Math.min(viewHeight/2, lvlHeight/2), Math.max(lvlHeight/2, lvlHeight-viewHeight/2));
		camera.position.set(playerPos, camera.position.z);
		camera.update(); // updates the camera "matrices"
		
		Rectangle renderSpace = new Rectangle(camera.position.x - viewWidth/2, camera.position.y - viewHeight/2, viewWidth, viewHeight);
		//Rectangle renderSpace = new Rectangle(0, 0, lvlWidth, lvlHeight);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		level.render(renderSpace, batch, delta);
		// TO-DO render GUI here
		batch.end();
	}
	
	private void zoom(int dir) {
		zoom += dir;
		
		double zoomFactor = Math.pow(2, dir);
		camera.viewportHeight /= zoomFactor;
		camera.viewportWidth /= zoomFactor;
	}
	
	@Override public void resize(int width, int height) {
		float zoomFactor = (float) Math.pow(2, zoom);
		camera.setToOrtho(false, width/zoomFactor, height/zoomFactor);
	}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
}

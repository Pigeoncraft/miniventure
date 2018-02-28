package miniventure.game;

import java.util.HashSet;

import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.Chunk;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class LevelManager {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	private boolean worldLoaded = false;
	
	private LevelGenerator levelGenerator;
	
	private Player mainPlayer;
	private float gameTime;
	
	//private Tile spawnTile = null;
	private final HashSet<WorldObject> keepAlives = new HashSet<>(); // always keep chunks around these objects loaded.
	
	LevelManager() {
		
	}
	
	boolean worldLoaded() { return worldLoaded; }
	
	void updateAndRender(GameScreen game, MenuScreen menu) {
		if(!worldLoaded || mainPlayer == null) return;
		
		Level level = mainPlayer.getLevel();
		if(level == null) {
			if(!(menu instanceof RespawnScreen))
				GameCore.setScreen(new RespawnScreen());
			return;
		}
		
		// render if no menu, or menu that has part of screen (update camera if update or multiplayer)
		// check input if no menu
		// update if no menu, or multiplayer
		
		if(menu == null)
			game.handleInput(mainPlayer);
		
		boolean update = menu == null; // later add "|| multiplayer";
		
		if(menu == null || !menu.usesWholeScreen()) {
			game.render(mainPlayer, TimeOfDay.getTimeOfDay(gameTime).getSkyColors(gameTime), level);
		}
		
		if(update) {
			level.update(Gdx.graphics.getDeltaTime());
			gameTime += Gdx.graphics.getDeltaTime();
		}
	}
	
	String getTimeOfDayString() {
		return TimeOfDay.getTimeOfDay(gameTime).getTimeString(gameTime);
	}
	
	public void createWorld(int width, int height) {
		worldLoaded = false;
		LoadingScreen loadingScreen = new LoadingScreen();
		GameCore.setScreen(loadingScreen);
		gameTime = 0;
		
		levelGenerator = new LevelGenerator(MathUtils.random.nextLong(), width, height, 32, 6);
		
		new Thread(() -> {
			Level.resetLevels(loadingScreen, levelGenerator);
			respawn();
			//noinspection ConstantConditions
			Tile spawnTile = mainPlayer.getLevel().getClosestTile(mainPlayer.getBounds());
			if (spawnTile != null) {
				mainPlayer.moveTo(spawnTile);
				keepAlives.add(spawnTile);
			}
			worldLoaded = true;
			Gdx.app.postRunnable(() -> GameCore.setScreen(null));
		}).start();
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	public void exitToMenu() { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		worldLoaded = false;
		keepAlives.clear();
		mainPlayer = null;
		Level.clearLevels();
		levelGenerator = null;
		//spawnTile = null;
		GameCore.setScreen(new MainMenu());
	}
	
	public void respawn() {
		if(mainPlayer != null) mainPlayer.remove();
		keepAlives.remove(mainPlayer);
		mainPlayer = new Player();
		keepAlives.add(mainPlayer);
		
		Level level = Level.getLevel(0);
		
		if(level == null)
			throw new NullPointerException("Surface level found to be null while attempting to respawn player.");
		
		// find a good spawn location near the middle of the map
		
		Rectangle spawnBounds = new Rectangle(0, 0, Math.min(level.getWidth(), 5*Chunk.SIZE), Math.min(level.getHeight(), 5*Chunk.SIZE));
		spawnBounds.setCenter(level.getWidth()/2, level.getHeight()/2);
		
		level.spawnMob(mainPlayer, spawnBounds);
	}
	
	public boolean isKeepAlive(WorldObject obj) {
		return keepAlives.contains(obj);
	}
	
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> keepAlives = new Array<>();
		for(WorldObject obj: this.keepAlives)
			if(obj.getLevel() == level)
				keepAlives.add(obj);
		
		return keepAlives;
	}
}

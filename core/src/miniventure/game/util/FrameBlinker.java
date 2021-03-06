package miniventure.game.util;

public class FrameBlinker implements Blinker {
	
	private final int framesOn;
	private final int framesOff;
	private final boolean startOn;
	
	private int frames;
	
	public FrameBlinker(int framesOn, int framesOff, boolean startOn) {
		this.framesOn = framesOn;
		this.framesOff = framesOff;
		this.startOn = startOn;
	}
	
	@Override
	public void update(float delta) {
		frames++;
	}
	
	@Override
	public boolean shouldRender() {
		int curFrame = frames % (framesOn + framesOff);
		
		if(startOn)
			return curFrame < framesOn;
		else
			return curFrame >= framesOff;
	}
	
	@Override
	public void reset() {
		frames = 0;
	}
}

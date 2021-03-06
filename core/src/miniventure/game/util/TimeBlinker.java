package miniventure.game.util;

public class TimeBlinker implements Blinker {
	
	private final float timeOn;
	private final float timeOff;
	private final boolean startOn;
	
	private float time;
	
	public TimeBlinker(float timeOn, float timeOff, boolean startOn) {
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.startOn = startOn;
	}
	
	@Override
	public void update(float delta) {
		time += delta;
	}
	
	@Override
	public boolean shouldRender() {
		float curTime = time % (timeOn + timeOff);
		
		if(startOn)
			return curTime < timeOn;
		else
			return curTime >= timeOff;
	}
	
	@Override
	public void reset() {
		time = 0;
	}
}

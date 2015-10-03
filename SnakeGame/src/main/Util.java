package main;

public class Util {
	public static int randomIntInRange(int lowerBound, int upperBound) {
		if(upperBound < lowerBound) throw new IllegalArgumentException("lowerBound must be smaller than upperBound");
		if(upperBound == lowerBound) return upperBound;
		double rand = Math.random();
		rand = rand*(upperBound-lowerBound);
		rand += lowerBound;
		float r = (float)rand;
		return Math.round(r);
	}
	
	public static String millisToTime(float millis) {
		float secs = millis/1000;
		int mins = (int) (secs/60);
		secs -= mins*60;
		secs = roundToAccuracy(secs, 0.1F);
		String time = mins+":"+secs;
		if(time.length()>time.lastIndexOf('.')+2){ // has excess zeros
			time = time.substring(0, time.lastIndexOf('.')+2);
		}
		return time;
	}

	private static float roundToAccuracy(float n, float i) {
		int cutoffN = (int) (n/i);
		return cutoffN*i;
	}
	
	public static int coordsToField(int x, int y) {
		return y*Game.GRID_SIZE_X + x;
	}
	
	public static int[] fieldToCoords(int field) {
		int x = field%Game.GRID_SIZE_X;
		int y = field/Game.GRID_SIZE_X;
		return new int[]{x,y};
	}
}

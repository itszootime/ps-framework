package org.uncertweb.util;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class DurationFormatter {
	
	public static String format(long durationMillis) {
		// calculate
		long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) - TimeUnit.HOURS.toMinutes(hours);
		double seconds = (durationMillis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes)) / 1000.0;
		
		// print
		StringBuilder formatted = new StringBuilder();
		if (hours > 0) {
			formatted.append(hours + "h");
		}
		if (minutes > 0) {
			formatted.append(minutes + "m");
		}
		if (hours == 0 && seconds != 0) {
			if (minutes > 0) {
				// forget about ms
				formatted.append(new DecimalFormat("##").format(seconds));
			}
			else if (seconds >= 1) {
				// round 2 digits ms
				formatted.append(new DecimalFormat("##.##").format(seconds));
			}
			else {
				// all ms
				formatted.append(seconds);
			}
			formatted.append("s");
		}
		return formatted.toString();
	}

}

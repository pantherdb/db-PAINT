package org.paint.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DuplicationColor {
	private static DuplicationColor dup_color;
	private List<Color> pastelColors = new ArrayList<Color>();
	private static Logger log = Logger.getLogger(DuplicationColor.class);
	private int color_index;

	public DuplicationColor() {
		color_index = 0;
		// may need to add more colors for large families...
		pastelColors.add(Color.white);
		pastelColors.add(new Color(255, 182, 193)); // light pink
		pastelColors.add(new Color(238, 232, 170)); // pale goldenrod
		pastelColors.add(new Color(135, 206, 250)); // light sky blue
		pastelColors.add(new Color(240, 128, 128)); // light coral
		pastelColors.add(new Color(152, 251, 152)); // pale green
		pastelColors.add(new Color(216, 191, 216)); // thistle
		pastelColors.add(new Color(240, 230, 140)); // khaki
		pastelColors.add(new Color(224, 255, 255)); // light cyan
		pastelColors.add(new Color(255, 218, 185)); // peach puff
		pastelColors.add(new Color(211, 211, 211)); // light gray
		pastelColors.add(new Color(255, 250, 205)); // lemon chiffon
		pastelColors.add(new Color(176, 196, 222)); // light steel blue
		pastelColors.add(new Color(255, 228, 173)); // moccasin
		pastelColors.add(new Color(175, 238, 238)); // pale turquoise
		pastelColors.add(new Color(244, 164, 96)); // sandy brown
		pastelColors.add(new Color(127, 255, 212)); // aquamarine
		pastelColors.add(new Color(245, 222, 179)); // wheat
		pastelColors.add(new Color(255, 160, 122)); // light salmon
		pastelColors.add(new Color(221, 160, 221)); // plum
		pastelColors.add(new Color(212, 202, 154));
		pastelColors.add(new Color(167, 174, 97));
		pastelColors.add(new Color(122, 159, 162));
		pastelColors.add(new Color(192, 151, 78));
		pastelColors.add(new Color(212, 217, 180));
		pastelColors.add(new Color(184, 209, 151));
		pastelColors.add(new Color(155, 222, 199));
		pastelColors.add(new Color(227, 255, 243));
		pastelColors.add(new Color(126, 130, 122));
		pastelColors.add(new Color(227, 205, 164));
		pastelColors.add(new Color(65, 204, 180));
		pastelColors.add(new Color(255, 124, 93));
		pastelColors.add(new Color(255, 234, 149));
		pastelColors.add(new Color(125, 153, 148));
		pastelColors.add(new Color(112, 153, 92));
		pastelColors.add(new Color(204, 194, 143));
		pastelColors.add(new Color(192, 234, 149));
		pastelColors.add(new Color(245, 211, 145));
		pastelColors.add(new Color(179, 204, 159));
		pastelColors.add(new Color(67, 168, 151));
		pastelColors.add(new Color(255, 150, 102));
		pastelColors.add(new Color(255, 225, 132));
		pastelColors.add(new Color(245, 233, 190));
		pastelColors.add(new Color(238, 242, 114));
		pastelColors.add(new Color(103, 191, 71));
		pastelColors.add(new Color(119, 207, 242));
		pastelColors.add(new Color(122, 191, 152));
		pastelColors.add(new Color(232, 201, 128));
		pastelColors.add(new Color(86, 255, 219));
		pastelColors.add(new Color(255, 169, 153));
		pastelColors.add(new Color(72, 212, 198));
		pastelColors.add(new Color(255, 190, 140));
	}

	public static DuplicationColor inst() {
		if (dup_color == null) {
			dup_color = new DuplicationColor();
		}
		return dup_color;
	}

	public Color getDupColor(int dupColorIndex) {
		return pastelColors.get(dupColorIndex);
	}

	public void initColorIndex() {
		color_index = 0;
	}

	public int getNextIndex() {
		++color_index;
		if (color_index >= 0 && color_index < pastelColors.size()) {
			return color_index;
		}
		else {
			int next = color_index % pastelColors.size();
			log.debug("reguested index=" + color_index + " only " + pastelColors.size() + " colors available, using " + next);
			return next;
		}
	}

}

package org.paint.gui.table;

import java.awt.Color;

import org.apache.log4j.Logger;

public class OrthoCell {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(OrthoCell.class);

	private Color program_color;
	private String program_name;
	
	public OrthoCell(Color program_color, String program_name) {
		this.program_color = program_color;
		this.program_name = program_name;
	}

	public Color getProgramColor() {
		return program_color;
	}

	public void setProgramColor(Color program_color) {
		this.program_color = program_color;
	}

	public String getProgramName() {
		return program_name;
	}

	public void setProgramName(String program_name) {
		this.program_name = program_name;
	}

}

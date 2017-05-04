package org.paint.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class CustomPreviewPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private int squareSize = 25;
	private int squareGap = 5;
	private int innerGap = 5;
	private int textGap = 5;
	private String sampleText = UIManager.getString("ColorChooser.sampleText");
	private int swatchWidth = 50;
	private Color oldColor = null;
	private JComponent host;
	
	public CustomPreviewPanel(JColorChooser host) {
		this.host = host;
	}
	
	public Dimension getPreferredSize() {
		FontMetrics fm = host.getFontMetrics(getFont());
		int height = fm.getHeight();
		int width = SwingUtilities.computeStringWidth(fm, sampleText);
		int y = height*3 + textGap*3;
		int x = squareSize * 3 + squareGap*2 + swatchWidth + width + textGap*3;
		return new Dimension( x,y );
	}
	
	public void paintComponent(Graphics g) {
		if (oldColor == null)
			oldColor = getForeground();
		g.setColor(getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		if (this.getComponentOrientation().isLeftToRight()) {
			int squareWidth = paintSquares(g, 0);
			int textWidth = paintText(g, squareWidth);
			paintSwatch(g, squareWidth + textWidth);
		} else {
			int swatchWidth = paintSwatch(g, 0);
			int textWidth = paintText(g, swatchWidth);
			paintSquares(g , swatchWidth + textWidth);
		}
	}

	public void setOldColor(Color oldColor) {
		this.oldColor = oldColor;
	}
	
	private int paintSwatch(Graphics g, int offsetX) {
		int swatchX = offsetX;
		g.setColor(oldColor);
		g.fillRect(swatchX, 0, swatchWidth, (squareSize) + (squareGap/2));
		g.setColor(getForeground());
		g.fillRect(swatchX, (squareSize) + (squareGap/2), swatchWidth, (squareSize) + (squareGap/2) );
		return (swatchX+swatchWidth);
	}

	private int paintText(Graphics g, int offsetX) {
		g.setFont(getFont());
		if (host == null) {
			host = this;
		}
		FontMetrics fm = host.getFontMetrics(getFont());
		int ascent = fm.getAscent();
		int height = fm.getHeight();
		int width = SwingUtilities.computeStringWidth(fm, sampleText);
		int textXOffset = offsetX + textGap;
		Color color = getForeground();
		g.setColor(color);
		g.drawString(sampleText,textXOffset+(textGap/2), ascent+2);
		g.fillRect(textXOffset,
				( height) + textGap,
				width + (textGap),
				height +2);
		g.setColor(Color.black);
		g.drawString(sampleText, textXOffset+(textGap/2), height+ascent+textGap+2);
		g.setColor(Color.white);
		g.fillRect(textXOffset,
				( height + textGap) * 2,
				width + (textGap),
				height +2);
		g.setColor(color);
		g.drawString(sampleText, textXOffset+(textGap/2), ((height+textGap) * 2)+ascent+2);
		return width + textGap*3;
	}
	
	private int paintSquares(Graphics g, int offsetX) {
		int squareXOffset = offsetX;
		Color color = getForeground();
		g.setColor(Color.white);
		g.fillRect(squareXOffset,0,squareSize,squareSize);
		g.setColor(color);
		g.fillRect(squareXOffset+innerGap,
				innerGap,
				squareSize - (innerGap*2),
				squareSize - (innerGap*2));
		g.setColor(Color.white);
		g.fillRect(squareXOffset+innerGap*2,
				innerGap*2,
				squareSize - (innerGap*4),
				squareSize - (innerGap*4));
		g.setColor(color);
		g.fillRect(squareXOffset,squareSize+squareGap,squareSize,squareSize);
		g.translate(squareSize+squareGap, 0);
		g.setColor(Color.black);
		g.fillRect(squareXOffset,0,squareSize,squareSize);
		g.setColor(color);
		g.fillRect(squareXOffset+innerGap,
				innerGap,
				squareSize - (innerGap*2),
				squareSize - (innerGap*2));
		g.setColor(Color.white);
		g.fillRect(squareXOffset+innerGap*2,
				innerGap*2,
				squareSize - (innerGap*4),
				squareSize - (innerGap*4));
		g.translate(-(squareSize+squareGap), 0);
		g.translate(squareSize+squareGap, squareSize+squareGap);
		g.setColor(Color.white);
		g.fillRect(squareXOffset,0,squareSize,squareSize);
		g.setColor(color);
		g.fillRect(squareXOffset+innerGap,
				innerGap,
				squareSize - (innerGap*2),
				squareSize - (innerGap*2));
		g.translate(-(squareSize+squareGap), -(squareSize+squareGap));
		g.translate((squareSize+squareGap)*2, 0);
		g.setColor(Color.white);
		g.fillRect(squareXOffset,0,squareSize,squareSize);
		g.setColor(color);
		g.fillRect(squareXOffset+innerGap,
				innerGap,
				squareSize - (innerGap*2),
				squareSize - (innerGap*2));
		g.setColor(Color.black);
		g.fillRect(squareXOffset+innerGap*2,
				innerGap*2,
				squareSize - (innerGap*4),
				squareSize - (innerGap*4));
		g.translate(-((squareSize+squareGap)*2), 0);
		g.translate((squareSize+squareGap)*2, (squareSize+squareGap));
		g.setColor(Color.black);
		g.fillRect(squareXOffset,0,squareSize,squareSize);
		g.setColor(color);
		g.fillRect(squareXOffset+innerGap,
				innerGap,
				squareSize - (innerGap*2),
				squareSize - (innerGap*2));
		g.translate(-((squareSize+squareGap)*2), -(squareSize+squareGap));
		return (squareSize*3+squareGap*2);
	}
}
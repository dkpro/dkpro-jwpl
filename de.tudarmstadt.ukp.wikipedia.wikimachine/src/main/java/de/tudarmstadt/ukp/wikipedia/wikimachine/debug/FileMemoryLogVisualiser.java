/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class FileMemoryLogVisualiser {

	private static final int OUTPUT_HEIGHT = 768;
	private static final int OUTPUT_WIDTH = 1024;
	private static final String OUTPUT_FILE = "output.jpg";
	private static final int OUTPUT_QUALITY = 10;

	private XYSeriesCollection series = new XYSeriesCollection();

	private void readFile(String fileName, String alias) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String currentLine = reader.readLine();

		XYSeries totalSeries = new XYSeries(alias + " total");
		XYSeries usedSeries = new XYSeries(alias + " used");
		Integer counter = 0;

		while ((currentLine = reader.readLine()) != null) {
			String[] currentLineItems = currentLine.split(",");
			try {
				Long totalMemory = Long.parseLong(currentLineItems[1]
						.split("\"")[1]);
				Long freeMemory = Long.parseLong(currentLineItems[2]
						.split("\"")[1]);

				totalSeries.add(counter, totalMemory);
				usedSeries.add(counter++, new Long(totalMemory - freeMemory));
			} catch (Exception e) {
				System.out.print("INFO: line ignored ");
				System.out.println(currentLine);
			}
		}

		series.addSeries(totalSeries);
		series.addSeries(usedSeries);

	}

	private BufferedImage draw(JFreeChart chart, int width, int height) {
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();

		chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));

		g2.dispose();
		return img;
	}

	private void saveToFile(JFreeChart chart, String aFileName, int width,
			int height, double quality) throws IOException {
		BufferedImage img = draw(chart, width, height);

		FileOutputStream fos = new FileOutputStream(aFileName);
		JPEGImageEncoder encoder2 = JPEGCodec.createJPEGEncoder(fos);
		JPEGEncodeParam param2 = encoder2.getDefaultJPEGEncodeParam(img);
		param2.setQuality((float) quality, true);
		encoder2.encode(img, param2);
		fos.flush();
		fos.close();
	}

	private void createChart() throws IOException {

		JFreeChart chart = ChartFactory.createXYLineChart("Memory logs",
				"Step", "Byte", series, PlotOrientation.VERTICAL, true, false,
				false);
		saveToFile(chart, OUTPUT_FILE, OUTPUT_WIDTH, OUTPUT_HEIGHT,
				OUTPUT_QUALITY);
	}

	public FileMemoryLogVisualiser(String args[]) throws IOException {
		if (args.length % 2 == 0 && args.length >= 2) {
			for (int i = 0; i < args.length / 2; i++) {
				readFile(args[i * 2], args[i * 2 + 1]);
			}
			createChart();
		} else {
			System.out
					.println("Use java -jar LogVisualiser <filename0> <alias0> <filename1> <alias1> ...");
		}
	}

	public static void main(String[] args) {
		try {
			new FileMemoryLogVisualiser(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package com.group8.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BeerScanner extends JFrame implements Runnable, ThreadFactory, WebcamImageTransformer {

	private static final long serialVersionUID = 6441489157408381878L;
	private static final JHGrayFilter GRAY = new JHGrayFilter();
	private Executor executor = Executors.newSingleThreadExecutor(this);
	private static Webcam webcam = null;
	protected WebcamPanel panel = null;
	protected JTextArea textarea = new JTextArea();
	private static Result result;
	private Thread t;

	/**
	 * Created by Mantas Namgaudis.
	 *
	 * Gets called when the user presses the "Beer scan" button
	 * in Home window.
	 *
	 * This class uses external libraries to open the webcam on
	 * users computer, scan and read provided barcode.
	 */

	public BeerScanner() {
		super();

		Dimension size = WebcamResolution.QVGA.getSize();
		webcam = webcam.getWebcams().get(0);
		webcam.setViewSize(size);
		webcam.setImageTransformer(this);
		panel = new WebcamPanel(webcam);
		panel.setPreferredSize(size);

		textarea = new JTextArea();
		textarea.setEditable(false);
		textarea.setPreferredSize(size);

		executor.execute(this);
	}

	@Override
	public void run(){

		boolean running = true;

		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = null;
			BufferedImage image = null;
			if (webcam.isOpen()) {
				if ((image = webcam.getImage()) == null) {
					continue;
				}
				LuminanceSource source = new BufferedImageLuminanceSource(image);
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				try {
					result = new MultiFormatReader().decode(bitmap);
				} catch (NotFoundException e) {
					// fall through, it means there is no QR code in image
				}
			}
			if (result != null){
				textarea.setText(result.getText());
				HomeCenter.setBarcode(result.getText());
				//HomeCenter.webcamLabel.setText(result.getText().toString());


				HomeCenter.checkIfbarcodeIsSet();




				// Stop the thread
				if(t != null){

					try {
						running = false;
						t.join();
					}
					catch (InterruptedException e){
					}
				}



			}
		} while (running);
	}


	@Override
	public Thread newThread(Runnable r) {
		t = new Thread(r, "BeerScanner-thread");
		t.setDaemon(false); // originally was set to true
		return t;
	}

	/**
	 * Adds a gray filter on webcam for better picture contrast.
	 *
	 * @param bufferedImage
	 * @return
     */
	@Override
	public BufferedImage transform(BufferedImage bufferedImage) {
		return GRAY.filter(bufferedImage, null);
	}

	/**
	 * Closes the webcam in a way depending on users OS.
	 */
	public static void disconnectWebcam(){
		if(System.getProperty("os.name").equals("Mac OS X")) {
			webcam.getDevice().close();
		}
		// Close the webcam service
		webcam.close();
	}

}
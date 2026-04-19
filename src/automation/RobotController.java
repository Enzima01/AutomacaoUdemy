package automation;

import java.awt.Robot;
import java.awt.event.InputEvent;

public class RobotController {

	private Robot robot;

	public RobotController() throws Exception {
		robot = new Robot();
	}

	public void clicar(int x, int y) {
		robot.mouseMove(x, y);
		robot.delay(200);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	public void esperar(long ms) throws InterruptedException {
		Thread.sleep(ms);
	}
}
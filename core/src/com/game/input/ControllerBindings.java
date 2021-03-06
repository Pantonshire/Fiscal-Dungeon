package com.game.input;

import com.badlogic.gdx.controllers.Controller;

public class ControllerBindings {

	public static int[] getBindings(Controller controller) {
		String name = controller.getName();
		if(name.equals("Wireless Controller")) { return new int[] { 9, 4, 5 }; }
		else if(name.contains("Xbox 360")) { return new int[] { 4, 8, 9 }; }
		
		return new int[] { -1, -1 };
	}
	
	public static int[] getAnalogueSticks(Controller controller) {
		String name = controller.getName();
		if(name.equals("Wireless Controller")) { return new int[] { 0, 1, 2, 3 }; }
		else if(name.contains("Xbox 360")) { return new int[] { 0, 1, 2, 3 }; }
		
		return new int[] { 0, 1, 2, 3 };
	}
}

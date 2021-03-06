package com.game.level;

import com.game.Main;
import com.game.graphics.LayerRenderer;
import com.game.run.Run;
import com.game.spells.SpellUpgrade;
import com.game.spells.WarpSpell;
import com.game.upgrades.UpgradeList;

public class LevelFactory {

	public static final int NUM_FLOORS = 8;
	public static final int
	EASY = 0,
	BOSS_1 = 1,
	NORMAL = 2,
	BOSS_2 = 3,
	HARD = 4,
	BOSS_3 = 5,
	MADNESS = 6,
	BOSS_4 = 7;

	public static int floor = 4;
	private static final int[] SIZES = new int[] { 100, 100, 170, 100, 200, 100, 240, 100 };

	public static void firstFloor(LayerRenderer game, LayerRenderer overlay, LayerRenderer fonts) {
		floor = EASY;
		Run.newRun(new WarpSpell());
		Run.currentRun.spell.upgrade(SpellUpgrade.CONTROL);
		Run.currentRun.spell.upgrade(SpellUpgrade.CONTROL);
		Run.currentRun.spell.upgrade(SpellUpgrade.CONTROL);
//		Run.currentRun.collectUpgrade(UpgradeList.BOUNCY_ARROW);
//		Run.currentRun.collectUpgrade(UpgradeList.KLOBB_QUIVER);
		Run.currentRun.collectUpgrade(UpgradeList.HOMING_ARROW);
		Level nextFloor = new Level(game, overlay, fonts, SIZES[floor], SIZES[floor], floor % 2 != 0);
		Main.nextLevel = nextFloor;
	}

	public static void nextFloor(Level current) {
		if(nextFloorExists()) {
			++floor;
			Level nextFloor = new Level(current.gameRenderer, current.overlayRenderer, current.fontRenderer, SIZES[floor], SIZES[floor], floor % 2 != 0);
			Main.nextLevel = nextFloor;
		}

		else {
			Main.toCongratulesScreen();
		}
	}

	public static boolean nextFloorExists() {
		return floor < NUM_FLOORS - 1;
	}

	public static String getFloorName() {
		switch(floor) {
		case EASY:
			return "dungeon entrance";
		case NORMAL:
			return "dungeon depths";
		case HARD:
			return "treasure trove";
		case MADNESS:
			return "madness";
		case BOSS_1: case BOSS_2: case BOSS_3: case BOSS_4:
			return "boss fight!";
		default:
			return "you shouldn\'t see this message";
		}
	}

	public static String getTileset() {
		switch(floor) {
		case EASY: case BOSS_1:
			return "tilemap";
		case NORMAL: case BOSS_2:
			return "tilemap_2";
		case HARD: case BOSS_3:
			return "tilemap_3";
		case MADNESS: case BOSS_4:
			return "tilemap_4";
		default:
			return "tilemap";
		}
	}
}

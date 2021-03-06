package com.game.run;

import java.util.ArrayList;

import com.game.audio.SoundEffects;
import com.game.entities.Player;
import com.game.level.Level;
import com.game.spells.Spell;
import com.game.upgrades.Upgrade;

public class Run {

	public static Run currentRun;

	public static void newRun(Spell spell) {
		currentRun = new Run(spell);
	}
	
	public Spell spell;
	
	private int coins;
	private int maxCoins;
	private boolean dead;
	private ArrayList<Upgrade> upgrades;

	private Run(Spell spell) {
		coins = 0;
		maxCoins = 100;
		dead = false;
		this.spell = spell;
		upgrades = new ArrayList<Upgrade>();
	}

	public void collectCoins(Level level, int amount) {
		coins += amount;
		if(coins >= maxCoins) {
			killPlayers(level);
		}

		if(coins < 0) {
			coins = 0;
		}
	}
	
	public int getCoins() {
		return coins;
	}
	
	public int getMaxCoins() {
		return maxCoins;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void killPlayers(Level level) {
		dead = true;
		coins = 0;
		level.startGameOverTimer();
		SoundEffects.instance.play("blast", 1, 1, 0);
		ArrayList<Player> players = level.getPlayers();
		for(Player player : players) {
			player.explode();
		}
	}
	
	public void collectUpgrade(Upgrade upgrade) {
		upgrades.add(upgrade);
	}
	
	public boolean hasUpgrade(Upgrade upgrade) {
		return upgrades.contains(upgrade);
	}
	
	public ArrayList<Upgrade> getUpgrades() {
		return upgrades;
	}
	
	public boolean hasPrerequisites(Upgrade upgrade) {
		Upgrade[] prerequisites = upgrade.getPrerequisites();
		for(Upgrade prerequisite : prerequisites) {
			if(!upgrades.contains(prerequisite)) {
				return false;
			}
		}
		
		return true;
	}
}

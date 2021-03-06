package com.game.spells;

import java.util.ArrayList;

import com.game.audio.SoundEffects;
import com.game.entities.Coin;
import com.game.entities.Player;
import com.game.entities.SparkParticle;
import com.game.level.Level;
import com.game.vector.Vector;
import static com.game.spells.SpellUpgrade.*;

public class RepelSpell extends Spell {

	public RepelSpell() {
		super(1000, 90, REPEL_RADIUS, REPEL_FORCE, REPEL_DAMAGE);
	}

	protected void onUsed(Level level, Player player) {
		SoundEffects.instance.play("magic", 1, 1, 0);
		Vector position = player.getPosition();
		for(int i = 0; i < 40; i++) { level.spawn(new SparkParticle("repel_particle", level, position.x, position.y, Math.PI * 2 / 40 * i, 4, 40)); }
		ArrayList<Coin> coins = level.getCoins();
		for(Coin coin : coins) {
			if(position.distBetween(coin.getPosition()) < 160) {
				double initialSpeed = coin.getVelocity().magnitude();
				double speed = Math.max(initialSpeed, 5);
				coin.push(position.angleBetween(coin.getPosition()), speed, initialSpeed, 0.1);
			}
		}
	}
}

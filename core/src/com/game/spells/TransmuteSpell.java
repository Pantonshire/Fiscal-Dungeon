package com.game.spells;

import java.util.ArrayList;

import com.game.audio.SoundEffects;
import com.game.entities.Coin;
import com.game.entities.Player;
import com.game.entities.SparkParticle;
import com.game.level.Level;
import com.game.vector.Vector;

public class TransmuteSpell extends Spell {

	public TransmuteSpell() {
		super(4000, 90);
	}

	protected void onUsed(Level level, Player player) {
		SoundEffects.instance.play("magic", 1, 1, 0);
		Vector position = player.getPosition();
		for(int i = 0; i < 40; i++) { level.spawn(new SparkParticle("fireball_particle_yellow", level, position.x, position.y, Math.PI * 2 / 40 * i, 4, 40)); }
		ArrayList<Coin> coins = level.getCoins();
		for(Coin coin : coins) {
			if(coin.isOnScreen(level.gameRenderer)) {
				coin.destroy();
//				world.spawn(new Fireball(world, coin.getPosition().x, coin.getPosition().y, RandomUtils.randAngle(), RandomUtils.randDouble(6, 10)));
			}
		}
	}
}

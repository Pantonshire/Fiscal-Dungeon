package com.game.entities;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.game.audio.SoundEffects;
import com.game.entities.projectiles.Arrow;
import com.game.graphics.Animation;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Sequence;
import com.game.graphics.Textures;
import com.game.input.Action;
import com.game.input.Input;
import com.game.level.Level;
import com.game.light.LevelLightManager;
import com.game.light.LightSource;
import com.game.run.Run;
import com.game.upgrades.UpgradeList;
import com.game.upgrades.UpgradeProperties;
import com.game.utils.AngleHelper;
import com.game.utils.RandomUtils;
import com.game.vector.Vector;

public class Player extends EntityLiving implements LightSource {

	public static final byte
	PLAYER_1 = 0,
	PLAYER_2 = 1,
	PLAYER_3 = 2,
	PLAYER_4 = 3;
	
	private static final double ARM_ROTATE_SPEED = Math.toRadians(8);

	private final byte id;
	private Animation animation;
	private Animation bow;
	private double armRotation;
	private double lookAngle;
	private int facing;
	private int shootTimer;
	private int magicTimer;
	private int mana;
	private int maxMana;
	private int manaRechargeCooldown;
	private boolean invisible;

	private double aimAssist = Math.toRadians(25);

	public Player(Level level, double x, double y, byte playerID) {
		super(level, x, y, 10, 30, 2.5);
		id = playerID;
		animation = new Animation(Textures.instance.getTexture(getBodyTexture(playerID)),
				Sequence.formatSequences(
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4)));

		bow = new Animation(Textures.instance.getTexture(getBowTexture(playerID)),
				Sequence.formatSequences(
						new Sequence(18, 14, 0, 1),
						new Sequence(18, 14, 4, 5).setNoLoop()));
		level.getLightManager().addDynamicLight(this);
		mana = maxMana = 1000;
	}

	private String getBodyTexture(byte playerID) {
		switch(playerID) {
		case 0:
			return "player";
		case 1:
			return "player_2";
		case 2:
			return "player_3";
		case 3:
			return "player_4";
		default:
			return "goblin";
		}
	}

	private String getBowTexture(byte playerID) {
		switch(playerID) {
		case 0:
			return "bow";
		case 1:
			return "bow_2";
		case 2:
			return "bow_3";
		case 3:
			return "bow_4";
		default:
			return "bow";
		}
	}

	public void setMagicCooldown(int cooldown) {
		magicTimer = cooldown;
	}

	public void useMana(int amount) {
		mana -= amount;
		if(mana < 0) { mana = 0; }
		manaRechargeCooldown = 15;
	}

	public int getMana() {
		return mana;
	}

	public double getManaPercentage() {
		return (double)mana / (double)maxMana;
	}

	public double getArmRotation() {
		return armRotation;
	}
	
	public double getLookAngle() {
		return lookAngle;
	}
	
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	public void explode() {
		destroy();
		SoundEffects.instance.play("blast", 1, 1, 0);
		for(int i = 0; i < 200; i++) {
			Coin coin1 = RandomUtils.randDouble() < 0.05 ? new RedGem(level, position.x, position.y) : new GoldCoin(level, position.x, position.y);
			coin1.getVelocity().setAngle(RandomUtils.randAngle(), RandomUtils.randDouble(1.0, 5.0));
			level.spawn(coin1);
			Coin coin2 = RandomUtils.randDouble() < 0.05 ? new RedGem(level, position.x, position.y) : new GoldCoin(level, position.x, position.y);
			coin2.getVelocity().setAngle(RandomUtils.randAngle(), RandomUtils.randDouble(5.0, 15.0));
			level.spawn(coin2);
		}
	}
	
	private double getRawTarget() {
		return position.copy().add(-3, 11).angleBetween(Input.instance.getTargetPos(this, level.gameRenderer, id));
	}

	private double getTarget() {
		double target = getRawTarget();
		ArrayList<Enemy> enemies = level.getEnemies();
		Enemy targetedEnemy = null;
		double shortestDist = 0;
		for(Enemy enemy : enemies) {
			if(enemy.isOnScreen(level.gameRenderer) && canSee(enemy)) {
				double dist = position.distBetween(enemy.getPosition());
				if(targetedEnemy == null || dist < shortestDist) {
					double angleToEnemy = position.angleBetween(enemy.position);
					double delta = Math.abs(AngleHelper.angleDifferenceRadians(target, angleToEnemy));
					if(delta <= aimAssist) {
						shortestDist = dist;
						targetedEnemy = enemy;
					}
				}
			}
		}

		if(targetedEnemy != null) {
			double arrowSpeed = UpgradeProperties.getArrowSpeed();
			int heuristicArrowTravelTime = (int)Math.ceil(shortestDist / arrowSpeed);
			Vector estimatedEnemyPosition = targetedEnemy.position.copy().add(targetedEnemy.velocity.copy().mply(heuristicArrowTravelTime));
			target = position.angleBetween(estimatedEnemyPosition);
		}

		return target;
	}
	
	private double rotateToTarget(double rotation, double target, double speed) {
		if(Math.abs(AngleHelper.angleDifferenceRadians(rotation, target)) > 0.01) {
			int rotationDirection = AngleHelper.getQuickestRotationDirection(rotation, target);
			double rotationAngle = rotationDirection * speed;
			rotation = AngleHelper.correctAngleRadians(rotation + rotationAngle);
			if(Math.abs(AngleHelper.angleDifferenceRadians(rotation, target)) < speed) {
				rotation = target;
			}
		}
		
		return rotation;
	}

	private void updateArmRotation() {
		armRotation = rotateToTarget(armRotation, getTarget(), ARM_ROTATE_SPEED);
		lookAngle = rotateToTarget(lookAngle, getRawTarget(), ARM_ROTATE_SPEED);
	}
	
	private void arrowAttack() {
		shootTimer = UpgradeProperties.getBowRate();
		bow.setSequence(1, true);
		SoundEffects.instance.play("bow", 1, 1, 0);
		
		double innacuracy = UpgradeProperties.getArrowInnacuracy();
		double speed = UpgradeProperties.getArrowSpeed();
		double damage = UpgradeProperties.getArrowDamage();
		
		spawnArrow(armRotation, innacuracy, speed, damage);
		
		if(Run.currentRun.hasUpgrade(UpgradeList.DOUBLE_ARROW)) {
			spawnArrow(armRotation + Math.PI, innacuracy, speed, damage);
			if(Run.currentRun.hasUpgrade(UpgradeList.SPLITTING_ARROW)) {
				spawnArrow(armRotation + Math.PI - Math.toRadians(10), innacuracy, speed, damage);
				spawnArrow(armRotation + Math.PI + Math.toRadians(10), innacuracy, speed, damage);
			}
		}
		
		if(Run.currentRun.hasUpgrade(UpgradeList.SPLITTING_ARROW)) {
			spawnArrow(armRotation - Math.toRadians(10), innacuracy, speed, damage);
			spawnArrow(armRotation + Math.toRadians(10), innacuracy, speed, damage);
		}
	}
	
	private void spawnArrow(double angle, double innacuracy, double speed, double damage) {
		Vector spawnPos = (new Vector()).setAngle(armRotation, 8).add(position).add(-3, 5);
		Arrow arrow = new Arrow(level, spawnPos.x, spawnPos.y, angle + RandomUtils.randInnacuracyDegrees(innacuracy), speed, damage);
		if(Run.currentRun.hasUpgrade(UpgradeList.BOUNCY_ARROW)) { arrow.setBouncy(4); }
		if(Run.currentRun.hasUpgrade(UpgradeList.HOMING_ARROW)) { arrow.setHoming(Math.toRadians(4), 96); }
		level.spawn(arrow);
	}

	protected void updateEntity() {
		up = Input.instance.up(id);
		down = Input.instance.down(id);
		left = Input.instance.left(id);
		right = Input.instance.right(id);

		if(isWalking()) {
			if(down) {
				if(left && !right) { facing = 0; }
				else if(right && !left) { facing = 1; }
				else {
					facing = (facing == 0 || facing == 2 || facing == 4) ? 0 : 1;
				}
			}

			else if(up) {
				if(left && !right) { facing = 4; }
				else if(right && !left) { facing = 5; }
				else {
					facing = (facing == 0 || facing == 2 || facing == 4) ? 4 : 5;
				}
			}

			else {
				if(left && !right) { facing = 2; }
				else if(right && !left) { facing = 3; }
			}
		}

		updateArmRotation();

		if(shootTimer > 0) { --shootTimer; }
		if(magicTimer > 0) { --magicTimer; }
		if(manaRechargeCooldown > 0) { --manaRechargeCooldown; }
		if(mana < maxMana && manaRechargeCooldown == 0) { ++mana; }

		if(Input.instance.isPerformingAction(Action.ATTACK, id) && shootTimer == 0) {
			arrowAttack();
		}

		else if(Input.instance.isPerformingAction(Action.MAGIC, id) && magicTimer == 0) {
			Run.currentRun.spell.use(level, this);
		}

		if(!invisible) {
			ArrayList<Coin> coins = level.getCoins();
			for(Coin coin : coins) {
				if(!coin.shouldRemove() && hitbox.intersectsHitbox(coin.hitbox)) {
					coin.collect(this);
					break;
				}
			}
		}
	}

	public void render(LayerRenderer renderer) {
		if(!invisible) {
			if(facing == 0 || facing == 1 || facing == 3) {
				renderBody(renderer);
				renderArm(renderer);
			}

			else {
				renderArm(renderer);
				renderBody(renderer);
			}
		}
	}

	private void renderBody(LayerRenderer renderer) {
		boolean walking = isWalking();
		if(!animation.isPaused() && !walking) { animation.resetFrame(); }
		animation.setPaused(!walking);
		if(!animation.isPaused()) { animation.setSequence(facing, false); }

		renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
		animation.updateTimer();
	}

	private void renderArm(LayerRenderer renderer) {
		float renderRotation = (float)Math.toDegrees(armRotation) - 90;
		Vector armPos = position.copy().add(-3, 11);
		renderer.getSpriteBatch().draw(bow.getFrame(), (float)armPos.x - (bow.getFrameWidth() / 2), (float)armPos.y - (bow.getFrameHeight() / 2), 9, 1, bow.getFrameWidth(), bow.getFrameHeight(), 1, 1, renderRotation);
		bow.updateTimer();

		if(bow.getSequence() == 1 && bow.isDone()) {
			bow.setSequence(0, true);
		}
	}

	public Vector lightPosition() {
		return position;
	}

	public int numLightRays() {
		return LevelLightManager.DEFAULT_RADIAL_SOURCE;
	}

	public float lightStrength() {
		return 320;
	}

	public Color lightColor() {
		return new Color(1.0F, 1.0F, 1.0F, level.getPlayerLightLevel());
	}
}

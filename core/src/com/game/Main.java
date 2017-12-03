package com.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.game.audio.SoundEffects;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Textures;
import com.game.world.World;
import com.game.world.WorldFactory;

public class Main extends ApplicationAdapter {
	
	private LayerRenderer gameRenderer;
	private LayerRenderer overlayRenderer;
	private World currentWorld;
	private int option;
	private boolean deleteWorld;
	
	public static World nextWorld;
	private static int screen;
	private static Main instance;
	
	public void create() {
		instance = this;
		SoundEffects.instance.loadSounds("blast", "boom", "coin", "good", "hurt", "select", "schut");
		
		gameRenderer = new LayerRenderer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0.5F);
		overlayRenderer = new LayerRenderer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0.5F);
	}
	
	public void render() {
		clearScreen();

		if(deleteWorld) {
			deleteWorld = false;
			currentWorld = null;
		}
		
		if(nextWorld != null) {
			currentWorld = null;
			currentWorld = nextWorld;
			nextWorld = null;
		}
		
		if(currentWorld != null) {
			currentWorld.update();
			gameRenderer.beginBatch();
			currentWorld.render(0);
			gameRenderer.endBatch();
			overlayRenderer.beginBatch();
			currentWorld.render(1);
			overlayRenderer.endBatch();
		}
		
		else if(screen == 0) {
			if(Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP)) {
				SoundEffects.instance.play("select", 1, 1, 0);
				--option;
				if(option < 0) { option = 2; }
			}
			
			else if(Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN)) {
				SoundEffects.instance.play("select", 1, 1, 0);
				++option;
				if(option > 2) { option = 0; }
			}
			
			else if(Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
				switch(option) {
				case 0:
					WorldFactory.firstFloor(gameRenderer, overlayRenderer);
					break;
				case 1:
					screen = 2;
					break;
				case 2:
					Gdx.app.exit();
					break;
				default:
					break;
				}
			}
			
			Texture title = Textures.instance.getTexture("title");
			Texture pointer = Textures.instance.getTexture("pointer");
			Texture newGame = Textures.instance.getTexture("new_game");
			Texture help = Textures.instance.getTexture("help");
			Texture quit = Textures.instance.getTexture("quit");
			
			overlayRenderer.beginBatch();
			overlayRenderer.getSpriteBatch().draw(title, Gdx.graphics.getWidth() / 2 - title.getWidth() / 2, Gdx.graphics.getHeight() / 2 + 120);
			overlayRenderer.getSpriteBatch().draw(newGame, Gdx.graphics.getWidth() / 2 - newGame.getWidth() / 2, Gdx.graphics.getHeight() / 2 + 20);
			overlayRenderer.getSpriteBatch().draw(help, Gdx.graphics.getWidth() / 2 - newGame.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 30);
			overlayRenderer.getSpriteBatch().draw(quit, Gdx.graphics.getWidth() / 2 - newGame.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 80);
			overlayRenderer.getSpriteBatch().draw(pointer, Gdx.graphics.getWidth() / 2 - newGame.getWidth() / 2 - 20, Gdx.graphics.getHeight() / 2 + 24 - (50 * option));
			overlayRenderer.endBatch();
		}
		
		else if(screen == 1) {
			
		}
		
		else if(screen == 2) {
			if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
				screen = 0;
			}
			
			overlayRenderer.beginBatch();
			overlayRenderer.setTextColour(Color.BLACK);
			overlayRenderer.drawText("Welcome, brave adventurer, to the Fiscal Dungeon!", 340, 580);
			overlayRenderer.drawText("Once you embark on your quest to conquer the dungeon, use the W, A, S and D keys to move,", 340, 550);
			overlayRenderer.drawText("use your mouse to aim your bow and use left-click to shoot.", 340, 530);
			overlayRenderer.drawText("Your quest will not be easy. The dungeon is full of gold and treasure, but beware; many", 340, 500);
			overlayRenderer.drawText("believe it all to be cursed. After all, the theme of this Ludum Dare is \"the more you have, the", 340, 480);
			overlayRenderer.drawText("worse it is\"!", 340, 460);
			overlayRenderer.drawText("A polite reminder that, by dungeon law, adventurers are required to pay a \'murder tax\' of 10", 340, 430);
			overlayRenderer.drawText("gold coins for each dungeon entity they slaugher on their travels. If you see any tax return", 340, 410);
			overlayRenderer.drawText("documents, I implore you to do the lawful thing and pick them up in order to pay them.", 340, 390);
			overlayRenderer.drawText("Press the ESC key to return to the main menu.", 340, 360);
			overlayRenderer.setTextColour(Color.WHITE);
			overlayRenderer.endBatch();
		}
	}
	
	public void dispose() {
		gameRenderer.dispose();
		Textures.instance.dispose();
		SoundEffects.instance.dispose();
	}
	
	private void clearScreen() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	public static void toMainMenu() {
		screen = 0;
		instance.deleteWorld = true;
		instance.option = 0;
	}
	
	public static void toCongratulesScreen() {
		screen = 1;
		instance.deleteWorld = true;
	}
}

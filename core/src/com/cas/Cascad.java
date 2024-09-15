package com.cas;

import com.badlogic.gdx.Game;

public class Cascad extends Game {
	
	@Override
	public void create () {
		setScreen(new GameScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}

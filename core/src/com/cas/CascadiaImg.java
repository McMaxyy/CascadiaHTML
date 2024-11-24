package com.cas;

import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CascadiaImg implements Screen {
    Skin skin;
    Viewport vp;
    public Stage stage;
    private Storage storage;
    private TextButton spin;
    private Image[][] elements = new Image[5][5];
    private Label scoreLabel;
    private int score;
    private Random rand = new Random();
    private float elX, elY, inc = 2f;
    private Texture aTex, bTex, cTex, dTex;

    public CascadiaImg(Viewport viewport, Game game, GameScreen gameScreen) {
        stage = new Stage(viewport);
        vp = viewport;
        Gdx.input.setInputProcessor(stage);
        storage = Storage.getInstance();
        storage.createFont();
        skin = storage.skin;
        elX = vp.getWorldWidth() / 10f;
        elY = vp.getWorldHeight() / inc;
        score = 0;

        loadTextures();
        createComponents();
    }
    
    private void loadTextures() {
        aTex = Storage.assetManager.get("A.png", Texture.class);
        bTex = Storage.assetManager.get("B.png", Texture.class);
        cTex = Storage.assetManager.get("C.png", Texture.class);
        dTex = Storage.assetManager.get("D.png", Texture.class);

        aTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        cTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        dTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private Texture getRandomTexture() {
        int randIndex = rand.nextInt(4);
        switch (randIndex) {
            case 0:
                return aTex;
            case 1:
                return bTex;
            case 2:
                return cTex;
            case 3:
                return dTex;
            default:
                return null;
        }
    }
    
    private void startSpinningImage(final Image image) {
        image.addAction(Actions.repeat(20, Actions.sequence(
            Actions.run(() -> image.setDrawable(new TextureRegionDrawable(getRandomTexture()))),
            Actions.delay(0.05f)
        )));

        image.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.run(() -> image.setDrawable(new TextureRegionDrawable(getRandomTexture())))
        ));
    }

    private void createComponents() {
        spin = new TextButton("Spin", storage.buttonStyle);
        spin.setColor(Color.LIGHT_GRAY);
        spin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (elements[0][0] != null)
                    for (int i = 0; i < 5; i++)
                        for (int j = 0; j < 5; j++)
                            elements[i][j].remove();
                spinElements();
            }
        });
        spin.setSize(100, 100);
        spin.setPosition(vp.getWorldWidth() / 1.2f, vp.getWorldHeight() / 32f);
        stage.addActor(spin);

        scoreLabel = new Label("Score: 0", storage.labelStyle);
        scoreLabel.setPosition(vp.getWorldWidth() / 1.5f, vp.getWorldHeight() / 13f);
        stage.addActor(scoreLabel);
    }

    private void spinElements() {
        float spaceX = 110f;
        float spaceY = 110f;
        elX = vp.getWorldWidth() / 10f;
        elY = vp.getWorldHeight() - 100;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Texture texture = getRandomTexture();
                elements[i][j] = new Image(texture);
                float xPos = elX + j * spaceX;
                float yPos = elY - i * spaceY;
                elements[i][j].setPosition(xPos, yPos);
                elements[i][j].setSize(100, 100);
                stage.addActor(elements[i][j]);

                startSpinningImage(elements[i][j]);
            }
        }

        stage.addAction(Actions.sequence(
            Actions.delay(2f),
            Actions.run(this::checkMatches)
        ));
    }

    private void checkMatches() {
        boolean[][] toRemove = new boolean[5][5];
        int[][] horizontalMatches = new int[5][5];
        int[][] verticalMatches = new int[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Texture currentTexture = ((TextureRegionDrawable) elements[i][j].getDrawable()).getRegion().getTexture();

                // Horizontal check
                if (j > 0 && currentTexture.equals(((TextureRegionDrawable) elements[i][j - 1].getDrawable()).getRegion().getTexture())) {
                    horizontalMatches[i][j] = horizontalMatches[i][j - 1] + 1;
                } else {
                    horizontalMatches[i][j] = 1;
                }

                // Vertical check
                if (i > 0 && currentTexture.equals(((TextureRegionDrawable) elements[i - 1][j].getDrawable()).getRegion().getTexture())) {
                    verticalMatches[i][j] = verticalMatches[i - 1][j] + 1;
                } else {
                    verticalMatches[i][j] = 1;
                }

                if (horizontalMatches[i][j] >= 3) {
                    for (int k = 0; k < horizontalMatches[i][j]; k++) {
                        toRemove[i][j - k] = true;
                    }
                    addPoints(horizontalMatches[i][j]);
                }

                if (verticalMatches[i][j] >= 3) {
                    for (int k = 0; k < verticalMatches[i][j]; k++) {
                        toRemove[i - k][j] = true;
                    }
                    addPoints(verticalMatches[i][j]);
                }
            }
        }

        // Apply removals and cascade
        applyMatches(toRemove);
    }


    private void addPoints(int matchLength) {
        if (matchLength == 3) {
            score += 1;
        } else if (matchLength == 4) {
            score += 3;
        } else if (matchLength == 5) {
            score += 6;
        }

        scoreLabel.setText("Score: " + score);
    }

    private void applyMatches(boolean[][] toRemove) {
        boolean hasMatches = false;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (toRemove[i][j]) {
                    hasMatches = true;
                    elements[i][j].addAction(Actions.fadeOut(0.5f));
                }
            }
        }

        if (hasMatches) {
            stage.addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.run(this::performCascading)
            ));
        }
    }

    private void cascadeDown(int col) {
        int emptyCount = 0;

        // Iterate from bottom to top of the column
        for (int i = 4; i >= 0; i--) {
            if (elements[i][col].getColor().a == 0) {
                emptyCount++;
            } else if (emptyCount > 0) {
                // Move the element down by emptyCount slots
                elements[i + emptyCount][col].setDrawable(elements[i][col].getDrawable());
                elements[i + emptyCount][col].setColor(elements[i][col].getColor());

                // Clear the original slot
                elements[i][col].setColor(new Color(1, 1, 1, 0));
            }
        }

        // Fill the top of the column with new random textures and fade them in
        for (int i = 0; i < emptyCount; i++) {
            elements[i][col].setDrawable(new TextureRegionDrawable(getRandomTexture()));
            elements[i][col].setColor(new Color(1, 1, 1, 0));
            elements[i][col].addAction(Actions.fadeIn(0.5f));
        }
    }


    private void performCascading() {
        for (int col = 0; col < 5; col++) {
            cascadeDown(col);
        }

        stage.addAction(Actions.sequence(
            Actions.delay(1f),
            Actions.run(this::checkMatches)
        ));
    }


    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Keys.F5)) {
            stage.clear();
            createComponents();
        }

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}

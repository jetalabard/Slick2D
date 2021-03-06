package de.pokemon;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.StateBasedGame;

public class InGameMenu {

	/** set to true if should be activated*/
	private boolean showMenu;
	
	/** true if FPS display is activated*/
	private boolean showFps;
	
	/** true if Grid display is activated*/
	private boolean showGrid;
	
	/** true if Blocked display is activated*/
	private boolean showBlocked;
	
	/** true if Position display is activated*/
	private boolean showPosition;
	
	/** x-Position of the menu*/
	private int x;
	
	/** y-Position of the menu*/
	private int y;
	
	/** width of the menu, is 1/4 of the game canvas*/
	private int width;
	
	/**	true if Debugn Menu is open*/
	private boolean showDebug;
	
	/** true if Main Menu is open*/
	private boolean showMain;
	
	/** the game, needed to enter states*/
	private StateBasedGame sbg;
	
	/** the container holding the game*/
	private GameContainer gc;
	
	/** the main-menu background*/
	private Rectangle background;
	
	/** the font used for the menu*/
	private SimpleFont font;
	
	/** the triangle cursor*/
	private Polygon cursor;
	
	/** the main menu items */
	private final List<String> mainItems  = Arrays.asList("Menu", "Weidaspuin", "Debugn", "Musi: O", "Beendn");
	//private final String[] mainItems = {"Menu", "Weidaspuin", "Debugn", "Musi", "Beendn"};
	
	/** the debug submenu items */
	private final List<String> debugItems  = Arrays.asList("Debugn", "FPS", "Grid", "Blocked", "Position", "Zruck");
	//private final String[] debugItems = {"Debugn", "FPS", "Grid", "Blocked", "Position", "Zruck"};
	
	/** the points for the triangle in x,y order*/
	private final float[] cursorPoints = new float[]{0,0,8,8,0,16};
	
	/** height of one item in the menu*/
	private final int itemHeight = 32;
	
	/** if menu currently sliding in or out */
	private boolean sliding;

	/**Initialises the InGameMenu
	 * 
	 * @param gc the container holding the game
	 * @param sbg the game 
	 */
	public InGameMenu(GameContainer gc,StateBasedGame sbg){
		this.gc = gc;
		this.sbg = sbg;
		width = gc.getWidth()/4;
		x = gc.getWidth()-width;
		y = 0;
		cursor = new Polygon(cursorPoints);
		cursor.setLocation(gc.getWidth(), y+40);
		background = new Rectangle(gc.getWidth(), y, width, mainItems.size() * itemHeight); 
		showMenu = false;
		showDebug = false;
		showFps = false;
		showGrid = false;
		showBlocked = false;
		showPosition = false;
		showMain = false;
		sliding = false;
		
		try {
			font = new SimpleFont("Arial", Font.PLAIN, 14);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	/** Updates the menu, e.g. cursor position, processes Input, adjusts the background of the menu to fit the items
	 *  Bringin back procedural style in this methods. uargh
	 * @param input Input of the PlayState
	 */
	public void update(Input input){
		if(!showMenu && !sliding)
			return;

		if(!sliding){

			if(showMain){
				background.setHeight(mainItems.size()*itemHeight);
			}else if(showDebug){
				background.setHeight(debugItems.size()*itemHeight);
			}

			if(input.isKeyPressed(Input.KEY_W)){
				if(cursor.getCenterY() > 48){
					cursor.setY(cursor.getY() - itemHeight);
				}else{  //jump to Beendn
					cursor.setCenterY(background.getMaxY()-16);
				}
			}else if(input.isKeyPressed(Input.KEY_S)){
				if(cursor.getCenterY() < background.getMaxY()-16){
					cursor.setY(cursor.getY() + itemHeight);
				}else{ //jump to first item
					resetCursor();
				}
			}

			if(input.isKeyPressed(Input.KEY_ENTER)){
				if(showMain){
					if(cursor.getCenterY() == 48){ //Weidaspuin
						sliding = true;
					}else if(cursor.getCenterY() == 80){ //DEBUG
						showMain = false;
						resetCursor();
						showDebug = true;
					}else if(cursor.getCenterY() == 112){ //Musi
						if(gc.isSoundOn()){
							gc.setMusicOn(false);   //weird behaviour current audio is saved and gets relooped
							gc.setSoundOn(false);
							mainItems.set(3, "Musi: Aus");
						}else{
							gc.setMusicOn(true);
							gc.setSoundOn(true);
							mainItems.set(3, "Musi: O");
						}	
					}else if(cursor.getCenterY() == 144){ //Beendn
						resetCursor();
						showMenu = false;
						showMain = false;
						sliding = false;
						background.setX(gc.getWidth());
						cursor.setX(gc.getWidth());
						sbg.enterState(Core.menu);
					}	
				}else if(showDebug){
					if(cursor.getCenterY() == 48){ // FPS
						if(!showFps){
							showFps = true;
						}else{
							showFps = false;
						}	
					}else if(cursor.getCenterY() == 80){ //GRID
						if(!showGrid){
							showGrid = true;
						}else{
							showGrid = false;
						}
					}else if(cursor.getCenterY() == 112){ // BLOCKED
						if(!showBlocked){
							showBlocked = true;
						}else{
							showBlocked = false;
						}
					}else if(cursor.getCenterY() == 144){ //POSITION
						if(!showPosition){
							showPosition = true;
						}else{
							showPosition = false;
						}
					}else if(cursor.getCenterY() == 176){ // BACK
						showDebug = false;
						showMain = true;
						cursor.setCenterY(mainItems.indexOf("Debugn")*itemHeight+16);
					}
				}
				Sound.audioTextBox.playAsSoundEffect(1.0f, 3.0f, false);
			}

			if(input.isKeyPressed(Input.KEY_ESCAPE)){
				if(showMain){
					sliding = true;	
				}else if(showDebug){
					showDebug = false;
					showMain = true;
					cursor.setCenterY(mainItems.indexOf("Debugn")*itemHeight+16);
				}
				Sound.audioTextBox.playAsSoundEffect(1.0f, 3.0f, false);
				
			}
			
		} else {
			if(showMenu){ // slide out
				if(background.getX() < gc.getWidth()){ //not at end position
					background.setX(background.getX() + 16);
					cursor.setX(background.getX() + 16);
				}else{ //end position
					sliding = false;
					showMain = false;
					showMenu = false;
					resetCursor();
					cursor.setX(gc.getWidth());
					background.setX(gc.getWidth());
				}
			}else{ // slide in 
				if(background.getX() > x){ //not at end position
					background.setX(background.getX() - 16);
					cursor.setX(background.getX() + 16);
				}else{ //end position
					sliding = false;
					showMenu = true;
					showMain = true;
					resetCursor();
				}
			}
		}
	}



	/** Renders the InGameMenu
	 * 
	 * @param g the current graphis context
	 */
	public void render(Graphics g){
		g.setFont(font.get());
		if(showMenu || sliding){
			g.setColor(Color.white);
			g.fill(background);
			if(showMain || sliding){ //render the main items
				g.setColor(Color.black);
				for(int i = 0, j = 16; i < mainItems.size(); i++, j += 32){
					g.drawString(mainItems.get(i), background.getCenterX()-font.get().getWidth(mainItems.get(i))/2, j-font.get().getHeight(mainItems.get(i))/2);
				}
			}else if(showDebug){ // render the debug items
				for(int i = 1; i < debugItems.size() - 1; i++){
					if((i == 1 && showFps == true) || (i == 2 && showGrid == true) || (i == 3 && showBlocked == true) || (i == 4 && showPosition == true))
						g.setColor(Color.green);
					else 
						g.setColor(Color.red);
					g.fillRect(background.getMinX(),i*itemHeight, background.getWidth(), itemHeight);
				}
				g.setColor(Color.black);
				for(int i = 0, j = 16; i < debugItems.size(); i++, j += 32){
					g.drawString(debugItems.get(i), background.getCenterX()-g.getFont().getWidth(debugItems.get(i))/2, j-g.getFont().getHeight(debugItems.get(i))/2);
					//g.drawRect(x, j-16, width, 32); // rectangles for debug purposes
				}
			}
			g.setColor(Color.black);
			g.fill(cursor);
			g.draw(background);
			g.drawLine(background.getX(), 32, gc.getWidth(), 32);
			g.drawRect(background.getX()+1, y+2, width-4, 28);
		}
		g.resetFont();
	}

	/**
	 * Reset the cursor the first item of the menu
	 */
	private void resetCursor(){
		cursor.setX(x + 16);
		cursor.setCenterY(itemHeight*3/2);
	}

	/**
	 * 
	 * @return true if menu is currently sliding in or out
	 */
	public boolean isSliding() {
		return sliding;
	}

	/**lets the menu slide in or out
	 * 
	 * @param sliding 
	 */
	public void setSliding(boolean sliding) {
		this.sliding = sliding;
	}

	/**
	 * 
	 * @return true if menu is active
	 */
	public boolean isShowMenu() {
		return showMenu;
	}

	/**
	 * 
	 * @return if fps display is activated 
	 */
	public boolean isShowFps() {
		return showFps;
	}

	/**
	 * 
	 * @return if grid display is active
	 */
	public boolean isShowGrid() {
		return showGrid;
	}

	/**
	 * 
	 * @return if position display is active
	 */
	public boolean isShowPosition() {
		return showPosition;
	}

	/**
	 * 
	 * @return if blocked display is active
	 */
	public boolean isShowBlocked() {
		return showBlocked;
	}

}

package FSE;

//importing everything
import java.io.*;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.math.*;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.ImageObserver;
import java.awt.geom.Rectangle2D;

public class LastStand extends JFrame {
	GamePanel game;
	int reqWidth = 700;
	int reqHeight = 930;

	public LastStand() throws IOException {
		super("K&L's Last Stand"); // name
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // making the window closable
		setSize(700, 930); // window size
		setResizable(false); // window is not resizable
		setLocationRelativeTo(null); // center the window
		setLayout(new BorderLayout());
		game = new GamePanel(this); // creating the panel
		add(game);
		setVisible(true);
	}

	public static void main(String[] args) throws IOException {
		LastStand laststand = new LastStand();
	}
}

class GamePanel extends JPanel implements KeyListener, MouseListener, ActionListener, MouseMotionListener {
	private JFrame frame;// game stuff
	private String username, typedValue, screen = "menu";
	private boolean[] keys;
	private Image[] backgrounds = new Image[1];
	private button[] buttons = new button[4];
	private String[] buttonText = { "PLAY GAME", "INSTRUCTIONS", "HIGH SCORE", "QUIT" };
	private double[][] starList;
	private char[] characters = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	private LinkedList<enemy>[] enemies = new LinkedList[26];// enemies and objects
	private ArrayList<enemy> bullets = new ArrayList<enemy>();
	private ArrayList<attack> b = new ArrayList<attack>();
	private ArrayList<String> basic = new ArrayList<String>();
	private ArrayList<String> intermediate = new ArrayList<String>();
	private ArrayList<String> advanced = new ArrayList<String>();
	private ArrayList<enemy> dead = new ArrayList<enemy>();
	private Rectangle2D ship = new Rectangle2D.Double(317, 820, 64, 55);
	private Rectangle2D check;
	private enemy activeTarget = null;
	private int enemySlot = 0, highestScore;

	private Image logo;// pics
	private Image ship1;
	private Image ship2;
	private Image ship3;
	private Image ship4;
	private Image ship5;
	private Image shot2;

	private circle empRad = new circle();// graphics
	private circle target = new circle();

	private AffineTransform trans = new AffineTransform();// miscellaneous
	private Font menuFont = new Font("Rocket Propelled", Font.TRUETYPE_FONT, 40);
	private Font text = new Font("Falling Sky", Font.TRUETYPE_FONT, 20);
	private Random random = new Random();
	private Color colour;
	private Timer timer;

	@SuppressWarnings("unchecked")
	private int level = 2, bombRad, filledEnemies;
	private int targetRad = 100;
	private boolean levelFinish = false, bombing, enemyShoot = false, paused = false; 
	private toggle toggle;
	private int emps = 2;
	private int lives = 2;

	boolean newTarget = true;
	private bigBoss testerBoss = new bigBoss("Amaaaaaaazing", 0, 0);

	// stats
	private int wrong = 0;// wrong
	private int counter = 0;// total amount of keys clicked
	private int levelWrong = 0;
	private int levelCounter = 0;
	private int score = 0;

	public GamePanel(JFrame frame) throws IOException {
		setSize(700, 930);
		getUsername();
		this.frame = frame;
		starList = genScrollStars(450);
		keys = new boolean[KeyEvent.KEY_LAST + 1];
		logo = new ImageIcon("logo.png").getImage();
		ship1 = new ImageIcon("ship1.gif").getImage();
		ship2 = new ImageIcon("ship2.gif").getImage();
		ship3 = new ImageIcon("ship3.gif").getImage();
		ship4 = new ImageIcon("ship4.gif").getImage();
		ship5 = new ImageIcon("ship5.gif").getImage();
		shot2 = new ImageIcon("shot2.png").getImage();
		initEnemies();
		addKeyListener(this);
		addMouseListener(this);
	}

	public void getUsername() {
		username = JOptionPane.showInputDialog(null, "Input Username:", "Username Input", JOptionPane.QUESTION_MESSAGE);
		while (username == null || username.equals("") || username.trim().length() <= 0)
			username = JOptionPane.showInputDialog(null, "Input Username:", "Username Input",
					JOptionPane.QUESTION_MESSAGE);
	}

	//////////////////// Making Enemies///////////////////

	public boolean isAlpha(String name) {// we have a huge textfile of words, this jsut checks if there is any special
											// characters
		char[] chars = name.toCharArray();

		for (char c : chars) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}

		return true;
	}

	public boolean checkClose(int x, int y) {// checking enemy coordinate values, so that they don't all spawn on top of
												// each other, making it hard to see
		for (int i = 0; i < 26; i++) {// goes through all the 26 linked list
			LinkedList<enemy> current = enemies[i];
			for (enemy enemyCheck : current) {// goes through each enemy
				if (Math.abs(enemyCheck.getX() - x) < 5) {// if its closer than 5 pixels
					return true;
				}
				if (Math.abs(enemyCheck.getY() - y) < 5) {// if its closer than 5 pixels
					return true;
				}
			}
		}
		return false;
	}

	public int randX() {// getting a random x value above the screen so that the enemies can float into
						// the visible screen
		Random rand = new Random();
		int random = (int) (rand.nextInt((basic.size()) + 1));
		int[] possibleX = new int[4];
		possibleX[0] = -1 * rand.nextInt(100);// 4 random coordinates and we pick 1
		possibleX[1] = 700 + rand.nextInt(100);// the x coordinates are in different regions
		possibleX[2] = rand.nextInt(346);
		possibleX[3] = 355 + rand.nextInt(346);
		return possibleX[rand.nextInt(4)];
	}

	public int randY(int level) {// returns a random y value above the screen
		Random rand = new Random();
		return -50 - rand.nextInt(226) - (level * 25);
	}

	public void addEnemy(enemy n) {
		int pos = n.index();
		enemies[pos].add(n);
	}

	public void initEnemies() throws IOException{
		for (int i = 0; i < 26; i++) {
			enemies[i] = new LinkedList<enemy>();
		}

		Scanner stdin = new Scanner(System.in);
		Scanner inFile = new Scanner(new BufferedReader(// scanning and opening the word textfile
				new FileReader("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\files\\words.txt")));
		while (inFile.hasNextLine()) {// while theres more to read
			String word = inFile.nextLine();
			if (isAlpha(word)) {// checking if theres any special characters
				if (word.length() <= 5) {// adding it to a corresponding list: basic, intermediate, advanced based on
											// the character length
					basic.add(word);
				} else if (word.length() <= 8) {
					intermediate.add(word);
				} else {
					advanced.add(word);
				}
			}
		}
		inFile.close();
		Collections.shuffle(basic);
		Collections.shuffle(intermediate);
		Collections.shuffle(advanced);
		makeEnemies();
	}

	public void makeEnemies() {// making enemies
		Random rand = new Random();
		switch (level) {// different amounts for each level
		// the way this code works is we have up to 3 for loops for each level, one loop
		// for basic, intermediate and advanced
		// some earlier levels may not have 3 as we do not want to introduce harder
		// words until later
		// in each for loop it takes a random x and y value, give it a random value from
		// the corresponding list, and if checkClose()==false, we add it
		case 1:
			for (int b = 0; b < 4 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(1);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			break;
		case 2:
			for (int b = 0; b < 4 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(2);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 4 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(2);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			break;
		case 3:
			for (int b = 0; b < 4 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(3);
				/*
				 * while(checkClose(pickedX,pickedY)==true){ pickedX = randX(); pickedY =
				 * randY(); }
				 */
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}

			}
			for (int i = 0; i < 4 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(3);
				/*
				 * while(checkClose(pickedX,pickedY)==true){ pickedX = randX(); pickedY =
				 * randY(); }
				 */
				int random = (int) (rand.nextInt((intermediate.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}

			}
			break;
		case 4:
			for (int b = 0; b < 4 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(4);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 5 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(4);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 5 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(4);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 5:
			for (int b = 0; b < 5 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(5);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 7 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(5);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 7 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(5);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 6:
			for (int b = 0; b < 6 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(6);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 7 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(6);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 7 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(6);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 7:
			for (int b = 0; b < 7 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(7);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 8 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(7);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 8 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(7);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 8:
			for (int b = 0; b < 9 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(8);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 13 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(8);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 13 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(8);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 9:
			for (int b = 0; b < 12 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(9);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 17 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(9);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 17 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(9);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		case 10:
			for (int b = 0; b < 20 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(10);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					basic.remove(val);
				}
			}
			for (int i = 0; i < 22 * (level - 1); i++) {
				int pickedX = randX();
				int pickedY = randY(10);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = intermediate.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					intermediate.remove(val);
				}
			}
			for (int a = 0; a < 19 * (level - 3); a++) {
				int pickedX = randX();
				int pickedY = randY(10);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = advanced.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				if (checkClose(pickedX, pickedY) == false) {
					addEnemy(newEnemy);
					advanced.remove(val);
				}
			}
			break;
		}
		levelFinish = false;
	}

	///////////////////////// Stars////////////////////////////

	public double[][] genScrollStars(int max) {
		double[][] stars = new double[max][];
		double[] star;
		for (int i = 0; i < max; i++) {
			star = new double[] { random.nextInt(700), random.nextInt(930), random.nextInt(100) }; // {x, y, speed}
			if (star[2] < 70) // 70% chance to go normally
				star[2] = 0.2;
			else if (star[2] < 95) // 35% chance to go 150% speed
				star[2] = 0.3;
			else // 5% chance to go even faster speed
				star[2] = 0.4;
			stars[i] = star;
		}
		return stars;
	}

	public void moveStars(double[][] stars, Graphics g) {
		double[] speeds = { 0.2, 0.3, 0.4 };
		for (double[] star : stars) {
			star[1] += star[2];
			if (star[1] > 930) {
				star[1] = 0;
				star[0] = random.nextInt(700);
				star[2] = speeds[random.nextInt(3)];
			}

			if (star[2] == 0.2)
				colour = new Color(100, 100, 100);

			else if (star[2] == 0.3)
				colour = new Color(160, 160, 160);

			else
				colour = new Color(250, 250, 250);
			g.setColor(colour);
			g.fillOval((int) star[0], (int) star[1], 2, 2);
		}
	}

	////////////////////// Graphics/////////////////////////

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if (screen == "menu")
			menu(g);
		else if (screen == "Play Game")
			try {
				game(g);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else if (screen == "Instructions")
			instructions(g);
		else if (screen == "High Scores")
			hsScreen(g);
		else if (screen == "next")
			next(g);
		else if (screen == "end") {
			endScreen(g);
		} else
			frame.dispose();
		repaint();
	}

	public void menu(Graphics g) {
		requestFocusInWindow();
		g.setColor(Color.black);
		g.fillRect(0, 0, 700, 930);
		g.setFont(menuFont);
		moveStars(starList, g);
		String[] values = { "Play Game", "Instructions", "High Scores", "Quit" };
		g.setColor(Color.white);
		// test.move();
		// g.fillRect(test.hitbox.x, test.hitbox.y, test.hitbox.width,
		// test.hitbox.height);
		// activeTarget.move();
		for (int i = 0; i < 4; i++) {
			buttons[i] = new button(220, 330 + 80 * i, 280, 60, values[i]);
			g.fillRect(buttons[i].x, buttons[i].y, buttons[i].width, buttons[i].height);
		}
		g.drawImage(logo, 110, 115, this);
		g.setColor(Color.black);
		g.drawString("PLAY GAME", 240, 375);
		g.drawString("INSTRUCTIONS", 228, 455);
		g.drawString("HIGH SCORES", 235, 535);
		g.drawString("QUIT", 315, 615);
	}

	public void createEnemyBullets() {
		ArrayList<enemy> allOptions = getAllEnemies();
		Random rand = new Random();
		if (allOptions.size() >= 3) {
			for (int i = 0; i < 2; i++) {
				int random = (int) (rand.nextInt((characters.length)));
				bullets.add(new enemy(Character.toString(characters[random]), allOptions.get(i).getX(),
						allOptions.get(i).getY()));
			}
		} else {
			for (int i = 0; i < allOptions.size(); i++) {
				int random = (int) (rand.nextInt((characters.length)));
				bullets.add(new enemy(Character.toString(characters[random]), allOptions.get(i).getX(),
						allOptions.get(i).getY()));

			}
		}

		for (Iterator<enemy> it = bullets.iterator(); it.hasNext();) {
			enemy n = it.next();
			n.setBullet();
		}
	}
	
	public void clearAll() {
		bullets.clear();
		b.clear();
		enemies = null;
	}

	public void game(Graphics g) throws NumberFormatException, IOException {
		// calling everything, checking everything, running everything\
		Graphics2D g2 = (Graphics2D) g;
		enemyShoot = toggle.getShoot();
		int total = 0;
		requestFocusInWindow();
		if (!paused){
			g.setColor(Color.black);
			g.fillRect(0, 0, 700, 930);
			moveStars(starList, g);
			if (bombing) {
				bombRad++;
				empRad.setRad(bombRad);
				drawBomb(g, bombRad);
				checkRad();
				if (bombRad == 300) {
					bombing = false;
					bombRad = 0;
				}
				repaint();
			}
	
			g.drawImage(ship2, 317, 820, this);
			g.setColor(Color.red);
			g.fillRect(317, 820, 64, 55);
			if (levelFinish) {
				makeEnemies();
			}
			g.setFont(text);
			g.setColor(Color.white);
	
			if (enemyShoot) {
				createEnemyBullets();
				enemyShoot = false;
				timer = new Timer();
				toggle = new toggle(enemyShoot);
				timer.schedule(toggle, 5000, 5000);
			}
	
			// testerBoss.move();
			// g.drawString(testerBoss.getValue(), testerBoss.getX(), testerBoss.getY());
			// testerBoss.attack();
	
			// testerBoss.moveAttack();
	
			for (int i = 0; i < 26; i++) {
				LinkedList<enemy> current = enemies[i];
				for (Iterator<enemy> it = current.iterator(); it.hasNext();) {
					enemy n = it.next();
					// check= new Rectangle2D.Double(n.getX()-29,n.getY()-29,58,58);
					n.move();
					g.setColor(Color.red);
					g.fillRect(n.getX(), n.getY(), 58, 58);
					if (ship.intersects(n.getX(), n.getY(), 58, 58)) {
						lives -= 1;
						if (lives <= 0) {
							clearAll();
							highScore();
							screen = "end";
							System.out.println(highestScore);
							return;
						}
					}
				}
			}
	
			for (Iterator<attack> it = b.iterator(); it.hasNext();) {
				attack attack = it.next();
				attack.move();
				if (attack.check()) {
					it.remove();
					attack.getOwner().getAttacks().remove(attack);
				}
			}
	
			if (activeTarget != null) {
				if (newTarget) {
					targetRad--;
					target.setRad(targetRad);
					drawTarget(g, targetRad, activeTarget);
					if (targetRad == 0) {
						newTarget = false;
						targetRad = 100;
					}
				}
	
				g.setColor(Color.red);
				g.drawLine(activeTarget.getX() + 29, activeTarget.getY() + 29, activeTarget.getX() + 29,
						activeTarget.getY() + 29);
	
				if (activeTarget.getValue().equals("")) {// if we finished typing that enemies word
					// "chewbacca"
					dead.add(activeTarget);
					score += activeTarget.getScore() * level;
					activeTarget = null;
				}
			}
	
			for (attack current : b) {
				g.drawImage(shot2, current.getX(), current.getY(), this);
			}
			/*
			 * for (int i = 0; i < 26; i++) { LinkedList<enemy> current = enemies[i]; for
			 * (enemy n : current) { double rotation = (Math.PI - getAngle(n.getX(),
			 * n.getY())); double locX = n.getImage().getWidth(this) / 2; double locY =
			 * n.getImage().getHeight(this) / 2; AffineTransform tx =
			 * AffineTransform.getRotateInstance(rotation, locX, locY); g2.drawImage(ship4,
			 * tx, this); } }
			 */
			g.setFont(text);
			for (int i = 0; i < 26; i++) {
				LinkedList<enemy> current = enemies[i];
				for (enemy n : current) {
					g.drawImage(ship4, n.getX(), n.getY(), this);
					g.setColor(Color.black);
					g.fillRect(n.getX(), n.getY() + 20, n.getValue().length() * 23, 20);
					g.setColor(Color.white);
					g.drawString(n.getValue(), n.getX() + 4, n.getY() + 35);
				}
			}
	
			for (Iterator<enemy> it = bullets.iterator(); it.hasNext();) {
				enemy n = it.next();
				g.setColor(Color.white);
				g.drawString(n.getValue(), n.getX(), n.getY());
				n.move();
				if (ship.intersects(n.getX(), n.getY(), 10, 10)) {
					lives -= 1;
					if (lives <= 0) {
						clearAll();
						highScore();
						screen = "end";
						return;
					}
				}
			}
	
			for (Iterator<enemy> it = dead.iterator(); it.hasNext();) {
				enemy n = it.next();
				if (n.getAttacks().isEmpty())
					explode(g, n);
			}
	
			/*
			 * for (Iterator<enemy> it = current.iterator(); it.hasNext();) { enemy n =
			 * it.next(); if (empRad.contains((n.getX() + 29), (n.getY() + 29)) && bombing)
			 * { if (n.equals(activeTarget)) activeTarget = null; it.remove(); } }
			 */
			if (activeTarget != null) {
				g.setColor(Color.red);
				g.drawString(activeTarget.getValue(), activeTarget.getX() + 4, activeTarget.getY() + 35);
			}
	
			g.setColor(Color.white);
			filledEnemies = 0;
			for (LinkedList<enemy> enemyList : enemies) {
				if (!enemyList.isEmpty())
					filledEnemies++;
			}
	
			for (enemy enemy : bullets) {
				filledEnemies++;
			}
	
			if (filledEnemies < 1) {
				bombing = false;
				bombRad = 0;
				emps = 2;
				dead = new ArrayList<enemy>();
				levelFinish = true;
				activeTarget = null;
				newTarget = true;
				dead.clear();
				level += 1;
				screen = "next";
			}
		}
		else{
			g.setColor(new Color(255, 255, 255, 1));
			g.fillRect(0, 0, 700, 930);
		}
	}

	public void next(Graphics g) {
		String levelStats = "LEVEL " + level;

		String gameHeader = "Game Stats                        Accuracy";
		String gameLine = "__";
		String gameStats = round(((double) (counter - wrong) / counter) * 100, 2) + "%";

		g.setColor(Color.black);
		g.fillRect(0, 0, 700, 930);
		g.setColor(Color.white);
		g.setFont(menuFont);
		// g.drawString(levelHeader, 50, 50);
		g.drawString(levelStats, 50, 100);
		g.drawString(gameHeader, 50, 140);
		g.drawString(gameLine, 50, 160);
		g.drawString(gameStats, 50, 190);

		g.fillRect(220, 450, 280, 60);
		g.setColor(Color.black);
		g.drawString("NEXT LEVEL", 240, 495);
	}

	public void drawBomb(Graphics g, int rad) {// super simple, just drawing an oval
		g.setColor(Color.white);
		g.drawOval(350 - rad, 850 - rad, rad * 2, rad * 2);
	}

	public void drawTarget(Graphics g, int rad, enemy e) {// super simple, just drawing an oval
		g.setColor(Color.orange);
		g.drawOval(e.getX() - rad + 28, e.getY() - rad + 28, rad * 2, rad * 2);
	}

	public void instructions(Graphics g) {
		g.setColor(Color.blue);
		g.fillRect(0, 0, 700, 930);
	}

	public void hsScreen(Graphics g) {
		g.setColor(Color.yellow);
		g.fillRect(0, 0, 700, 930);
	}

	public void endScreen(Graphics g) {
		g.setColor(Color.yellow);
		g.fillRect(0, 0, 700, 930);
	}

	public void explode(Graphics g, enemy n) {
		Image[] explosion = n.getPics();// making an image array of all the explosion sprites
		n.addFrame();// adding to the enemies field value of frame which dictates which sprite to
						// draw
		if (n.isBullet() == true) {
			g.drawImage(explosion[n.getFrame()], n.getX() - 58, n.getY() - 58, this);// drawing the picture
		} else {
			g.drawImage(explosion[n.getFrame()], n.getX() - 28, n.getY() - 28, this);// drawing the picture
		}

		if (n.getFrame() == 5) {// if it reaches 5
			if (n.isBullet() == false) {
				for (int i = 0; i < 26; i++) {
					LinkedList<enemy> current = enemies[i];
					if (current.contains(n)) {
						current.remove(n);
					}
				}
			} else {
				bullets.remove(n);
			}
		}
	}
	////////////////////// Math and Logic Functions//////////////////////

	public double getAngle(int x, int y) {// trig
		double diffx = 350 - x;
		double diffy = 850 - y;
		double ang = Math.atan(diffx / diffy);
		return ang;
	}

	public void checkRad() {
		for (int i = 0; i < 26; i++) {// going through our enemy list
			LinkedList<enemy> current = enemies[i];
			for (Iterator<enemy> it = current.iterator(); it.hasNext();) {
				enemy n = it.next();
				if (empRad.contains((n.getX() + 29), (n.getY() + 29)) && bombing) {// if its inside
					if (n.equals(activeTarget))
						activeTarget = null;
					it.remove();
				}
			}
		}
	}

	public static double round(double value, int places) {// from stack overflow
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public enemy getLowest(LinkedList<enemy> n) {// takes in LL as parameter
		int max = n.get(0).getY();// setting a base max y value
		enemy checker = n.get(0);
		for (enemy check : n) {// goes through the LL
			int distance = check.getY();
			if (distance < 0) {

			}
			if (distance > max) {// if its lower reset the checker
				max = distance;
				checker = check;
			}
		}

		enemy lowest = checker;// returning lowest
		return lowest;
	}

	public ArrayList<enemy> getAllEnemies() {// adds all enemies into one arraylist
		ArrayList<enemy> returning = new ArrayList<enemy>();
		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (Iterator<enemy> it = current.iterator(); it.hasNext();) {
				enemy n = it.next();
				returning.add(n);
			}
		}
		return returning;
	}

	public void reOrganize() {
		if (activeTarget == null)
			return;
		else {
			enemies[enemySlot].remove(activeTarget);// removing the enemy from its old linked list
			enemies[activeTarget.index()].add(activeTarget);// adding it to the index it should be in now
			activeTarget = null;// resetting
		}
	}

	public void target(char n) {
		boolean bulletKilled = false;
		if (levelFinish == false) {
			int slot = (int) n - 97;// this is finding the index in the list, ascii codes for lower case start at
									// 97, so thats why im subtracting

			if (bullets.size() > 0) {
				for (Iterator<enemy> it = bullets.iterator(); it.hasNext();) {
					enemy b = it.next();
					if (b.getValue().charAt(0) == n) {
						// it.remove();
						bulletKilled = true;
						activeTarget = b;
						activeTarget.remove();
						break;
					}
				}
			}

			if (bulletKilled == false) {
				if (!enemies[slot].isEmpty()) {// if there's an enemy that starts with this character
					activeTarget = getLowest(enemies[slot]);// getting the enemy that is the closest
					if (activeTarget.getY() > -70 && activeTarget.getX() < 700 && activeTarget.getX() > 0) {
						typing(n); // used later when im deleting and reorganizing
						enemySlot = slot;//
						newTarget = true;
					}
				}

				else {
					levelWrong++;
					wrong++;
				}
			}
		}
	}

	public void typing(char n) {
		if (levelFinish == false) {
			if (activeTarget == null) {// if there's not target
				target(n);
			}

			else {
				// newTarget=false;
				if (!activeTarget.getValue().equals("")) {
					if (activeTarget.getValue().charAt(0) == n && !activeTarget.getValue().equals("")) { // if you enter
																											// a
																											// character
																											// correctly
						attack newAttack = new attack(activeTarget.getX() + 29, activeTarget.getY() + 58);
						newAttack.setOwner(activeTarget);
						b.add(newAttack);
						activeTarget.addAttack(newAttack);
						activeTarget.remove();// removes first letter
						counter++;
						// score
						// target(n);
					} else {
						// counter++;
						wrong++;
						levelWrong++;
					}
				}
			}
		}
	}

	/////////////////////// KeyPressed,Interactions////////////////////////

	public void keyPressed(KeyEvent e) {
		if (screen == "Play Game") {
			// inScreenCount=0;
			counter++;
			levelCounter++;
			boolean inScreen = false;
			for (int i = 0; i < 26; i++) {// this loop helps prevent an error we were having
				LinkedList<enemy> current = enemies[i];
				for (enemy x : current) {
					if (x.getY() > -70) {
						if (x.getX() > 0 && x.getX() < 700) {
							inScreen = true;
						}
					}
				}
			}
			// calls typing for each letter value
			if (inScreen && screen.equals("Play Game")) {
				if (e.getKeyCode() == KeyEvent.VK_A) {
					typing('a');
				}
				if (e.getKeyCode() == KeyEvent.VK_B) {
					typing('b');
				}
				if (e.getKeyCode() == KeyEvent.VK_C) {
					typing('c');
				}
				if (e.getKeyCode() == KeyEvent.VK_D) {
					typing('d');
				}
				if (e.getKeyCode() == KeyEvent.VK_E) {
					typing('e');
				}
				if (e.getKeyCode() == KeyEvent.VK_F) {
					typing('f');
				}
				if (e.getKeyCode() == KeyEvent.VK_G) {
					typing('g');
				}
				if (e.getKeyCode() == KeyEvent.VK_H) {
					typing('h');
				}
				if (e.getKeyCode() == KeyEvent.VK_I) {
					typing('i');
				}
				if (e.getKeyCode() == KeyEvent.VK_J) {
					typing('j');
				}
				if (e.getKeyCode() == KeyEvent.VK_K) {
					typing('k');
				}
				if (e.getKeyCode() == KeyEvent.VK_L) {
					typing('l');
				}
				if (e.getKeyCode() == KeyEvent.VK_M) {
					typing('m');
				}
				if (e.getKeyCode() == KeyEvent.VK_N) {
					typing('n');
				}
				if (e.getKeyCode() == KeyEvent.VK_O) {
					typing('o');
				}
				if (e.getKeyCode() == KeyEvent.VK_P) {
					typing('p');
				}
				if (e.getKeyCode() == KeyEvent.VK_Q) {
					typing('q');
				}
				if (e.getKeyCode() == KeyEvent.VK_R) {
					typing('r');
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					typing('s');
				}
				if (e.getKeyCode() == KeyEvent.VK_T) {
					typing('t');
				}
				if (e.getKeyCode() == KeyEvent.VK_U) {
					typing('u');
				}
				if (e.getKeyCode() == KeyEvent.VK_V) {
					typing('v');
				}
				if (e.getKeyCode() == KeyEvent.VK_W) {
					typing('w');
				}
				if (e.getKeyCode() == KeyEvent.VK_X) {
					typing('x');
				}
				if (e.getKeyCode() == KeyEvent.VK_Y) {
					typing('y');
				}
				if (e.getKeyCode() == KeyEvent.VK_Z) {
					typing('z');
				}
			}

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				paused = !paused;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				if (!bombing && emps > 0) {
					emps--;
					bombing = true;
					bombRad = 0;
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (activeTarget != null)
				if (!activeTarget.getValue().equals("")) {
					reOrganize();
				}
		}

	}

	public void keyTyped(KeyEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		if (screen == "menu") {
			for (button button : buttons) {
				if (button.contains(e.getX(), e.getY())) {
					screen = button.getVal();
					if (screen == "Play Game") {
						timer = new Timer();
						toggle = new toggle(enemyShoot);
						timer.schedule(toggle, 2500, 2500);
					}

				}
			}
		}
		if (screen == "next") {
			Rectangle nextButton = new Rectangle(220, 450, 280, 60);
			if (nextButton.contains(e.getX(), e.getY())) {
				screen = "Play Game";
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void highScore() throws IOException, NumberFormatException {
		BufferedReader scoreFile = new BufferedReader(new FileReader("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\files\\highscore.txt"));
		String line = scoreFile.readLine();
		highestScore = Integer.parseInt(line);
		scoreFile.close();
		
		BufferedWriter scoreFile2 = new BufferedWriter(new FileWriter("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\files\\highscore.txt"));
		
		if (score > highestScore){
			highestScore = score;
			String scoreStr = Integer.toString(score);
			scoreFile2.write(scoreStr);
			
		}
		else{
			String scoreStr = Integer.toString(highestScore);
			scoreFile2.write(scoreStr);
			
		}
		scoreFile2.close(); 
	}
}

/////////////////////////// Classes//////////////////////////

class Base {// base class that has setters getters
	protected double x;
	protected double y;
	public Rectangle hitbox = new Rectangle((int) x, (int) y, 5, 5);

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public Base(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void translate(double dx, double dy) {// simple moving function
		x += dx;
		y += dy;
		hitbox = new Rectangle((int) x, (int) y, 5, 5);
	}
}

class Bullet extends Base {// this is for our enemies when they shoot
	private char letter;
	private int destX, destY;
	private double rad, dx, dy;
	private enemy owner;

	public char getLetter() {
		return letter;
	}

	public Bullet(char n, int x, int y, int destX, int destY) {// we take this many parameters and we can move them
		// independtely without needing to access through the enemies
		super(x, y);
		letter = n;
		this.destX = destX;
		this.destY = destY;
		double diffx = destX - x;
		double diffy = destY - y;
		rad = Math.atan(diffx / diffy);
		dx = Math.sin(rad) / 5;
		dy = Math.cos(rad) / 5;
	}

	public void move() {
		if (y < 850) {
			translate(dx, dy);
		}
	}

	public void explode(Graphics g, ImageObserver io) {

	}

	public enemy getOwner() {
		return owner;
	}

	public void setOwner(enemy owner) {
		this.owner = owner;
	}

}

class attack extends Base {// the player attacks which just fly, simple functions that reoccur with
							// everything else
	double rad, dx, dy;
	int destx, desty;
	enemy owner;

	public attack(int x, int y) {
		super(317 + 29, 800);
		this.destx = x;
		this.desty = y;
		double diffx = 346 - x;
		double diffy = 800 - y;
		rad = Math.atan(diffx / diffy);
		dx = Math.sin(rad) * 5;
		dy = Math.cos(rad) * 5;
	}

	public void move() {
		if (y < 850) {
			translate(-dx, -dy);
		}
	}

	public boolean check() {
		// if (x==destx){
		if (y <= desty) {
			return true;
		}
		// }
		return false;
	}

	public enemy getOwner() {
		return owner;
	}

	public void setOwner(enemy owner) {
		this.owner = owner;
	}
}

class enemy extends Base {
	// basic setters getters, nothing advanced
	private String value;
	private int type;
	private int score;
	private Image pic = new ImageIcon("ship4.gif").getImage();
	private Image[] explosion = new Image[14];
	private ArrayList<attack> targetedAttacks = new ArrayList<attack>();
	double rad, dx, dy, frameRate = 0.0;
	private boolean dead = false;
	private boolean isBullet = false;

	public enemy(String value, int x, int y) {
		super(x + 29, y + 29);
		this.value = value;
		double diffx = 310 - x;
		double diffy = 810 - y;
		rad = Math.atan(diffx / diffy);
		dx = Math.sin(rad) / 11;
		dy = Math.cos(rad) / 11;
		score = value.length();
		for (int i = 0; i < 13; i++) {
			explosion[i] = new ImageIcon("tile" + i + ".png").getImage();
		}
	}

	public Image[] getPics() {
		return explosion;
	}

	public void setBullet() {
		isBullet = true;
	}

	public boolean isBullet() {
		return isBullet;
	}

	public int getFrame() {
		return (int) frameRate;
	}

	public void addFrame() {
		if (frameRate <= 13)
			frameRate += 0.125;
	}

	public String getValue() {
		return value;
	}

	public boolean getDead() {
		return dead;
	}

	public void setDead() {
		dead = true;
	}

	public int getType() {
		return type;
	}

	public int getScore() {
		return score;
	}

	public Image getImage() {
		return pic;
	}

	public double rad() {
		return rad;
	}

	public ArrayList<attack> getAttacks() {
		return targetedAttacks;
	}

	public void addAttack(attack a) {
		targetedAttacks.add(a);
	}

	public void move() {
		if (y < 850) {
			if (isBullet == true) {
				translate(dx * 2, dy * 2);
			}
			translate(dx, dy);
		}
	}

	public int index() {
		char first = value.charAt(0);
		return (int) first - 97;
	}

	public void remove() {
		value = value.substring(1);
	}
}

class bigBoss extends enemy {
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	int[] xCoordinates = new int[] { 0, 0, 0, 175, 390, 630, 700, 700, 700 };
	int[] yCoordinates = new int[] { 500, 685, 870, 900, 900, 900, 500, 685, 870 };
	char[] characters = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	Random rand = new Random();
	int random = (int) (rand.nextInt((characters.length)));

	public bigBoss(String value, int x, int y) {
		super(value, x, y);
	}

	public void attack() {
		for (int i = 0; i < 9; i++) {
			bullets.add(new Bullet(characters[random], (int) x, (int) y, xCoordinates[i], yCoordinates[i]));
		}
	}

	public void moveAttack() {
		for (Bullet n : bullets) {
			n.move();
		}
	}
}

class button extends Rectangle {
	private String val;

	public button(int x, int y, int wid, int len, String val) {
		super(x, y, wid, len);
		this.val = val;
	}

	public String getVal() {
		return val;
	}
}

class circle {
	private int cx = 350, cy = 850, rad;

	public boolean contains(int x, int y) {
		double dist = Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
		if (dist <= rad) {
			return true;
		}
		return false;
	}

	public void setRad(int r) {
		rad = r;
	}

	public int getRad() {
		return rad;
	}
}

class toggle extends TimerTask {
	private boolean b;

	public toggle(boolean b) {
		this.b = b;
	}

	public void run() {
		b = !b;
	}

	public boolean getShoot() {
		return b;
	}
}

class pewpew extends TimerTask {
	private boolean timesup;
	private ArrayList<enemy> allOptions;
	private ArrayList<Bullet> bullets;
	char[] characters = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	Random rand = new Random();
	int random = (int) (rand.nextInt((characters.length)));

	public pewpew(boolean timesup, ArrayList<enemy> allOptions, ArrayList<Bullet> bullets) {
		this.timesup = timesup;
		this.allOptions = allOptions;
		this.bullets = bullets;
		Collections.shuffle(allOptions);
	}

	public void run() {
		if (timesup) {
			if (allOptions.size() >= 3) {
				for (int i = 0; i < 2; i++) {
					bullets.add(new Bullet(characters[random], allOptions.get(i).getX(), allOptions.get(i).getY(), 350,
							850));
				}
			} else {
				for (int i = 0; i < allOptions.size(); i++) {
					bullets.add(new Bullet(characters[random], allOptions.get(i).getX(), allOptions.get(i).getY(), 350,
							850));
				}
			}
			timesup = false;
		}

		else {
			timesup = true;
		}
	}
}
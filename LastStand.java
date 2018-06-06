package FSE;

import java.io.*;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import java.math.*;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.ImageObserver;

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
	private JFrame frame;
	private String username, typedValue, screen = "menu";
	private boolean[] keys;
	private Image[] backgrounds = new Image[1];
	private Image logo;
	private Image ship1;
	private Image ship2;
	private Image ship3;
	private Image ship4;
	private Image ship5;
	private Image shot2;
	private Image frame0, frame1,frame2,frame3,frame4,frame5,frame6,frame7,frame8,frame9,frame10,frame11,frame12,frame13;
	Image[] explosion = new Image[]{frame0, frame1,frame2,frame3,frame4,frame5,frame6,frame7,frame8,frame9,frame10,frame11,frame12,frame13};
	//private double frameIndex = 0.0;
	private circle empRad = new circle();
	private circle target = new circle();
	private Font menuFont = new Font("Rocket Propelled", Font.TRUETYPE_FONT, 40);
	private Font text = new Font("Falling Sky", Font.TRUETYPE_FONT, 20);
	private Random random = new Random();
	private double[][] starList;
	private Color colour;
	private enemy test = new enemy("a", 0, 0);
	private button[] buttons = new button[4];
	private String[] buttonText = { "PLAY GAME", "INSTRUCTIONS", "HIGH SCORE", "QUIT" };
	final Timer timer;
	private enemy activeTarget = null;// = new enemy("lol", 0, 0);
	private int enemySlot = 0;
	@SuppressWarnings("unchecked")
	private LinkedList<enemy>[] enemies = new LinkedList[26];
	private int level = 1, bombRad, filledEnemies;
	private int targetRad=100;
	private boolean levelFinish = false, bombing;
	private int bombs = 3, emps = 200;
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private ArrayList<attack> b = new ArrayList<attack>();
	private ArrayList<String> basic = new ArrayList<String>();
	private ArrayList<String> intermediate = new ArrayList<String>();
	private ArrayList<String> advanced = new ArrayList<String>();
	private AffineTransform trans = new AffineTransform();
	int inScreenCount=0;
	boolean newTarget=false;
	private bigBoss testerBoss = new bigBoss("Amaaaaaaazing", 0,0);


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
		timer = new Timer(40, this);
		timer.setDelay(1);
		timer.start();
		starList = genScrollStars(450);
		keys = new boolean[KeyEvent.KEY_LAST + 1];
		logo = new ImageIcon("logo.png").getImage();
		ship1 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\ship1.gif").getImage();
		ship2 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\ship2.gif").getImage();
		ship3 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\ship3.gif").getImage();
		ship4 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\ship4.gif").getImage();
		ship5 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\ship5.gif").getImage();
		shot2 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\shot2.png").getImage();

		frame0 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\explosion\\tile000.png").getImage();
		frame1 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\explosion\\tile001.png").getImage();
		frame2 = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\explosion\\tile002.png").getImage();
		frame3 = new ImageIcon("tile003.png").getImage();
		frame4 = new ImageIcon("tile004.png").getImage();
		frame5 = new ImageIcon("tile005.png").getImage();
		frame6 = new ImageIcon("tile006.png").getImage();
		frame7 = new ImageIcon("tile007.png").getImage();
		frame8 = new ImageIcon("tile008.png").getImage();
		frame9 = new ImageIcon("tile009.png").getImage();
		frame10 = new ImageIcon("tile010.png").getImage();
		frame11 = new ImageIcon("tile011.png").getImage();
		frame12 = new ImageIcon("tile012.png").getImage();
		frame13 = new ImageIcon("tile013.png").getImage();

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

	public boolean isAlpha(String name) {
		char[] chars = name.toCharArray();

		for (char c : chars) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}

		return true;
	}

	public void addEnemy(enemy n) {
		// System.out.println("Adding enemy");
		int pos = n.index();
		enemies[pos].add(n);
	}

	public void initEnemies() throws IOException {
		for (int i = 0; i < 26; i++) {
			enemies[i] = new LinkedList<enemy>();
		}
		/*
		 * int index1 = test1.index(); int index2 = test2.index();
		 * enemies[index1].add(test1); enemies[index2].add(test2);
		 */

		Scanner stdin = new Scanner(System.in);
		Scanner inFile = new Scanner(new BufferedReader(new FileReader("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\files\\words.txt")));
		while (inFile.hasNextLine()) {
			String word = inFile.nextLine();
			if (isAlpha(word)) {
				if (word.length() <= 5) {
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

	public boolean checkClose(int x, int y) {
		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (enemy enemyCheck : current) {
				if (Math.abs(enemyCheck.getX() - x) < 5) {
					return true;
				}
				if (Math.abs(enemyCheck.getY() - y) < 5) {
					return true;
				}
			}
		}
		return false;
	}

	public int randX() {
		Random rand = new Random();
		int random = (int) (rand.nextInt((basic.size()) + 1));
		int[] possibleX = new int[4];
		possibleX[0] = -1 * rand.nextInt(100);
		possibleX[1] = 700 + rand.nextInt(100);
		possibleX[2] = rand.nextInt(346);
		possibleX[3] = 355 + rand.nextInt(346);
		return possibleX[rand.nextInt(4)];
	}

	public int randY(int level) {
		Random rand = new Random();
		return -50 - rand.nextInt(226) - level * 25;
	}

	public void makeEnemies() {
		Random rand = new Random();
		switch (level) {
		case 1:
			for (int b = 0; b < 4 * level; b++) {
				int pickedX = randX();
				int pickedY = randY(1);
				int random = (int) (rand.nextInt((basic.size()) + 1));
				String val = basic.get(random).toLowerCase();
				enemy newEnemy = new enemy(val, pickedX, pickedY);
				System.out.println(newEnemy.getValue());
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

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (screen == "menu")
			menu(g);
		else if (screen == "Play Game")
			game(g);
		else if (screen == "Instructions")
			instructions(g);
		else if (screen == "High Scores")
			hsScreen(g);
		else if (screen == "next")
			next(g);
		else
			frame.dispose();
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

	public void checkRad() {
		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (Iterator<enemy> it = current.iterator(); it.hasNext();) {
				enemy n = it.next();
				if (empRad.contains((n.getX() + 29), (n.getY() + 29)) && bombing) {
					if (n.equals(activeTarget))
						activeTarget = null;
					it.remove();
				}
			}
		}

		/*
		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (enemy n : current) {
				if (empRad.contains(n.getX(), n.getY())) {
					if (n.equals(activeTarget))
						activeTarget = null;
					current.remove(n);
				}
			}
		}
		*/
	}

	public void game(Graphics g) {
		//System.out.println(levelCounter + " Wrong: " + levelWrong);
		Graphics2D g2 = (Graphics2D) g;
		int total = 0;
		requestFocusInWindow();
		g.setColor(Color.black);
		g.fillRect(0, 0, 700, 930);
		moveStars(starList, g);
		if (bombing) {
			bombRad ++;
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
		if (levelFinish) {
			makeEnemies();
		}
		g.setFont(menuFont);
		g.setColor(Color.white);

		/*
		testerBoss.move();
		g.drawString(testerBoss.getValue(),testerBoss.getX(),testerBoss.getY());
		testerBoss.attack();
		testerBoss.moveAttack();
		*/
		
		for (Bullet n : bullets){
			g.drawString(Character.toString(n.getLetter()),n.getX(),n.getY());
		}

		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (enemy n : current) {
				//total++;
				n.move();
			}
		}

		if (newTarget){
			targetRad--;
			target.setRad(targetRad);
			drawTarget(g,targetRad);
			if (targetRad==0){
				newTarget=false;
				targetRad=100;
			}

		}

		for (Iterator<attack> it = b.iterator(); it.hasNext();) {
			attack attack = it.next();
			attack.move();
			if (attack.check()) {
				it.remove();
				activeTarget.getAttacks().remove(attack);
			}
		}

		for (attack current : b){
			g.drawImage(shot2,current.getX(),current.getY(),this);
		}


		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (enemy n : current) {
				double rotation = (Math.PI - getAngle(n.getX(), n.getY()));
				double locX = n.getImage().getWidth(this) / 2;
				double locY = n.getImage().getHeight(this) / 2;
				AffineTransform tx = AffineTransform.getRotateInstance(rotation, locX, locY);
				g2.drawImage(ship4, tx, null);
			}
		}
		g.setFont(text);
		g.setColor(Color.white);
		for (int i = 0; i < 26; i++) {
			LinkedList<enemy> current = enemies[i];
			for (enemy n : current) {
				g.drawImage(ship4,n.getX(),n.getY(),this);
				g.drawString(n.getValue(), n.getX()+4, n.getY()+35);
				// System.out.println(total);
			}
		}

		if (activeTarget != null) {
			//System.out.println(activeTarget.getY() + 29);
			g.setColor(Color.red);
			g.drawRect(activeTarget.getX() + 29, activeTarget.getY() + 29, 10, 10);
			g.drawString(activeTarget.getValue(),activeTarget.getX()+4,activeTarget.getY()+35);
		}

		if (activeTarget != null) {
			//System.out.println(activeTarget.getY() + 29);
			g.setColor(Color.red);
			g.drawLine(activeTarget.getX() + 29, activeTarget.getY() + 29, activeTarget.getX() + 29, activeTarget.getY() + 29);
			if (activeTarget.getValue().equals("") && activeTarget.getAttacks().isEmpty()) {// if we finished typing that enemies word
				while(activeTarget.getDead()==false){
					activeTarget.explode(g, this);
				}
				score += activeTarget.getScore() * level;
				enemies[enemySlot].remove(activeTarget);
				activeTarget = null;
			}
		}

		g.setColor(Color.white);
		filledEnemies = 0;
		for (LinkedList<enemy> enemyList : enemies) {
			if (!enemyList.isEmpty())
				filledEnemies++;
		}

		if (filledEnemies < 1) {
			bombing = false;
			bombRad = 0;
			emps = 2;
			bombs = 3;
			levelFinish = true;
			level += 1;
			screen = "next";
		}
	}

	public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void next(Graphics g) {
        String levelStats = "LEVEL "+level;

        String gameHeader =  "Game Stats                        Accuracy";
        String gameLine = "__";
        String gameStats = round(((double)(counter-wrong) / counter) * 100, 2) + "%";

        g.setColor(Color.black);
        g.fillRect(0, 0, 700, 930);
        g.setColor(Color.white);
        g.setFont(menuFont);
        //g.drawString(levelHeader, 50, 50);
        g.drawString(levelStats, 50, 100);
        g.drawString(gameHeader, 50, 140);
        g.drawString(gameLine, 50, 160);
        g.drawString(gameStats, 50, 190);

        g.fillRect(220, 450, 280, 60);
        g.setColor(Color.black);
        g.drawString("NEXT LEVEL", 240, 495);
    }

	public void drawBomb(Graphics g, int rad) {
		g.setColor(Color.white);
		System.out.println(rad);
		//System.out.println(rad);
		g.drawOval(350 - rad, 850 - rad, rad * 2, rad * 2);
	}

	public void drawTarget(Graphics g, int rad){
		g.setColor(Color.orange);
		g.drawOval(activeTarget.getX() - rad + 28 , activeTarget.getY() - rad + 28, rad * 2, rad * 2);
	}

	public void instructions(Graphics g) {
		g.setColor(Color.blue);
		g.fillRect(0, 0, 700, 930);
	}

	public void hsScreen(Graphics g) {
		g.setColor(Color.yellow);
		g.fillRect(0, 0, 700, 930);
	}

	public double getAngle(int x, int y) {
		double diffx = 350 - x;
		double diffy = 850 - y;
		double ang = Math.atan(diffx / diffy);
		return ang;
	}

	public void keyPressed(KeyEvent e) {
		if (screen == "Play Game") {
			//inScreenCount=0;
			counter++;
			levelCounter++;
			boolean inScreen=false;
			for (int i = 0; i < 26; i++) {
				LinkedList<enemy> current = enemies[i];
				for (enemy x : current) {
					if (x.getY()>0){
						if (x.getX()>0 && x.getX()<700){
							inScreen=true;
						}
					}
				}
			}
			if (inScreen){
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
		// System.out.println(e.getX() + ", " + e.getY());
		if (screen == "menu") {
			for (button button : buttons) {
				if (button.contains(e.getX(), e.getY())) {
					screen = button.getVal();
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
	//////////////////////////////////////////////////////////////////////

	public enemy getLowest(LinkedList<enemy> n) {
		int max = 0;
		enemy checker = null;
		for (enemy check : n) {
			int distance = check.getY();
			if (distance > max) {
				max = distance;
				checker = check;
			}
		}

		enemy lowest = checker;
		return lowest;
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
		if (levelFinish == false) {
			int slot = (int) n - 97;// this is finding the index in the list, ascii codes for lower case start at
									// 97, so thats why im subtracting
			if (bullets.contains(n) == true) {// if its in this list which will be enemy bullets
				// levelCounter++;
				// counter++;
				bullets.remove(n);
				return;// end the function here
			}

			else if (!enemies[slot].isEmpty()) {// if there's an enemy that starts with this character
				// System.out.println("Herro");
				activeTarget = getLowest(enemies[slot]);// getting the enemy that is the closest
				if (activeTarget.getY()>0){
					if (activeTarget.getX()<700 && activeTarget.getX()>0){
						typing(n); //used later when im deleting and reorganizing
						enemySlot = slot;//
						newTarget=true;
					}
				}
				else{
					activeTarget=null;
				}
			}

			else {
				// levelCounter++;
				levelWrong++;
				// counter++;
				wrong++;
			}
		}
	}

	public void typing(char n) {
		if (levelFinish == false) {
			if (activeTarget == null) {// if there's not target
				target(n);
			}

			else {
				//newTarget=false;
				if (activeTarget.getValue().charAt(0) == n) { //java.lang.StringIndexOutOfBoundsException
					attack newAttack = new attack(activeTarget.getX()+29,activeTarget.getY()+58);
					b.add(newAttack);
					activeTarget.addAttack(newAttack);
					activeTarget.remove();// removes first letter
					counter++;
						// score
						// target(n);
					}
				 else {
					// counter++;
					wrong++;
					levelWrong++;
				}
			}
		}
	}

	///////////////////////////////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}

class Base {
	protected int x;
	protected int y;
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
		// System.out.println(x);
	}
}

class Bullet extends Base {
	private char letter;
	private int destX, destY;
	private double rad, dx, dy;

	public char getLetter() {
		return letter;
	}

	public Bullet(char n, int x, int y, int destX, int destY) {
		super(x, y);
		letter = n;
		this.destX=destX;
		this.destY=destY;
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
}

class attack extends Base {
	double rad, dx, dy;
	int destx,desty;

	public attack(int x, int y){
		super(317+29,800);
		this.destx=x;
		this.desty=y;
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

	public boolean check(){
		//if (x==destx){
			if (y<=desty){
				return true;
			}
	//	}
		return false;
	}
}

class enemy extends Base {
	private String value;
	private int type;
	private int score;
	private Image pic = new ImageIcon("ship4.gif").getImage();
	private Image[] explosion = new Image[14];
	private ArrayList<attack> targetedAttacks = new ArrayList<attack>();
	double rad, dx, dy, frameRate;
	private boolean dead=false;

	public enemy(String value, int x, int y) {
		super(x + 29, y + 29);
		this.value = value;
		double diffx = 350 - x;
		double diffy = 850 - y;
		rad = Math.atan(diffx / diffy);
		dx = Math.sin(rad) / 11;
		dy = Math.cos(rad) / 11;
		score = value.length();
		for (int i = 0; i < 13; i++) {
			explosion[i] = new ImageIcon("C:\\Users\\kkyyh\\eclipse-workspace\\School 17-18\\src\\FSE\\images\\sprites\\explosion\\tile" + i + ".png").getImage();
		}
		// AffineTransform pic = AffineTransform.getRotateInstance(rad, x+29, y+29);
		// AffineTransformOp op = new AffineTransformOp(pic,
		// AffineTransformOp.TYPE_BILINEAR);
		// type = (int)(Math.random() * 4 + 1);
	}

	public String getValue() {
		return value;
	}

	public boolean getDead(){
		return dead;
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

	public ArrayList<attack> getAttacks(){
		return targetedAttacks;
	}

	public void addAttack(attack a) {
		targetedAttacks.add(a);
	}

	public void move() {
		if (y < 850) {
			translate(dx, dy);
		}
	}

	public int index() {
		char first = value.charAt(0);
		// System.out.println(first - 97);
		return (int) first - 97;
	}

	public void remove() {
		// System.out.println("here");
		value = value.substring(1);
	}

	public void explode(Graphics g, ImageObserver i){
		frameRate+=0.25;
		g.drawImage(explosion[(int)frameRate] ,x, y, i );
		if ((int)frameRate==13){
			dead=true;
		}
	}
}

class bigBoss extends enemy{
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	int[] xCoordinates = new int[]{0,0,0,175,390,630,700,700,700};
	int[] yCoordinates = new int[]{500,685,870,900,900,900,500,685,870	};
	char[] characters = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	Random rand = new Random();
	int random = (int) (rand.nextInt((characters.length)));

	public bigBoss(String value, int x, int y){
		super(value,x,y);
	}

	public void attack(){
		for (int i=0;i<9;i++){
			bullets.add(new Bullet(characters[random],(int)x,(int)y,xCoordinates[i],yCoordinates[i]));
		}
	}

	public void moveAttack(){
		for (Bullet n : bullets){
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

class circle{
	private int cx = 350, cy = 850, rad;
	public boolean contains(int x, int y) {
		double dist = Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
		//System.out.println(dist);
		if (dist <= rad) {
			return true;
		}
		return false;
	}

	public void setRad(int r) {
		rad = r;
		//System.out.println(850-rad);
	}
	public int getRad() {
		return rad;
	}
}
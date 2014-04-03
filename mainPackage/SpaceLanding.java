/*
 * SpaceLanding.java
 * by Christopher Elwell (christopherelwell.com)
 * under MIT License - go ahead and use it however you want. Though, it would be cool to hear from you any comments.
 */

package mainPackage;

import java.applet.*;
import java.awt.*;
import java.util.ArrayList;

public class SpaceLanding extends Applet implements Runnable
{
	double posx, posy; 
	double velx, vely;

	ArrayList lasers;
	// left/right leg positions
	double[][] leftlpos;
	double[][] rightlpos;
	double[][] leftlvel;
	double[][] rightlvel;
	
	int explosionDiameter = 100;
	
	Image buffer;
	Graphics2D b;
	double offset = 0;
	
	Thread t;

	Font standardFont, bigFont;
	
	double timer;
	double bestTime;
	long startTime;
	boolean newRecord;
	
	double levelTransitionPause;
	
	int level;
	
	boolean gameover=false;
	boolean win=false;
	boolean up=false, down=false, left=false, right=false;

	public void init()
	{
		setSize(800,600);
		buffer = createImage(800,600);
		b = (Graphics2D)buffer.getGraphics();
		b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		bigFont = new Font("Courier",Font.BOLD,30);
		b.setFont(standardFont = new Font("Courier",Font.BOLD,16));
		b.setStroke(new BasicStroke(3));
				
		posx = Math.random()*600+150;
		posy = 40;
		velx = 0;
		vely = 0;
		
		lasers = new ArrayList();
		leftlpos = new double[3][2];
		rightlpos = new double[3][2];
		leftlvel = new double[3][2];
		rightlvel = new double[3][2];
		
		
		// leg positions and velocities
		leftlpos[0][0] = posx-10;
		leftlpos[1][0] = posx-20;
		leftlpos[2][0] = posx-10;
		leftlpos[0][1] = posy+35;
		leftlpos[1][1] = posy+45;
		leftlpos[2][1] = posy+55;
		rightlpos[0][0] = posx+10;
		rightlpos[1][0] = posx+20;
		rightlpos[2][0] = posx+10;
		rightlpos[0][1] = posy+35;
		rightlpos[1][1] = posy+45;
		rightlpos[2][1] = posy+55;
		leftlvel[0][0] = leftlvel[1][0] = leftlvel[2][0] = 0;
		leftlvel[0][1] = leftlvel[1][1] = leftlvel[2][1] = 0;
		rightlvel[0][0] = rightlvel[1][0] = rightlvel[2][0] = 0;
		rightlvel[0][1] = rightlvel[1][1] = rightlvel[2][1] = 0;
		
		
		level = 1;
		levelTransitionPause = 3;
		
		timer = 0;
		bestTime = 999.9;
		startTime = System.currentTimeMillis();
		newRecord = false;
		
		t = new Thread(this);
		t.start();
	}
	
	public void paint(Graphics g)
	{
		if (!gameover)
		{
			b.setColor(Color.black);
			b.fillRect(0, 0, 800, 600);
			int px = (int)posx;
			int py = (int)posy;
			if (up)
			{
				b.setColor(new Color((int)(Math.random()*100)+155,(int)(Math.random()*200)+55,(int)(Math.abs(vely)*10)%255));
				int[] flameX = {px-15,px-15,px-7,px,px+7,px+15,px+15};
				int[] flameY = {py+35,py+72,py+62,py+72,py+62,py+72,py+35};
				b.fillPolygon(flameX, flameY, 7);
			}
			b.setColor(Color.lightGray);
			b.fillOval(px-10, py-40, 20, 70);
			
			if (!(vely > 1 || Math.abs(velx) > 1))
			{
				b.fillRect(px+9, py+35, 6, 15);
				b.fillRect(px-3, py+35, 6, 15);
				b.fillRect(px-15, py+35, 6, 15);
				b.setColor(Color.black);
				b.drawRect(px+8, py+34, 8, 17);
				b.drawRect(px-4, py+34, 8, 17);
				b.drawRect(px-16, py+34, 8, 17);
				b.setColor(Color.lightGray);
			}
			/*
			for (int i=0; i<leftlpos.length; i++)
			{
				b.fillOval((int)(leftlpos[i][0])-3, (int)(leftlpos[i][1])-3, 6, 6);
				b.fillOval((int)(rightlpos[i][0])-3, (int)(rightlpos[i][1])-3, 6, 6);
			}
			// connect the joints of legs
			b.drawLine((int)(leftlpos[0][0]), (int)(leftlpos[0][1]), (int)(leftlpos[1][0]), (int)(leftlpos[1][1]));
			b.drawLine((int)(leftlpos[1][0]), (int)(leftlpos[1][1]), (int)(leftlpos[2][0]), (int)(leftlpos[2][1]));
			b.drawLine((int)(rightlpos[0][0]), (int)(rightlpos[0][1]), (int)(rightlpos[1][0]), (int)(rightlpos[1][1]));
			b.drawLine((int)(rightlpos[1][0]), (int)(rightlpos[1][1]), (int)(rightlpos[2][0]), (int)(rightlpos[2][1]));
			*/
			
			b.fillRect(px-15, py, 30, 35);
			b.setColor(Color.darkGray);
			b.fillRect(px-15, py+32, 30, 2);
			b.setColor(Color.black);
			//b.fillOval(px-30, py-75, 30, 70);
			//b.fillOval(px, py-75, 30, 70);
			if ((left && vely<=0) || (right && vely>0))
			{
				b.setColor(Color.lightGray);
				int[] wingX = {px-15,px-25,px-15};
				int[] wingY = {py+10,py+35,py+35};
				b.fillPolygon(wingX, wingY, 3);
				b.setColor(Color.darkGray);
				b.fillRect(px-25, py+32, 10, 2);
			}
			else if ((right && vely<=0) || (left && vely>0))
			{
				b.setColor(Color.lightGray);
				int[] wingX = {px+15,px+25,px+15};
				int[] wingY = {py+10,py+35,py+35};
				b.fillPolygon(wingX, wingY, 3);
				b.setColor(Color.darkGray);
				b.fillRect(px+15, py+32, 10, 2);
			}
			else
			{
				b.setColor(Color.lightGray);
				int[] wingX = {px+15,px+25,px+15};
				int[] wingY = {py+10,py+35,py+35};
				b.fillPolygon(wingX, wingY, 3);
				int[] wing2X = {px-15,px-25,px-15};
				int[] wing2Y = {py+10,py+35,py+35};
				b.fillPolygon(wing2X, wing2Y, 3);
				b.setColor(Color.darkGray);
				b.fillRect(px-25, py+32, 10, 2);
				b.fillRect(px+15, py+32, 10, 2);
			}
			
			b.setColor(Color.green);
			if (py+72 < 0)
			{
				int[] arrowX = {px,px-15,px-5,px-5,px+5,px+5,px+15};
				int[] arrowY = {5,20,20,35,35,20,20};
				b.drawPolygon(arrowX, arrowY, 7);
				b.drawString(""+Math.abs(py), px-(Integer.toString(py).length()*5)+6, 50);
			}
			b.setColor(Color.red);
			for (int i=0; i<lasers.size(); i++)
			{
				Point p = (Point)lasers.get(i);
				b.fillRect(p.x-4, p.y, 5, 2);
			}
			if (win)
			{
				// starry background
				b.setColor(Color.white);
				for (int r=0; r<100; r++)
				{
					b.drawRect((int)(Math.random()*800), (int)(Math.random()*600), 1, 1);
				}
				b.setFont(bigFont);
				if (newRecord)
				{
					if (levelTransitionPause - (int)levelTransitionPause < .5)
						b.drawString("NEW RECORD!!!", 292, 235);
				}
				b.drawString("- SAFE LANDING -", 260, 265);
				b.drawString("CONGRATULATIONS!", 260, 295);
				b.setFont(standardFont);
				levelTransitionPause -= .01;
				b.drawString("Level "+(level+1)+" will begin in "+((int)(levelTransitionPause))+" seconds.", 250, 350);
				if (levelTransitionPause < 0)
				{
					win=false; 
					posx=Math.random()*600+150; 
					posy=40; 
					velx = vely = 0; 
					startTime = System.currentTimeMillis();
					levelTransitionPause = 3;
					level++;
				}
			}
		}
		else
		{
			b.setColor(Color.orange);
			b.fillOval((int)posx-explosionDiameter/2, (int)posy-explosionDiameter/2, explosionDiameter, explosionDiameter);
			explosionDiameter++;
			if (System.currentTimeMillis() % 40 == 0)
				b.setColor(Color.red);
			else
				b.setColor(Color.black);
			b.setFont(bigFont);
			b.drawString("YOU BLEW UP!", 300, 290);
			if (offset == 0)
				offset = 25;
			else if (offset > 0)
				offset = -Math.abs(offset)*.99;
			else
				offset = Math.abs(offset)*.99;
			b.setFont(standardFont);
			b.drawString("Press the spacebar to restart.", 260, 330);
		}
		
		b.setColor(new Color(150,150,100));
		b.fillRect(0, 500, 800, 100);
		b.setColor(Color.green);
		b.drawString("Time: "+timer, 15, 30);
		b.drawString("Fastest Time: "+bestTime, 600, 30);
		b.drawString("Spacebar to restart", 600, 480);
		b.drawString("Level: "+level, 15, 480);
		
		if (!this.hasFocus())
		{
			b.setColor(Color.black);
			b.fillRect(285, 270, 210, 30);
			b.setColor(Color.white);
			b.drawRect(285, 270, 210, 30);
			b.setColor(Color.green);
			b.drawString("Click to Activate.", 304, 290);
		}
		
		g.drawImage(buffer,0,(int)offset,this);
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void process()
	{
		timer =((double)(System.currentTimeMillis()-startTime))/1000;
		posx += velx;
		posy += vely;
		//air friction
		velx *= .999;
		//gravity
		vely += .01;
		
		/*
		leftlpos[0][0] = posx-10;
		leftlpos[0][1] = posy+35;
		rightlpos[0][0] = posx+10;
		rightlpos[0][1] = posy+35;
		for (int i=1; i<leftlpos.length; i++)
		{
			leftlpos[i][0] += leftlvel[i][0];
			leftlpos[i][1] += leftlvel[i][1];
			rightlpos[i][0] += rightlvel[i][0];
			rightlpos[i][1] += rightlvel[i][1];
			leftlvel[i][1] += .01; 
			rightlvel[i][1] += .01;
			leftlvel[i][0] *= .999; 
			rightlvel[i][0] *= .999;
		}
		
		// springs on legs
		double dx = leftlpos[0][0] - leftlpos[1][0];
		double dy = leftlpos[0][1] - leftlpos[1][1];
		double vlen = Math.sqrt(dx*dx+dy*dy);
		double disp = .999*(vlen-10)*dx/vlen;
		//leftlpos[0][0] += disp/2*dx;
		leftlpos[1][0] += -.2*dx;
		//leftlpos[0][1] += disp/2*dy;
		leftlpos[1][1] += -.2*(dy/vlen);
		*/
		
		if (up)
			vely -= .07;
		if(down)
			vely += .04;
		if (left)
			velx -= .06;
		if (right)
			velx += .06;
		
		
		if (posy > 450)
		{
			if (vely > 1 || Math.abs(velx) > 1)
				gameover = true;
			else
			{
				win = true;
				newRecord = false;
				if (timer < bestTime)
				{
					bestTime = timer;
					newRecord = true;
				}
			}
		}
		
		for (int i=0; i<lasers.size(); i++)
		{
			Point p = (Point)lasers.get(i);
			p.x+=4;
			if (p.x > 800)
			{
				lasers.remove(i);
				i--;
			}
		}
		
		for (int i=0; i<lasers.size(); i++)
		{
			Point p = (Point)lasers.get(i);
			if (posy-35 < p.y && posy+45 > p.y)
			{
				if (posx-15 < p.x && posx+15 > p.x)
					gameover=true;
			}
		}
		
		if (Math.random() < level*.01)
			lasers.add(new Point(0,(int)(Math.random()*300)+90));
	}
	
	
	public boolean keyDown(Event e, int k)
	{
		switch (k)
		{
		case 32: 
			if (!win) 
			{
				gameover=false; win=false; posx=Math.random()*600+150; posy=40; velx = vely = 0; startTime = System.currentTimeMillis(); explosionDiameter = 100;
			}
		break;
		case 1004: up=true;
		break;
		case 1005: down=true;
		break;
		case 1006: left=true;
		break;
		case 1007: right=true;
		}
		
		return true;
	}
	
	public boolean keyUp(Event e, int k)
	{
		switch (k)
		{
		case 32: up=false;
		break;
		case 1004: up=false;
		break;
		case 1005: down=false;
		break;
		case 1006: left=false;
		break;
		case 1007: right=false;
		}
		
		return true;
	}
	
	public void run()
	{
		while (true)
		{
			if (!gameover && !win && this.hasFocus())
				process();
			try {t.sleep(10);} catch (Exception e) {}
			repaint();
		}
	}

}

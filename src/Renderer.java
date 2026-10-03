import javax.swing.*;
import java.awt.*;

public class Renderer extends JPanel {
    int screenWidth, screenHeight;
    Grid grid;

    public Renderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(grid.redrawImage(), 0, 0, null);

        g.setColor(Color.WHITE);
        for (int i = 0; i < grid.antArray.length; i++) {
            g.fillOval((int) grid.antArray[i].x-Ant.antRadius, (int) grid.antArray[i].y-Ant.antRadius, Ant.antRadius*2, Ant.antRadius*2);
        }

        g.setColor(Color.DARK_GRAY);
        g.fillOval((Ant.home.x*grid.chunkSize)-10, (Ant.home.y*grid.chunkSize)-10,20, 20);

        g.setColor(Color.WHITE);
        g.drawString("" + Ant.totalCollectedFood, Ant.home.x*grid.chunkSize-5, Ant.home.y*grid.chunkSize+5);
    }

    public static void main(String[] args) {
        int screenWidth = 720;
        int screenHeight = 720;
        boolean showAntPerspective = true;

        JFrame frame = new JFrame("Ants");
        Renderer panel  = new Renderer(screenWidth,screenHeight);
        panel.grid = new Grid(screenWidth, screenHeight, 2);

        panel.setBackground(Color.BLACK);
        panel.setOpaque(true);

        frame.setSize(screenWidth, screenHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);

        new Timer(16, e -> {
            panel.repaint();
            panel.grid.update();

            System.out.println(panel.grid.antArray[0].desiredChunk.position + " " + Ant.totalCollectedFood);
            gridChunk g = panel.grid.antArray[0].desiredChunk;
            System.out.println(g.foodValue + " | " + g.toHomeStrength + ", " + g.toHomeStrength);
            System.out.println("Home chunk is home: " + panel.grid.gridArray[Ant.home.x][Ant.home.y].isHomeChunk);
            System.out.println("Desired chunk is home: " + g.isHomeChunk);
        }).start();
    }
}

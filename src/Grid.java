import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Grid {
    int screenWidth, screenHeight;
    int gridWidth, gridHeight;
    int chunkSize;

    gridChunk[][] gridArray;
    Ant[] antArray;

    BufferedImage image;

    public Grid(int screenWidth, int screenHeight, int chunkSize) {
        Random random = new Random();
        int numAnts = 50;
        Ant.FOV = 120; //Degrees
        Ant.antRadius = 2; //Pixels
        Ant.viewDistance = 50; //In chunks
        Ant.totalCollectedFood = 0;

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.gridWidth = screenWidth/chunkSize;
        this.gridHeight = screenHeight/chunkSize;
        this.chunkSize = chunkSize;

        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);

        //Initializing gridArray
        gridArray = new gridChunk[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                gridArray[x][y] = new gridChunk(new Point(x, y));
            }
        }

        Ant.home = getGridCoordinates((double) screenWidth /2, (double) screenHeight /2); //Home chunk
        gridArray[Ant.home.x][Ant.home.y].isHomeChunk = true;

        //Initializing antArray
        antArray = new Ant[numAnts];
        for (int i = 0; i < numAnts; i++) {
            antArray[i] = new Ant(Ant.home.x*chunkSize, Ant.home.y*chunkSize, new Vector2(1, Math.toRadians(random.nextInt(360))));
        }

        createFoodCluster(new Point(160, 220), 5);
    }

    public void createFoodCluster(Point foodClusterCenter, int foodClusterRadius){
        //The number of collectible foods per chunk
        int foodValue = 5;

        //Calculates Euclidean distance to generate a circle
        for (int y = foodClusterCenter.y - foodClusterRadius; y <= foodClusterCenter.y + foodClusterRadius; y++) {
            //using pythagoras, radius^2 = y^2 + sx^2,
            //so sx = sqrt(radius^2-y^2)
            int dy = y - foodClusterCenter.y;
            int sx = (int) Math.sqrt((foodClusterRadius*foodClusterRadius)-(dy*dy));

            for (int x = foodClusterCenter.x - sx; x <= foodClusterCenter.x + sx; x++) {
                //Making sure chunk is inside the grid
                if ((x >= 0 && x < gridWidth) && (y >= 0) && (y < gridHeight)) {
                    gridArray[x][y].foodValue = foodValue;
                }
            }
        }
    }

    public void update() {
        //Updating grid pheromones
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                gridArray[x][y].updatePheromones();
            }
        }

        //Updating ants
        for (Ant ant : antArray) {
            ant.update(screenWidth, screenHeight, this);
        }
    }

    public Point getGridCoordinates(double x, double y) {
        if ((x >= 0 && x < screenWidth) && (y >= 0) && (y < screenHeight)) {
            return new Point(
                    (int) x / this.chunkSize,
                    (int) y / this.chunkSize
            );
        }
        else return new Point(-1, -1);
    }

    public void fillChunk(Point p, Color color, int alpha) {
        int pixelOffset = 0; //optional border around each chunk. Makes it easier to count

        if ((p.x >= 0 && p.x < gridWidth) && (p.y >= 0) && (p.y < gridHeight)) {
            for (int x = (p.x*chunkSize) + pixelOffset; x < ((p.x*chunkSize) + (chunkSize-pixelOffset)); x++) {
                for (int y = (p.y*chunkSize) + pixelOffset; y < ((p.y*chunkSize) + (chunkSize-pixelOffset)); y++) {
                    int argb = ((alpha << 24) | (color.getRGB() & 0x00FFFFFF));
                    image.setRGB(x, y, argb); //Pixel coordinates
                }
            }
        }
    }

    public BufferedImage redrawImage() {
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(Color.BLACK);

        g2d.fillRect(0,0, screenWidth,screenHeight);

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                double toFoodStrength = gridArray[x][y].toFoodStrength;
                double toHomeStrength = gridArray[x][y].toHomeStrength;
                int foodValue = gridArray[x][y].foodValue;

                if (foodValue > 0) {
                    fillChunk(new Point(x, y), Color.ORANGE, 255);
                }

                else if ((toFoodStrength > 0) && (toHomeStrength > 0)) {
                    int alpha = (int)(255*((toFoodStrength+toHomeStrength)/2));
                    fillChunk(new Point(x, y), new Color((int)(255*toHomeStrength), (int)(255*toFoodStrength), 0), alpha);
                }

                else if ((toFoodStrength == 0) && (toHomeStrength > 0)) {
                    int alpha = (int)(255*toHomeStrength);
                    fillChunk(new Point(x, y), Color.RED, alpha);
                }

                else if ((toFoodStrength > 0) && (toHomeStrength == 0)) {
                    int alpha = (int)(255*toFoodStrength);
                    fillChunk(new Point(x, y), Color.GREEN, alpha);
                }
            }
        }
//        for (Ant a : antArray) {
//            Point p = getGridCoordinates(a.x, a.y);
//            double currentBearing = Math.toDegrees(a.velocity.bearing);
//            double startAngle = currentBearing - (Ant.FOV / 2);
//            double endAngle = currentBearing + (Ant.FOV / 2);
//
//            double angleStep = 0.5;
//
//            for (int currentRadius = 0; currentRadius <= Ant.viewDistance; currentRadius++) {
//                for (double currentAngle = startAngle; currentAngle <= endAngle; currentAngle += angleStep) {
//                    double rad = Math.toRadians(currentAngle);
//
//                    int x = (int) Math.round(p.x + currentRadius * Math.cos(rad));
//                    int y = (int) Math.round(p.y + currentRadius * Math.sin(rad));
//
//                    if ((x >= 0) && (x < gridWidth) && (y >= 0) && (y < gridHeight)) {
//                        fillChunk(new Point(x, y), Color.LIGHT_GRAY, 100);
//                    }
//                }
//            }
//        }


        return image;
    }

}

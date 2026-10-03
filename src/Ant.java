import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Ant {
    static Point home; //Home chunk
    static double FOV;
    static int antRadius;
    static int viewDistance; //In chunks
    static int totalCollectedFood;

    gridChunk desiredChunk;
    Vector2 velocity;
    double x, y;
    double wanderTendency;
    boolean hasFood;

    public Ant (double x, double y, Vector2 velocity) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.desiredChunk = new gridChunk(new Point(-1, -1));
        this.wanderTendency = 0.4;
    }

    public double calculateBearing(Point p, Point q) {
        double dx = q.x - p.x;
        double dy = q.y - p.y;

        return Math.atan2(dy, dx);
    }

    public void update(int screenWidth, int screenHeight, Grid grid) {
        //Updating positions based on velocity. Makes ants move
        this.x += velocity.x;
        this.y += velocity.y;
        if (this.desiredChunk.position.equals(new Point(-1, -1))) {
            this.wanderTendency = 0.2;
        }
        else wanderTendency = 0.1;

        double bearingVariation = this.velocity.bearing + ((Math.random() - 0.5) * this.wanderTendency);
        this.velocity.updateBearing(bearingVariation);

//        Point currentMouse = MouseInfo.getPointerInfo().getLocation();
//        if ((currentMouse.x >= 0) && (currentMouse.x < screenWidth) && (currentMouse.y >= 0) && (currentMouse.y < screenHeight)) {
//            double dx = currentMouse.x - this.x;
//            double dy = currentMouse.y - this.y;
//
//            System.out.println(Math.toDegrees(Math.atan2(dy, dx)));
//            this.velocity.updateBearing(Math.atan2(dy, dx));
//        }

        //Collisions
        //Horizontal panel collisions
        if ((this.x > screenWidth - Ant.antRadius) || (this.x < 0)) {
            double updateBearing = this.velocity.bearing + Math.toRadians(180);
            updateBearing = (2 * Math.PI + updateBearing) % (2 * Math.PI);

            this.x = (this.x > screenWidth - Ant.antRadius) ? screenWidth - Ant.antRadius : 0;
            this.velocity.updateBearing(updateBearing);
        }

        //Vertical panel collisions
        else if ((this.y > screenHeight - Ant.antRadius) || (this.y < 0)) {
            double updateBearing = 2 * this.velocity.bearing + Math.toRadians(180);
            updateBearing = (2 * Math.PI + updateBearing) % 2 * Math.PI;

            this.y = (this.y > screenHeight - Ant.antRadius) ? screenHeight - Ant.antRadius : 0;
            this.velocity.updateBearing(updateBearing);
        }

        Point here = grid.getGridCoordinates(this.x, this.y);
        if ((here.x >= 0) && (here.x < grid.gridWidth) && (here.y >= 0) && (here.y < grid.gridHeight)) {
            if (!this.hasFood) {
                grid.gridArray[here.x][here.y].toHomeStrength = 1;
            } else {
                grid.gridArray[here.x][here.y].toFoodStrength = 1;
            }
        }

        Set<gridChunk> antFOV = new HashSet<>();

        double currentBearing = Math.toDegrees(this.velocity.bearing);
        double startAngle = currentBearing - (Ant.FOV / 2);
        double endAngle = currentBearing + (Ant.FOV / 2);

        double angleStep = 0.5;

        for (int currentRadius = 0; currentRadius <= Ant.viewDistance; currentRadius++) {
            for (double currentAngle = startAngle; currentAngle <= endAngle; currentAngle += angleStep) {
                double rad = Math.toRadians(currentAngle);

                int x = (int) Math.round(here.x + currentRadius * Math.cos(rad));
                int y = (int) Math.round(here.y + currentRadius * Math.sin(rad));

                if (((x >= 0) && (x < grid.gridWidth) && (y >= 0) && (y < grid.gridHeight)) &&
                ((grid.gridArray[x][y].foodValue > 0) || (grid.gridArray[x][y].toFoodStrength > 0) || (grid.gridArray[x][y].toHomeStrength > 0) ||
                (grid.gridArray[x][y].isHomeChunk))) {
                    antFOV.add(grid.gridArray[x][y]);
                }
            }
        }

        //Checking all ants' perspectives.
        //Filter antFOV chunks to find food or pheromones.
        //targetArray contains the closest food[0], toFoodPheromone[1], toHomePheromone[2], and targetArray[3] contains homeChunk if perceived. Position (-1, -1) if not in FOV.
        gridChunk[] targetArray = new gridChunk[4];
        for (int i = 0; i < targetArray.length; i++) {
            targetArray[i] = new gridChunk(new Point(-1, -1));
        }

        double shortestFood = Double.MAX_VALUE;
        double shortestToFood = 0;
        double shortestToHome = 0;

        for (gridChunk g : antFOV) {
            double dx = this.x - (g.position.x*grid.chunkSize);
            double dy = this.y - (g.position.y*grid.chunkSize);
            double distanceToCurrentChunk = Math.hypot(dx, dy);

            if ((g.foodValue > 0) && (distanceToCurrentChunk < shortestFood)) { //Food found in antFOV
                shortestFood = distanceToCurrentChunk;
                targetArray[0] = g;
            } else if (g.isHomeChunk) { //homeChunk found in antFOV
//                System.out.println("homeChunk found in antFOV");
                targetArray[3] = g;
            } else if ((g.toFoodStrength > 0) && (distanceToCurrentChunk > shortestToFood)) { //toFood Pheromones found in antFOV
                shortestToFood = distanceToCurrentChunk;
                targetArray[1] = g;
            } else if ((g.toHomeStrength > 0) && (distanceToCurrentChunk > shortestToHome)) { //toHome Pheromones found in antFOV
                shortestToHome = distanceToCurrentChunk;
                targetArray[2] = g;
            }
        }
        int intersectionThreshold = 10; //Pixel error to count as arriving at desiredChunk

        if ((this.desiredChunk.position.equals(new Point(-1, -1)))) {

            if ((!this.hasFood) && (!targetArray[0].position.equals(new Point(-1, -1)))) { //Doesn't have food and food found, so direct towards food
                desiredChunk = targetArray[0];
                System.out.println();
            }
            else if ((this.hasFood) && (!targetArray[3].position.equals(new Point(-1, -1)))) { //Has food and homeChunk found, so direct towards home
                desiredChunk = targetArray[3];
                System.out.println();
            }
            else if ((!this.hasFood) && (targetArray[0].position.equals(new Point(-1, -1)))) { //Doesn't have food and no food found, so looking for toFoodPheromones
                desiredChunk = targetArray[1];
                System.out.println();
            }
            else if ((this.hasFood) && (targetArray[3].position.equals(new Point(-1, -1)))) { //Has food and homeChunk not found, so looking for toHome pheromones
                desiredChunk = targetArray[2];
                System.out.println();
            }
        }
        else {
            double desiredLocationDamping = 0.1;
            double idealBearing = calculateBearing(new Point((int)this.x, (int)this.y),
                new Point((desiredChunk.position.x*grid.chunkSize), (desiredChunk.position.y*grid.chunkSize)));
            double distanceDesiredChunk = Math.hypot(this.x - (desiredChunk.position.x*grid.chunkSize), this.y - (desiredChunk.position.y*grid.chunkSize));

            //Adjust node velocity bearing to direct it towards target
            double deltaBearing = (idealBearing-this.velocity.bearing + Math.PI*3) % (Math.PI*2) - (Math.PI);

            if (deltaBearing > 0) {
                this.velocity.updateBearing(this.velocity.bearing + ((Math.random()*desiredLocationDamping)));
            }
            else this.velocity.updateBearing(this.velocity.bearing - ((Math.random()*desiredLocationDamping)));

//            double delta = idealBearing - this.velocity.bearing;
//
//            // Normalize to range (-π, π]
//            delta = ((delta + Math.PI) % (2 * Math.PI)) - Math.PI;
//
//            double step = Math.signum(delta) * (Math.random() * desiredLocationDamping);
//            this.velocity.updateBearing(this.velocity.bearing + step);

            if ((distanceDesiredChunk < intersectionThreshold) && (desiredChunk.foodValue > 0)) { //Looking for food
                grid.gridArray[desiredChunk.position.x][desiredChunk.position.y].foodValue--;
                this.desiredChunk = new gridChunk(new Point(-1, -1));
                this.hasFood = true;

                double updateBearing = this.velocity.bearing + Math.toRadians(180);
                updateBearing = (2 * Math.PI + updateBearing) % (2 * Math.PI);
                this.velocity.updateBearing(updateBearing);

            }
            else if ((distanceDesiredChunk < grid.chunkSize*5) && (desiredChunk.isHomeChunk) && (this.hasFood)) {
                System.out.println("Intersected with homeChunk");
                totalCollectedFood++;
                this.desiredChunk = new gridChunk(new Point(-1, -1));
                this.hasFood = false;

                double updateBearing = this.velocity.bearing + Math.toRadians(180);
                updateBearing = (2 * Math.PI + updateBearing) % (2 * Math.PI);
                this.velocity.updateBearing(updateBearing);

            }
            else if ((distanceDesiredChunk < intersectionThreshold) && (desiredChunk.toFoodStrength > 0)) { //Looking for toFoodPheromones
                this.desiredChunk = new gridChunk(new Point(-1, -1));
            }
            else if ((distanceDesiredChunk < intersectionThreshold) && (desiredChunk.toHomeStrength > 0)) { //Looking for toHomePheromones
                this.desiredChunk = new gridChunk(new Point(-1, -1));
            }
        }



    }
}

import java.awt.*;

public class gridChunk {
    Point position;
    //foodValue of 0 means no food on that chunk. If foodValue > 0, it means there are that many collectible foods on that chunk.
    int foodValue;
    double toHomeStrength;
    double toFoodStrength;
    boolean isHomeChunk;

    //To keep track of when the pheromone will go back to 0.
    static double pheromoneDissipationRate = 0.999;

    public gridChunk(Point position) {
        this.position = position;
        //When a new chunk is initialized, it does not contain any pheromones or food
        this.foodValue = 0;
        this.toHomeStrength = 0;
        this.toFoodStrength = 0;
        this.isHomeChunk = false;
    }

    public void updatePheromones() {
        this.toFoodStrength = (this.toFoodStrength < 0.1) ? 0 : this.toFoodStrength * pheromoneDissipationRate;
        this.toHomeStrength = (this.toHomeStrength < 0.1) ? 0 : this.toHomeStrength * pheromoneDissipationRate;
    }
}

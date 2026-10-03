public class Vector2 {
    double magnitude;
    double x, y;
    double bearing;

    public Vector2(double magnitude, double bearing) {
        this.magnitude = magnitude;
        this.x = magnitude * Math.cos(bearing);
        this.y = -magnitude * Math.sin(bearing);
        this.bearing = bearing;
    }

    public void updateBearing(double bearing) {
        if (bearing > (2*Math.PI)) {
            bearing -= (2*Math.PI);
        }
        else if (bearing < 0){
            bearing += (2*Math.PI);
        }
        this.x = this.magnitude * Math.cos(bearing);
        this.y = this.magnitude * Math.sin(bearing);
        this.bearing = bearing;
    }
}
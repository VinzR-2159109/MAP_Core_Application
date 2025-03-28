package be.uhasselt.dwi_application.model.basic;

public class HandLightBox {
    private final int xStart, xStop;
    private final int yStart, yStop;
    private final int xAvgHand, yAvgHand;

    public HandLightBox(Position thumbTip, Position pinkyTip, Position middleFingerTip, Position wrist, Position avgHandPosition, int gridSize) {
        double positionXLeft = thumbTip.getX();
        double positionXRight = pinkyTip.getX();
        double positionYTop = middleFingerTip.getY();
        double positionYBottom = wrist.getY();

        this.xStart = 43 - (int) (positionXLeft / gridSize);
        this.xStop  = 43 - (int) (positionXRight / gridSize);
        this.yStart = 31 - (int) (positionYBottom / gridSize);
        this.yStop  = 31 - (int) (positionYTop / gridSize);

        this.xAvgHand = 43 - (int) (avgHandPosition.getX() / gridSize);
        this.yAvgHand = 31 - (int) (avgHandPosition.getY() / gridSize);
    }

    public int getXStart() { return Math.min(xStart, xStop); }
    public int getXStop()  { return Math.max(xStart, xStop); }
    public int getYStart() { return Math.min(yStart, yStop); }
    public int getYStop()  { return Math.max(yStart, yStop); }

    public int getXAvgHandRange(){
        return xAvgHand;
    }

    public int getYAvgHandRange(){
        return yAvgHand;
    }
}

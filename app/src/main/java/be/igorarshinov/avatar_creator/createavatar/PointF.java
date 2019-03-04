package be.igorarshinov.avatar_creator.createavatar;

@SuppressWarnings("DefaultFileTemplate")
class PointF {

    private final int x;

    private final int y;

    PointF(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int X() {
        return x;
    }

    int Y() {
        return y;
    }
}

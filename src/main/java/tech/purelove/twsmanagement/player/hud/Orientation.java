package tech.purelove.twsmanagement.player.hud;

import org.jetbrains.annotations.NotNull;

public enum Orientation {
    N(180, Axis.Z, -1), E(-90, Axis.X, 1), S(0, Axis.Z, 1), W(90, Axis.X, -1);

    public final Axis axis;
    public final int add;
    public final int yaw;

    Orientation(int yaw, @NotNull Axis axis, int add) {
        this.yaw = yaw;
        this.add = add;
        this.axis = axis;
    }

    public static Orientation getOrientation(float yaw) {
        if (yaw < 0)
            yaw += 360;

        if (yaw >= 315 || yaw < 45)
            return S;
        else if (yaw < 135)
            return W;
        else if (yaw < 225)
            return N;
        else if (yaw < 315)
            return E;

        return N;
    }

    public enum Axis {
        X, Y, Z
    }
}
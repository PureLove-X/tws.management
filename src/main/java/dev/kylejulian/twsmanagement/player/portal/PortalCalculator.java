package dev.kylejulian.twsmanagement.player.portal;

public final class PortalCalculator {

    public final int netherX;
    public final int netherZ;
    public final String tunnel;
    public final String facing;
    public final String tunnelCode;

    private PortalCalculator(int x, int z, String tunnel, String facing) {
        this.netherX = x;
        this.netherZ = z;
        this.tunnel = tunnel;
        this.facing = facing;
        this.tunnelCode = tunnel + " " +
                (tunnel.equals("EAST") || tunnel.equals("WEST") ? x : z);

    }

    public static PortalCalculator calculate(
            double overworldX,
            double overworldZ,
            int hubRadiusNether
    ) {
        int x = (int) Math.round(overworldX / 8.0);
        int z = (int) Math.round(overworldZ / 8.0);

        String tunnel;
        String facing;

        boolean eastWest = Math.abs(x) >= Math.abs(z);

        if (eastWest) {
            // EAST / WEST tunnel
            tunnel = (x >= 0) ? "EAST" : "WEST";
            facing = "North/South";

            // Keep portal outside hub, clamp X only
            if (Math.abs(x) < hubRadiusNether) {
                x = (x >= 0 ? hubRadiusNether : -hubRadiusNether);
            }
        } else {
            // NORTH / SOUTH tunnel
            // NOTE: tunnel is named by direction traveled TOWARD the hub
            tunnel = (z >= 0) ? "SOUTH" : "NORTH";
            facing = "East/West";

            // Keep portal outside hub, clamp Z only
            if (Math.abs(z) < hubRadiusNether) {
                z = (z >= 0 ? hubRadiusNether : -hubRadiusNether);
            }
        }

        return new PortalCalculator(x, z, tunnel, facing);
    }


    public static OverworldResult calculateOverworld(
            double netherX,
            double netherZ
    ) {
        int owX = (int) Math.round(netherX * 8);
        int owZ = (int) Math.round(netherZ * 8);

        return new OverworldResult(owX, owZ);
    }
    public static final class OverworldResult {
        public final int overworldX;
        public final int overworldZ;

        private OverworldResult(int x, int z) {
            this.overworldX = x;
            this.overworldZ = z;
        }
    }

}

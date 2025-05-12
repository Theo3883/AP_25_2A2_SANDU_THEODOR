package org.main;

public class RouteInfo {
    private int friendlyCount;
    private int neutralCount;
    private int enemyCount;

    public void incrementTypeCount(Type type) {
        switch (type) {
            case FRIENDLY -> friendlyCount++;
            case NEUTRAL -> neutralCount++;
            case ENEMY -> enemyCount++;
        }
    }

    public int getFriendlyCount() {
        return friendlyCount;
    }

    public int getNeutralCount() {
        return neutralCount;
    }

    public int getEnemyCount() {
        return enemyCount;
    }
}
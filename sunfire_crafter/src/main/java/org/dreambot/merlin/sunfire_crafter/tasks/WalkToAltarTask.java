package org.dreambot.merlin.sunfire_crafter.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.merlin.common.WaitTimer;
import org.dreambot.merlin.common.WalkingUtils;

public class WalkToAltarTask extends TaskNode {
  private static final int MAX_ALTAR_DIST = 5;
  private static final Tile[] ALTAR_TILES = {
    new Tile(1699, 3085), new Tile(1699, 3086), new Tile(1699, 3087), new Tile(1699, 3088),
    new Tile(1700, 3085), new Tile(1700, 3086), new Tile(1700, 3087), new Tile(1700, 3088),
  };

  private final WaitTimer waitTimer = new WaitTimer(2000, 3000);
  private Tile altarTile = randomAltarTile();
  private boolean reachedAltarTile;

  @Override
  public boolean accept() {
    if (Players.getLocal().distance(altarTile) <= MAX_ALTAR_DIST) {
      reachedAltarTile = true;
      return false;
    }

    if (reachedAltarTile) {
      altarTile = randomAltarTile();
      reachedAltarTile = false;
    }

    return true;
  }

  @Override
  public int execute() {
    Logger.info("Walking to Shrine of Ralos tile " + altarTile);
    WalkingUtils.walkToTile(altarTile);

    return waitTimer.next();
  }

  private Tile randomAltarTile() {
    return ALTAR_TILES[Calculations.random(0, ALTAR_TILES.length - 1)];
  }
}

package org.dreambot.merlin.sunfire_crafter.tasks;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.merlin.common.WaitTimer;

public class CraftRunesTask extends TaskNode {
  private static final int MAX_ALTAR_DIST = 5;
  private static final String ALTAR_NAME = "Shrine of Ralos";
  private static final String COLOSSAL_POUCH = "Colossal pouch";
  private static final String PURE_ESSENCE = "Pure essence";
  private static final String SUNFIRE_SPLINTERS = "Sunfire splinters";
  private static final long CRAFT_TIMEOUT_MS = 5000;
  private static final long EMPTY_POUCH_TIMEOUT_MS = 3000;
  private final WaitTimer waitTimer = new WaitTimer(2000, 3000);

  @Override
  public boolean accept() {
    return GameObjects.closest(ALTAR_NAME) != null
        && Players.getLocal().distance(GameObjects.closest(ALTAR_NAME)) <= MAX_ALTAR_DIST;
  }

  @Override
  public int execute() {
    GameObject altar = GameObjects.closest(ALTAR_NAME);

    if (altar == null) {
      Logger.error("Shrine of Ralos not found.");
      return -1;
    }

    for (int pouchEmptyCount = 0; pouchEmptyCount < 3; pouchEmptyCount++) {
      if (!craftRunes(altar)) {
        Logger.error("Failed to craft sunfire runes.");
        return -1;
      }

      if (pouchEmptyCount < 2 && !emptyColossalPouch()) {
        Logger.error("Failed to empty colossal pouch.");
        return -1;
      }
    }

    return waitTimer.next();
  }

  private boolean craftRunes(GameObject altar) {
    Item splinters =
        Inventory.get(item -> item != null && item.getName().equalsIgnoreCase(SUNFIRE_SPLINTERS));
    int essenceCount = Inventory.count(PURE_ESSENCE);

    if (splinters == null || essenceCount == 0) {
      Logger.error("Missing sunfire splinters or pure essence.");
      return false;
    }

    if (!splinters.useOn(altar)) {
      return false;
    }

    return Sleep.sleepUntil(() -> Inventory.count(PURE_ESSENCE) < essenceCount, CRAFT_TIMEOUT_MS);
  }

  private boolean emptyColossalPouch() {
    Item pouch =
        Inventory.get(item -> item != null && item.getName().equalsIgnoreCase(COLOSSAL_POUCH));
    int essenceCount = Inventory.count(PURE_ESSENCE);

    if (pouch == null || !pouch.interact("Empty")) {
      return false;
    }

    return Sleep.sleepUntil(
        () -> Inventory.count(PURE_ESSENCE) > essenceCount, EMPTY_POUCH_TIMEOUT_MS);
  }
}

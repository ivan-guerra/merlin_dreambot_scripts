package org.dreambot.merlin.sunfire_crafter.tasks;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.merlin.common.Utility;
import org.dreambot.merlin.common.WaitTimer;

public class WithDrawSuppliesTask extends TaskNode {
  private static final String PURE_ESSENCE = "Pure essence";
  private static final String COLOSSAL_POUCH = "Colossal pouch";
  private static final long POUCH_FILL_TIMEOUT_MS = 5000;
  private static final int COLOSSAL_POUCH_CAPACITY = 40;
  private static final int INVENTORY_ESSENCE_CAPACITY = 24;
  private final WaitTimer waitTimer = new WaitTimer(3000, 5000);

  @Override
  public boolean accept() {
    return Utility.countInInventory(PURE_ESSENCE) < INVENTORY_ESSENCE_CAPACITY;
  }

  @Override
  public int execute() {
    if (!Bank.open()) {
      Logger.info("Walking to nearest bank to withdraw supplies.");
      return waitTimer.next();
    }

    if (!fillColossalPouch()) {
      Logger.error("Failed to fill colossal pouch.");
      return -1;
    }

    if (!Utility.withdrawAll(PURE_ESSENCE)) {
      Logger.error("Failed to withdraw pure essence for inventory.");
      return -1;
    }

    Logger.info("Colossal pouch and inventory filled with pure essence.");

    if (!Utility.closeBank()) {
      Logger.error("Failed to close bank.");
      return -1;
    }

    return waitTimer.next();
  }

  private boolean fillColossalPouch() {
    int essenceRemainingForPouch = COLOSSAL_POUCH_CAPACITY;

    while (essenceRemainingForPouch > 0) {
      int withdrawAmount = Math.min(essenceRemainingForPouch, INVENTORY_ESSENCE_CAPACITY);

      if (!Utility.withdrawAll(PURE_ESSENCE)) {
        return false;
      }

      Item pouch = Inventory.get(item -> item != null && item.getName().equals(COLOSSAL_POUCH));
      if (pouch == null || !pouch.interact("Fill")) {
        return false;
      }

      if (!Sleep.sleepUntil(
          () -> Utility.countInInventory(PURE_ESSENCE) < withdrawAmount, POUCH_FILL_TIMEOUT_MS)) {
        return false;
      }

      essenceRemainingForPouch -= withdrawAmount;
    }

    return true;
  }
}

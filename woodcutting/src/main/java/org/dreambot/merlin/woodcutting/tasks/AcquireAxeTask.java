package org.dreambot.merlin.woodcutting.tasks;

import java.util.concurrent.atomic.AtomicReference;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.merlin.common.Utility;
import org.dreambot.merlin.common.WaitTimer;
import org.dreambot.merlin.common.WalkingUtils;
import org.dreambot.merlin.woodcutting.Axe;

/**
 * Task node responsible for acquiring the current axe, either from the bank or by purchasing it
 * from the Grand Exchange.
 */
public class AcquireAxeTask extends TaskNode {
  private final AtomicReference<Axe> currAxe;
  private boolean buyingFromGE = false;
  private final WaitTimer waitTimer = new WaitTimer(5000, 6000);

  /**
   * Constructs a new AcquireAxeTask with the given AtomicReference to the current axe.
   *
   * @param currAxe an AtomicReference to the current axe being used
   */
  public AcquireAxeTask(AtomicReference<Axe> currAxe) {
    this.currAxe = currAxe;
    this.buyingFromGE = false;
  }

  /**
   * Checks if the player needs to acquire a new axe. The task will be accepted if the player does
   * not have the current axe equipped and does not have it in their inventory.
   *
   * @return true if the task should be executed, false otherwise
   */
  @Override
  public boolean accept() {
    final boolean isEquipped = Utility.isEquipped(currAxe.get().getName());
    final boolean isInInventory = Utility.isInInventory(currAxe.get().getName());

    return !isEquipped && !isInInventory;
  }

  /**
   * Executes the task of acquiring the current axe. The method first attempts to remove any
   * mainhand weapon, then checks if the axe is in the bank. If it is, it withdraws it; if not, it
   * attempts to buy it from the Grand Exchange.
   *
   * @return a human-like randomised delay in milliseconds before the next task execution, or -1 if
   *     an error occurred during the process
   */
  @Override
  public int execute() {
    final String axeName = currAxe.get().getName();

    // Removing the mainhand weapon ensures that we bank it when acquiring the axe upgrade. This
    // avoids scenarios where we have an axe of a type we don't want in our mainhand or take up a
    // slot with a random weapon in our inventory when we later equip the upgraded axe.
    if (!removeMainhandWeapon()) {
      Logger.error("Failed to remove mainhand weapon.");
      return -1;
    }

    if (buyingFromGE) {
      return buyFromGrandExchange(axeName);
    }

    if (Bank.open()) {
      if (Bank.contains(axeName)) {
        if (!Utility.depositAllItemsInBank()) {
          Logger.error("Failed to deposit all items in bank.");
          return -1;
        }
        if (!Utility.withdrawItemFromBank(axeName, 1)) {
          Logger.error("Failed to withdraw " + axeName + " from bank.");
          return -1;
        }
        Logger.info("Successfully withdrew " + axeName + " from bank.");
        Bank.close();
        Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
      } else {
        Logger.info(axeName + " not found in bank. Attempting to buy from Grand Exchange.");
        buyingFromGE = true;
      }
    }

    return waitTimer.next();
  }

  /**
   * Removes the mainhand weapon from the player's equipment slot.
   *
   * @return true if the mainhand weapon was successfully removed or if the slot is already empty,
   *     false otherwise
   */
  private boolean removeMainhandWeapon() {
    if (Equipment.isSlotEmpty(EquipmentSlot.WEAPON)) {
      return true;
    }
    Equipment.unequip(EquipmentSlot.WEAPON);
    return Sleep.sleepUntil(() -> Equipment.isSlotEmpty(EquipmentSlot.WEAPON), 3000);
  }

  /**
   * Attempts to buy the specified axe from the Grand Exchange. If the axe is already in the Grand
   * Exchange, it will be collected. If there are open slots, it will attempt to place a buy offer.
   *
   * @param axeName the name of the axe to buy
   * @return a human-like randomised delay in milliseconds before the next task execution, or -1 if
   *     an error occurred during the process
   */
  private int buyFromGrandExchange(String axeName) {
    if (GrandExchange.open()) {
      if (GrandExchange.isReadyToCollect()) {
        if (GrandExchange.contains(axeName)) {
          Logger.info("Collecting " + axeName + " from Grand Exchange.");

          buyingFromGE = false;
          collectToBank();
          closeGrandExchange();
        } else {
          collectToBank();
          Sleep.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 5000);
        }
      } else if (GrandExchange.getOpenSlots() > 0) {
        final int axeBuyPrice = LivePrices.getHigh(axeName);
        final boolean bidPlaced = GrandExchange.buyItem(axeName, 1, axeBuyPrice);
        final boolean axeBought = Sleep.sleepUntil(() -> hasAxeBought(axeName), 10_000);

        Logger.info("Buying " + axeName + " from Grand Exchange for " + axeBuyPrice + " coins.");
        if (bidPlaced && axeBought) {
          Logger.info("Successfully bought " + axeName + " from Grand Exchange.");
        } else {
          Logger.error("Failed to buy " + axeName + " from Grand Exchange.");
          closeGrandExchange();
          return -1;
        }
      } else {
        Logger.error("No open Grand Exchange slots available.");
        return -1;
      }
    } else {
      WalkingUtils.walkToTile(BankLocation.GRAND_EXCHANGE.getTile());
    }
    return waitTimer.next();
  }

  /**
   * Checks if the specified axe has been bought from the Grand Exchange.
   *
   * @param axeName the name of the axe to check
   * @return true if the axe has been bought, false otherwise
   */
  private boolean hasAxeBought(String axeName) {
    for (GrandExchangeItem item : GrandExchange.getItems()) {
      if (item.getName().equals(axeName) && (item.getStatus() == Status.BUY_COLLECT)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Closes the Grand Exchange interface if it is open.
   *
   * @return true if the Grand Exchange was successfully closed or was already closed, false
   *     otherwise
   */
  private boolean closeGrandExchange() {
    if (GrandExchange.isOpen()) {
      GrandExchange.close();
      return Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 5000);
    }
    return true;
  }

  /**
   * Collects items from the Grand Exchange if they are ready to be collected.
   *
   * @return true if the items were successfully collected or if there were no items to collect,
   *     false otherwise
   */
  private boolean collectToBank() {
    if (GrandExchange.isReadyToCollect()) {
      GrandExchange.collectToBank();
      return Sleep.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 5000);
    }
    return true;
  }
}

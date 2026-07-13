package org.dreambot.merlin.woodcutting;

import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicReference;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.merlin.common.AntiBanTask;
import org.dreambot.merlin.common.BankMiscItemsTask;
import org.dreambot.merlin.common.PlayerAvoidanceTask;
import org.dreambot.merlin.woodcutting.tasks.AcquireAxeTask;
import org.dreambot.merlin.woodcutting.tasks.ChopTreeTask;
import org.dreambot.merlin.woodcutting.tasks.DragonAxeSpecTask;
import org.dreambot.merlin.woodcutting.tasks.DropLogsTask;
import org.dreambot.merlin.woodcutting.tasks.EquipAxeTask;
import org.dreambot.merlin.woodcutting.tasks.UpgradeAxeTask;
import org.dreambot.merlin.woodcutting.tasks.UpgradeTreeTask;
import org.dreambot.merlin.woodcutting.tasks.WalkToTreeAreaTask;

/**
 * The Main class is the entry point for the Merlin's Woodcutting script. It extends TaskScript and
 * manages the execution of various woodcutting tasks.
 */
@ScriptManifest(
    name = "Merlin's Woodcutting",
    author = "Merlin",
    description = "Merlin's power chopping script.",
    category = Category.WOODCUTTING,
    version = 0.3)
public class Main extends TaskScript {
  private final AntiBanTask antiBan;
  private final String nonMiscItemRegex = "axe|logs|scroll";
  private AtomicReference<Tree> currTree = new AtomicReference<>(Tree.Normal);
  private AtomicReference<Axe> currAxe = new AtomicReference<>(Axe.BRONZE);

  /** Constructor for the Main class. Initializes the anti-ban task. */
  public Main() {
    this.antiBan = new AntiBanTask(this);
  }

  /** Paint method rendering anti-ban information on the screen. */
  @Override
  public void onPaint(Graphics2D g) {
    antiBan.onPaint(g);
  }

  /**
   * Initializes the script by adding various woodcutting tasks to the task list. This method is
   * called when the script starts.
   */
  @Override
  public void onStart() {
    addNodes(
        this.antiBan,
        new UpgradeTreeTask(currTree),
        new UpgradeAxeTask(currAxe),
        new AcquireAxeTask(currAxe),
        new EquipAxeTask(currAxe),
        new BankMiscItemsTask(nonMiscItemRegex),
        new WalkToTreeAreaTask(currTree),
        new DropLogsTask(),
        new PlayerAvoidanceTask<Tree>(currTree),
        new DragonAxeSpecTask(currAxe),
        new ChopTreeTask(currTree));
  }
}

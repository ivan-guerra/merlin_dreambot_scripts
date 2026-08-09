package org.dreambot.merlin.sunfire_crafter;

import java.awt.Graphics2D;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.merlin.common.AntiBanTask;
import org.dreambot.merlin.sunfire_crafter.tasks.CraftRunesTask;
import org.dreambot.merlin.sunfire_crafter.tasks.WalkToAltarTask;
import org.dreambot.merlin.sunfire_crafter.tasks.WithDrawSuppliesTask;

@ScriptManifest(
    name = "Merlin's Sunfire Runecrafting",
    author = "Merlin",
    description = "Craft Sunfire Runes.",
    category = Category.RUNECRAFTING,
    version = 0.1)
public class Main extends TaskScript {
  private final AntiBanTask antiBan;

  /** Constructor for the Main class. Initializes the anti-ban task. */
  public Main() {
    this.antiBan = new AntiBanTask(this);
  }

  /** Paint method rendering anti-ban information on the screen. */
  @Override
  public void onPaint(Graphics2D g) {
    antiBan.onPaint(g);
  }

  @Override
  public void onStart() {
    addNodes(this.antiBan, new WithDrawSuppliesTask(), new WalkToAltarTask(), new CraftRunesTask());
  }
}

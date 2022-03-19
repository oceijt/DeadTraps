package com.deadtraps;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "DeadTraps"
)
public class DeadTrapPlugin extends Plugin {

	private static final int LAID_BOX_IDENTIFIER = 9380;
	private static final int CAUGHT_BOX_IDENTIFIER = 9383;
	private static final int CAUGHT_BLACK_BOX_IDENTIFIER = 721;
	private static final int FAILED_BOX_IDENTIFIER = 9385;

	private static final int RED_CHIN_IDENTIFIER = 2911;
	private static final int BLACK_CHIN_IDENTIFIER = 2912;


	private final Map<WorldPoint, HunterTrap> traps = new HashMap<>();
	private final Map<Actor, List<HunterTrap>> chinTargets = new HashMap<>();
	private final List<HunterTrap> deadBoxes = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TrapOverlay overlay;

	@Inject
	private DeadTrapConfig config;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		overlay.setPriority(OverlayPriority.HIGH);
		overlay.updateConfig();
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
		GameObject object = gameObjectSpawned.getGameObject();
		WorldPoint trapLocation = object.getWorldLocation();

		switch (object.getId()) {
			case LAID_BOX_IDENTIFIER:
			case CAUGHT_BOX_IDENTIFIER:
			case FAILED_BOX_IDENTIFIER:
			case CAUGHT_BLACK_BOX_IDENTIFIER:
				traps.put(trapLocation, new HunterTrap(object));
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned) {
		GameObject object = gameObjectDespawned.getGameObject();
		HunterTrap trap = traps.get(object.getWorldLocation());
		if (traps.containsKey(object.getWorldLocation())) {
			chinTargets.forEach((key, value) -> value.remove(trap));
			if (!traps.remove(object.getWorldLocation(), trap)) log.debug("Failed to remove: " + object.getId());
		}
		deadBoxes.remove(trap);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		NPC npc = npcDespawned.getNpc();
		if (npc.getId() == RED_CHIN_IDENTIFIER || npc.getId() == BLACK_CHIN_IDENTIFIER) {
			if (chinTargets.get(npcDespawned.getActor()).size() >= 2) {
				log.debug("removing chin which caused dead trap");
				deadBoxes.addAll(chinTargets.get(npcDespawned.getActor()));
			}
			chinTargets.remove(npcDespawned.getActor());
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged) {
		Actor target = interactingChanged.getTarget();
		Actor source = interactingChanged.getSource();

		if (target != null && source != null) {
			if (source.getName() == null) { // May be a trap, trap source name is null
				traps.forEach((worldPoint, trap) -> {
					if (source.getWorldLocation().distanceTo(worldPoint) == 0) { // Check if source is a trap in the list of traps
						if (chinTargets.get(target) == null) { // Check if chin target entry doesnt exist yet
							// add new entry with new list for the chin
							chinTargets.put(target, new ArrayList<>());
							chinTargets.get(target).add(trap);
						} else {
							if (!chinTargets.get(target).contains(trap)) chinTargets.get(target).add(trap);
						}
					}
				});
			}
		}
	}

	public List<HunterTrap> getDeadBoxes() {
		return deadBoxes;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("DeadTraps")) {
			overlay.updateConfig();
		}
	}

	@Provides
	DeadTrapConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(DeadTrapConfig.class);
	}
}
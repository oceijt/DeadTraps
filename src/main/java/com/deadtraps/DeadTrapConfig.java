package com.deadtraps;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("DeadTraps")
public interface DeadTrapConfig extends Config
{
	@ConfigItem(
			position = 2,
			keyName = "hexColorDeadTrap",
			name = "Dead trap color",
			description = "Color of dead trap"
	)
	default Color getDeadTrapColor()
	{
		return Color.BLACK;
	}
}

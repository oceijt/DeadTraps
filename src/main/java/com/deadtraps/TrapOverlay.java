package com.deadtraps;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

import javax.inject.Inject;
import java.awt.*;

public class TrapOverlay extends Overlay {
    private final Client client;
    private final DeadTrapPlugin plugin;
    private final DeadTrapConfig config;

    private Color colorDead, colorDeadBorder;

    @Inject
    TrapOverlay(Client client, DeadTrapPlugin plugin, DeadTrapConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.plugin = plugin;
        this.config = config;
        this.client = client;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        drawTraps(graphics);
        return null;
    }

    /**
     * Updates the timer colors.
     */
    public void updateConfig()
    {
        colorDeadBorder = config.getDeadTrapColor();
        colorDead = new Color(colorDeadBorder.getRed(), colorDeadBorder.getGreen(), colorDeadBorder.getBlue(), 100);
    }

    /**
     * Iterates over all the traps that were placed by the local player, and
     * draws a circle or a timer on the trap, depending on the trap state.
     *
     * @param graphics
     */
    private void drawTraps(Graphics2D graphics)
    {
        for (HunterTrap box : plugin.getDeadBoxes())
        {
            drawTimerOnTrap(graphics, box, colorDead, colorDeadBorder);
            drawCircleOnTrap(graphics, box, colorDead, colorDeadBorder);
        }
    }

    /**
     * Draws a timer on a given trap.
     *
     * @param graphics
     * @param trap The trap on which the timer needs to be drawn
     * @param fill The fill color of the timer
     * @param border The border color of the timer
     */
    private void drawTimerOnTrap(Graphics2D graphics, HunterTrap trap, Color fill, Color border)
    {
        if (trap.getWorldLocation().getPlane() != client.getPlane())
        {
            return;
        }
        LocalPoint localLoc = LocalPoint.fromWorld(client, trap.getWorldLocation());
        if (localLoc == null)
        {
            return;
        }
        net.runelite.api.Point loc = Perspective.localToCanvas(client, localLoc, client.getPlane());

        if (loc == null)
        {
            return;
        }

        ProgressPieComponent pie = new ProgressPieComponent();
        pie.setFill(fill);
        pie.setBorderColor(border);
        pie.setPosition(loc);
        pie.setProgress(1 - trap.getTrapTimeRelative());
        pie.render(graphics);
    }

    /**
     * Draws a timer on a given trap.
     *
     * @param graphics
     * @param trap The trap on which the timer needs to be drawn
     * @param fill The fill color of the timer
     * @param border The border color of the timer
     */
    private void drawCircleOnTrap(Graphics2D graphics, HunterTrap trap, Color fill, Color border)
    {
        if (trap.getWorldLocation().getPlane() != client.getPlane())
        {
            return;
        }
        LocalPoint localLoc = LocalPoint.fromWorld(client, trap.getWorldLocation());
        if (localLoc == null)
        {
            return;
        }
        net.runelite.api.Point loc = Perspective.localToCanvas(client, localLoc, client.getPlane());

        ProgressPieComponent pie = new ProgressPieComponent();
        pie.setFill(fill);
        pie.setBorderColor(border);
        pie.setPosition(loc);
        pie.setProgress(1);
        pie.render(graphics);
    }
}

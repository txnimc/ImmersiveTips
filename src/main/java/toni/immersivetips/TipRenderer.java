package toni.immersivetips;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import toni.immersivemessages.api.ImmersiveMessage;
import toni.immersivetips.foundation.config.AllConfigs;

import lombok.Getter;

import net.minecraft.client.player.LocalPlayer;
import toni.immersivemessages.renderers.CaxtonRenderer;
import toni.immersivemessages.renderers.ITooltipRenderer;
import toni.immersivemessages.renderers.VanillaRenderer;
import toni.lib.utils.PlatformUtils;

import java.util.LinkedList;
import java.util.Queue;

public class TipRenderer {
    private static final Queue<ImmersiveMessage> tooltipQueue = new LinkedList<>();
    @Getter
    private static final ITooltipRenderer renderer = initRenderer();

    private static final float NANOSECONDS_PER_TICK = 1000000000.0f / 20; // 50 million ns per tick for 20 ticks per second
    private static long lastTime = System.nanoTime();

    private static ImmersiveMessage currentTooltip;

    public static void drawOnScreen(GuiGraphics graphics, Screen screen, float delta) {
        var isEnabled = ImmersiveTips.EnabledScreens.contains(screen.getClass()) || AllConfigs.client().whitelistedScreens.get().contains(screen.getClass().getName());
        if (!isEnabled || AllConfigs.client().blacklistedScreens.get().contains(screen.getClass().getName()))
            return;


        if (ImmersiveTips.AllTips.isEmpty()) {
            return;
        }

        if (tooltipQueue.isEmpty())
        {
            var randomTip = ImmersiveTips.getNextTip();
            randomTip.getMessage().animation.resetPlayhead(0f);
            if (randomTip.getMessage().subtext != null)
                randomTip.getMessage().subtext.animation.resetPlayhead(0f);

            tooltipQueue.add(randomTip.getMessage());
        }

        render(graphics, delta);
    }


    static void render(GuiGraphics graphics, float delta) {
        long currentTime = System.nanoTime();
//        if (Minecraft.getInstance().isPaused())
//        {
//            lastTime = currentTime;
//            return;
//        }

        var partialTicks = (currentTime - lastTime) / NANOSECONDS_PER_TICK;
        lastTime = currentTime;

        if (currentTooltip == null) {
            if (tooltipQueue.isEmpty())
                return;

            currentTooltip = tooltipQueue.remove();
        }

        renderTooltip(graphics, partialTicks, currentTooltip, 0);
    }

    static void renderTooltip(GuiGraphics graphics, float deltaTicks, ImmersiveMessage tooltip, int depth) {
        tooltip.tick(deltaTicks);
        tooltip.animation.advancePlayhead(deltaTicks / 20);

        if (depth == 0 && tooltip.animation.getCurrent() >= tooltip.animation.duration)
        {
            currentTooltip = null;
            return;
        }

        renderer.render(tooltip, graphics, deltaTicks);

        if (tooltip.subtext != null)
            renderTooltip(graphics, deltaTicks, tooltip.subtext, depth + 1);
    }


    private static ITooltipRenderer initRenderer() {
        if (PlatformUtils.isModLoaded("caxton"))
            return new CaxtonRenderer();

        return new VanillaRenderer();
    }

    static void showToPlayer(LocalPlayer player, ImmersiveMessage tooltip) {
        tooltipQueue.add(tooltip);
    }

    public static boolean hasTooltip() {
        return currentTooltip != null;
    }

    public static int queueCount() {
        return tooltipQueue.size();
    }
}

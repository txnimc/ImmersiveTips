package toni.immersivetips.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toni.immersivetips.TipRenderer;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void renderWithTooltip(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
        TipRenderer.drawOnScreen(graphics, (Screen) ((Object) this), delta);
    }
}
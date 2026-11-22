package optimize;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class OptimizeClient implements ClientModInitializer {
    private static KeyMapping toggleKey;
    private static KeyMapping modeKey;
    private static boolean enabled = false;
    private static boolean fullCharge = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.optimize.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KeyMapping.Category.MISC));
        modeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.optimize.mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) enabled = !enabled;
            while (modeKey.consumeClick()) fullCharge = !fullCharge;

            Player player = mc.player;
            if (player == null || mc.level == null) return;

            Component text = enabled
                    ? Component.literal("OPTIMIZE ON").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                    : Component.literal("OPTIMIZE OFF").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            mc.gui.setOverlayMessage(text, false);

            if (!enabled || mc.screen != null || mc.gameMode == null) return;
            if (player.isSpectator() || !player.isAlive()) return;

            float threshold = fullCharge ? 1.0f : 0.848f;
            if (player.getAttackStrengthScale(0.0f) < threshold) return;

            if (mc.hitResult instanceof EntityHitResult hit
                    && hit.getEntity().isAlive() && hit.getEntity().isAttackable()) {
                mc.gameMode.attack(player, hit.getEntity());
            } else {
                player.resetAttackStrengthTicker();
            }
            player.swing(InteractionHand.MAIN_HAND);
        });
    }
}

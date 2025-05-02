package NoPlaceNearObsidian;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class NoPlaceNearObsidianMod implements ModInitializer {
    private static boolean isEnabled = true;
    private KeyBinding toggleBinding;

    @Override
    public void onInitialize() {
        // 注册快捷键
        toggleBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.noplaceobsidian.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V, // 默认使用V键
            "category.noplaceobsidian.general"
        ));

        // 注册快捷键检查事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleBinding.wasPressed()) {
                isEnabled = !isEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("黑曜石周围方块限制: " + (isEnabled ? "开启" : "关闭")), true);
                }
            }
        });

        // 注册方块使用事件
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!isEnabled) return ActionResult.PASS;

            // 检查是否正在尝试放置方块
            if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
                BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());

                // 检查四周是否有黑曜石
                for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                    BlockPos checkPos = pos.offset(direction);
                    BlockState checkState = world.getBlockState(checkPos);
                    if (checkState.getBlock() == Blocks.OBSIDIAN) {
                        if (player.getWorld().isClient) {
                            player.sendMessage(Text.literal("无法在黑曜石旁边放置方块！"), true);
                        }
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}

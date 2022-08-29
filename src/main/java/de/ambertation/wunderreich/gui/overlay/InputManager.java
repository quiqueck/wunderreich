package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.mixin.client.overlay.MouseHandlerAccessor;
import de.ambertation.wunderreich.network.ChangedSDFMessage;
import de.ambertation.wunderreich.registries.WunderreichItems;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import org.jetbrains.annotations.ApiStatus;

public class InputManager {
    public static final InputManager INSTANCE = new InputManager();

    public enum Mode {
        NONE, TRANSLATE, ROTATE, SCALE
    }

    public static final int LOCK_X = 1 << 0;
    public static final int LOCK_Y = 1 << 1;
    public static final int LOCK_Z = 1 << 2;
    public static final int LOCK_LOCAL = 1 << 3;
    public static final int LOCK_INVERT = 1 << 4;

    private boolean transformMode = false;
    private ItemStack ruler = null;
    private Mode mode = Mode.NONE;
    private int lockFlag = 0;
    private boolean shiftDown = false;
    private boolean altDown = false;
    private boolean ctrlDown = false;

    private double lastX, lastY;

    private Transform startTransform = Transform.IDENTITY;

    public ItemStack getActiveRuler() {
        return ruler;
    }

    @ApiStatus.Internal
    public boolean handleKey(InputConstants.Key key, int button) {
        final int keyValue = key.getValue();
        Wunderreich.LOGGER.info("handleKey: " + inTransformMode() + ", button=" + button + " - " + (button == InputConstants.PRESS) + ", key=" + key + ", mode=" + mode + ", lock=" + lockFlag + ", ruler=" + ruler + ", shift=" + shiftDown);
        if (inTransformMode()) {
            if (button == InputConstants.PRESS) {
                if (mode == Mode.NONE) {
                    switch (keyValue) {
                        case InputConstants.KEY_ESCAPE:
                            stopTransformMode();
                            return true;
                        case InputConstants.KEY_G:
                            if (altDown) {
                                updateTransform((sdf, t) -> t.moveTo(sdf.defaultTransform().center));
                                return true;
                            }
                            startTransformMode(Mode.TRANSLATE);
                            return true;
                        case InputConstants.KEY_R:
                            if (altDown) {
                                updateTransform((sdf, t) -> t.setRotation(sdf.defaultTransform().rotation));
                                return true;
                            }
                            startTransformMode(Mode.ROTATE);
                            return true;
                        case InputConstants.KEY_S:
                            if (altDown) {
                                updateTransform((sdf, t) -> t.setScale(sdf.defaultTransform().size));
                                return true;
                            }
                            startTransformMode(Mode.SCALE);
                            return true;
                    }
                } else {
                    switch (keyValue) {
                        case InputConstants.KEY_ESCAPE:
                            endTransformMode(false);
                            return true;
                        case InputConstants.KEY_RETURN:
                            endTransformMode(true);
                            return true;
                        case InputConstants.KEY_X:
                            if ((lockFlag & LOCK_X) == 0) {
                                lockFlag |= LOCK_X;
                                if (shiftDown) lockFlag |= LOCK_INVERT;
                            } else lockFlag ^= LOCK_LOCAL;
                            return true;
                        case InputConstants.KEY_S:
                        case InputConstants.KEY_W:
                        case InputConstants.KEY_A:
                        case InputConstants.KEY_D:
                            return false;
                    }
                }
            }
        }
        if (keyValue == InputConstants.KEY_LSHIFT || keyValue == InputConstants.KEY_RSHIFT) {
            if (button == InputConstants.RELEASE) shiftDown = false;
            else if (button == InputConstants.PRESS) shiftDown = true;
        } else if (keyValue == InputConstants.KEY_LALT || keyValue == InputConstants.KEY_RALT) {
            if (button == InputConstants.RELEASE) altDown = false;
            else if (button == InputConstants.PRESS) altDown = true;
        } else if (keyValue == InputConstants.KEY_LCONTROL || keyValue == InputConstants.KEY_RCONTROL) {
            if (button == InputConstants.RELEASE) ctrlDown = false;
            else if (button == InputConstants.PRESS) ctrlDown = true;
        }
        return false;
    }

    private void updateTransform(BiFunction<SDF, Transform, Transform> updater) {
        if (ruler != null) {
            ConstructionData cd = ConstructionData.getConstructionData(ruler);
            if (cd != null) {
                Transform newT = cd.updateActiveTransformOnClient(updater);
                ChangedSDFMessage.INSTANCE.sendTransform(null, newT);
            }
        }
    }

    public Transform getCurrentTransform() {
        if (ruler != null) {
            ConstructionData cd = ConstructionData.getConstructionData(ruler);
            if (cd != null) {
                SDF s = cd.getActiveSDF();
                if (s != null) {
                    return s.getLocalTransform();
                }
            }
        }
        return Transform.IDENTITY;
    }

    @ApiStatus.Internal
    public boolean onMove(MouseHandlerAccessor h, double mouseX, double mouseY) {
        boolean handled = false;
        if (inTransformMode()) {
            if (mode != Mode.NONE) {
                if (ruler != null) {
                    //makes sure the screen does not jump when ending transform mode
                    h.wunder_setXPos(mouseX);
                    h.wunder_setYPos(mouseY);
                    h.wunder_setAccumulatedDX(0);
                    h.wunder_setAccumulatedDY(0);

                    double dx = mouseX - lastX;
                    double dy = mouseY - lastY;
                    System.out.println("x:" + mouseX + ", y:" + mouseY + ", dx:" + dx + ", dy" + dy);
                    updateTransform((sdf, t) -> t.moveBy(Float3.of(dx * .01, 0, 0)));

                    handled = true;
                }
            }

            lastX = mouseX;
            lastY = mouseY;
        }
        return handled;
    }

    @ApiStatus.Internal
    public boolean onPress(int button, int state, int unk) {
        if (inTransformMode() && mode != Mode.NONE) {
            if (state == InputConstants.RELEASE) {
                endTransformMode(true);
            }
            return true;
        }
        System.out.println(button + ", " + state + ", " + unk);
        return false;
    }

    private void endTransformMode(boolean accept) {
        this.mode = Mode.NONE;
        this.lockFlag = 0;
        if (!accept) {
            updateTransform((sdf, t) -> startTransform);
        }
        Wunderreich.LOGGER.info("endTransformMode: " + inTransformMode() + ", mode=" + mode + ", lock=" + lockFlag + ", ruler=" + ruler + ", shift=" + shiftDown);
    }

    private void startTransformMode(Mode mode) {
        this.mode = mode;
        this.lockFlag = 0;
        startTransform = getCurrentTransform();
        Wunderreich.LOGGER.info("startTransformMode: " + inTransformMode() + ", mode=" + mode + ", lock=" + lockFlag + ", ruler=" + ruler + ", shift=" + shiftDown);
    }

    public void startTransformMode() {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.is(WunderreichItems.RULER)) {
            ConstructionData constructionData = ConstructionData.getConstructionData(stack);
            if (constructionData != null) {
                SDF sdf = constructionData.SDF_DATA.get();
                if (sdf != null && !sdf.isEmpty()) {
                    ruler = stack;
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.translatable("msg.wunderreich.start_transform_mode"),
                            false
                    );
                    transformMode = true;
                }
            }
        }
    }

    public void stopTransformMode() {
        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable("msg.wunderreich.stop_transform_mode"),
                false
        );
        transformMode = false;
    }


    public boolean inTransformMode() {
        return transformMode;
    }

    public Mode getMode() {
        return mode;
    }

    public int getLockFlag() {
        return lockFlag;
    }
}

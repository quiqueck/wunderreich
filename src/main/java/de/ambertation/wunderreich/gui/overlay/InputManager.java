package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Quaternion;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.mixin.client.overlay.MouseHandlerAccessor;
import de.ambertation.wunderreich.network.ChangedSDFMessage;
import de.ambertation.wunderreich.registries.WunderreichItems;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Camera;
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

    public static final int CONTROL_WIDGET_RADIUS = 25;

    public static final int LOCK_X = 1;
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

    private double lastX, lastY, startX, startY;
    private boolean negative = false;
    private String numberString = null;
    private Camera camera;

    private Transform startTransform = Transform.IDENTITY;

    public ItemStack getActiveRuler() {
        return ruler;
    }

    @ApiStatus.Internal
    public boolean handleKey(char keyChar, InputConstants.Key key, int button) {
        final int keyValue = key.getValue();
        Wunderreich.LOGGER.info("handleKey: " + inTransformMode() + ", button=" + button + " - " + (button == InputConstants.PRESS) + ", key=" + key + ", c=" + keyChar + "(" + ((int) keyChar) + ")" + ", mode=" + mode + ", lock=" + lockFlag + ", ruler=" + ruler + ", shift=" + shiftDown);

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
        if (inTransformMode()) {
            if (mode != Mode.NONE) {
                if (keyValue == InputConstants.KEY_LSHIFT || keyValue == InputConstants.KEY_RSHIFT) {
                    return true;
                }
            }
            if (button == InputConstants.PRESS) {
                if (mode == Mode.NONE) {
                    if (keyValue == InputConstants.KEY_ESCAPE) {
                        stopTransformMode();
                        return true;
                    }
                    switch (keyChar) {
                        case 't' -> {
                            if (altDown) {
                                updateTransform((sdf, t) -> t.moveTo(sdf.defaultTransform().center));
                                return true;
                            }
                            startTransformMode(Mode.TRANSLATE);
                            return true;
                        }
                        case 'r' -> {
                            if (altDown) {
                                updateTransform((sdf, t) -> t.setRotation(sdf.defaultTransform().rotation));
                                return true;
                            }
                            startTransformMode(Mode.ROTATE);
                            return true;
                        }
                        case 'z' -> {
                            if (altDown) {
                                updateTransform((sdf, t) -> t.setScale(sdf.defaultTransform().size));
                                return true;
                            }
                            startTransformMode(Mode.SCALE);
                            return true;
                        }
                    }
                } else {
                    if (keyValue == InputConstants.KEY_ESCAPE) {
                        endTransformMode(false);
                        return true;
                    }
                    if (keyValue == InputConstants.KEY_RETURN) {
                        endTransformMode(true);
                        return true;
                    }
                    if (keyValue == InputConstants.KEY_BACKSPACE) {
                        addToNumberString(keyValue, keyChar);
                        return true;
                    }
                    switch (keyChar) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case '0':
                        case '-':
                        case '.':
                        case ',':
                            addToNumberString(keyValue, keyChar);
                            return true;
                        case 'x':
                            if ((lockFlag & LOCK_X) == 0) {
                                lockFlag = LOCK_X;
                                if (shiftDown) lockFlag |= LOCK_INVERT;
                            } else lockFlag ^= LOCK_LOCAL;
                            return true;
                        case 'y':
                            if ((lockFlag & LOCK_Y) == 0) {
                                lockFlag = LOCK_Y;
                                if (shiftDown) lockFlag |= LOCK_INVERT;
                            } else lockFlag ^= LOCK_LOCAL;
                            return true;
                        case 'z':
                            if ((lockFlag & LOCK_Z) == 0) {
                                lockFlag = LOCK_Z;
                                if (shiftDown) lockFlag |= LOCK_INVERT;
                            } else lockFlag ^= LOCK_LOCAL;
                            return true;
                        case 's':
                        case 'w':
                        case 'a':
                        case 'd':
                            return false;
                    }
                }
            }
        }

        return false;
    }

    private static final double MOUSE_SCALE = 0.1;

    private void addToNumberString(int keyValue, char keyChar) {
        if (keyChar == '-') negative = !negative;
        else if (keyValue == InputConstants.KEY_BACKSPACE) {
            if (numberString != null && !numberString.isBlank()) {
                numberString = numberString.substring(0, numberString.length() - 1);
            }
        } else {
            if (numberString == null) numberString = "";
            if (keyChar == ',') keyChar = '.';
            numberString += keyChar;
        }

        if (mode == Mode.TRANSLATE) {
            updateTransform(this::getMouseTranslation);
        } else if (mode == Mode.SCALE) {
            updateTransform(this::getMouseScale);
        } else if (mode == Mode.ROTATE) {
            updateTransform(this::getMouseRotation);
        }
    }

    private Transform getMouseScale(SDF sdf, Transform t) {
        double scale = getDelta().x;
        if ((lockFlag & LOCK_LOCAL) == LOCK_LOCAL) {
            if ((lockFlag & LOCK_INVERT) == LOCK_INVERT) {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.scaleBy(
                            sdf.getWorldTransformMatrix().getBasisY().mul(scale)
                               .add(sdf.getWorldTransformMatrix().getBasisZ().mul(scale))
                               .add(sdf.getWorldTransformMatrix().getBasisX())
                    );
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.scaleBy(
                            sdf.getWorldTransformMatrix().getBasisX().mul(scale)
                               .add(sdf.getWorldTransformMatrix().getBasisZ().mul(scale))
                               .add(sdf.getWorldTransformMatrix().getBasisY())
                    );
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.scaleBy(
                            sdf.getWorldTransformMatrix().getBasisX().mul(scale)
                               .add(sdf.getWorldTransformMatrix().getBasisY().mul(scale))
                               .add(sdf.getWorldTransformMatrix().getBasisZ())
                    );
                }
            } else {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.scaleBy(sdf.getWorldTransformMatrix().getBasisX().mul(scale)
                                                     .add(sdf.getWorldTransformMatrix().getBasisY())
                                                     .add(sdf.getWorldTransformMatrix().getBasisZ()));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.scaleBy(sdf.getWorldTransformMatrix().getBasisX()
                                                     .add(sdf.getWorldTransformMatrix().getBasisY().mul(scale))
                                                     .add(sdf.getWorldTransformMatrix().getBasisZ()));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.scaleBy(sdf.getWorldTransformMatrix().getBasisX()
                                                     .add(sdf.getWorldTransformMatrix().getBasisY())
                                                     .add(sdf.getWorldTransformMatrix().getBasisZ().mul(scale)));
                }
            }
        } else {
            if ((lockFlag & LOCK_INVERT) == LOCK_INVERT) {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.scaleBy(Float3.of(1, scale, scale));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.scaleBy(Float3.of(scale, 1, scale));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.scaleBy(Float3.of(scale, scale, 1));
                }
            } else {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.scaleBy(Float3.of(scale, 1, 1));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.scaleBy(Float3.of(1, scale, 1));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.scaleBy(Float3.of(1, 1, scale));
                }
            }
        }
        if (camera != null) {
            return startTransform.scaleBy(Float3.of(scale));
        }
        return t;
    }

    private Transform getMouseRotation(SDF sdf, Transform t) {
        double angle = getDelta().x;
        if ((lockFlag & LOCK_LOCAL) == LOCK_LOCAL) {
            if ((lockFlag & LOCK_X) == LOCK_X) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(
                        sdf.getWorldTransformMatrix().getBasisX(),
                        angle
                ));
            } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(
                        sdf.getWorldTransformMatrix().getBasisY(),
                        angle
                ));
            } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(
                        sdf.getWorldTransformMatrix().getBasisZ(),
                        angle
                ));
            }
        } else {
            if ((lockFlag & LOCK_X) == LOCK_X) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(Float3.X_AXIS, angle));
            } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(Float3.Y_AXIS, angle));
            } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                return startTransform.rotateBy(Quaternion.ofAxisAngle(Float3.Z_AXIS, angle));
            }
        }
        if (camera != null) {
            return startTransform.rotateBy(Quaternion.ofAxisAngle(Float3.of(camera.getLookVector()), angle));
        }
        return startTransform;
    }

    private Transform getMouseTranslation(SDF sdf, Transform t) {
        Float2 delta = getDelta();
        if ((lockFlag & LOCK_LOCAL) == LOCK_LOCAL) {
            if ((lockFlag & LOCK_INVERT) == LOCK_INVERT) {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.moveBy(
                            sdf.getWorldTransformMatrix().getBasisY().mul(delta.y)
                               .add(sdf.getWorldTransformMatrix().getBasisZ().mul(delta.x))
                    );
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.moveBy(
                            sdf.getWorldTransformMatrix().getBasisX().mul(delta.x)
                               .add(sdf.getWorldTransformMatrix().getBasisZ().mul(delta.y))
                    );
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.moveBy(
                            sdf.getWorldTransformMatrix().getBasisX().mul(delta.x)
                               .add(sdf.getWorldTransformMatrix().getBasisY().mul(delta.y))
                    );
                }
            } else {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.moveBy(sdf.getWorldTransformMatrix().getBasisX().mul(delta.x));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.moveBy(sdf.getWorldTransformMatrix().getBasisY().mul(delta.y));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.moveBy(sdf.getWorldTransformMatrix().getBasisZ().mul(delta.x));
                }
            }
        } else {
            if ((lockFlag & LOCK_INVERT) == LOCK_INVERT) {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.moveBy(Float3.of(0, delta.y, delta.x));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.moveBy(Float3.of(delta.x, 0, delta.y));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.moveBy(Float3.of(delta.x, delta.y, 0));
                }
            } else {
                if ((lockFlag & LOCK_X) == LOCK_X) {
                    return startTransform.moveBy(Float3.of(delta.x, 0, 0));
                } else if ((lockFlag & LOCK_Y) == LOCK_Y) {
                    return startTransform.moveBy(Float3.of(0, delta.y, 0));
                } else if ((lockFlag & LOCK_Z) == LOCK_Z) {
                    return startTransform.moveBy(Float3.of(0, 0, delta.x));
                }
            }
        }

        if (camera != null) {
            Float3 up = Float3.of(camera.getUpVector()).mul(-1 * delta.y);
            Float3 left = Float3.of(camera.getLeftVector()).mul(-1 * delta.x);
            return startTransform.moveBy(up.add(left));
        }
        return startTransform;
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

                    lastX = mouseX;
                    lastY = mouseY;

                    if (mode == Mode.TRANSLATE) {
                        Float3 delta = Float3.of(MOUSE_SCALE * (mouseX - lastX), MOUSE_SCALE * (mouseY - lastY), 0);
                        System.out.println("x:" + mouseX + ", y:" + mouseY + ", dx:" + delta.x + ", dy" + delta.y);

                        updateTransform(this::getMouseTranslation);
                    } else if (mode == Mode.ROTATE) {
                        Float3 delta = Float3.of(MOUSE_SCALE * (mouseX - lastX), MOUSE_SCALE * (mouseY - lastY), 0);
                        System.out.println("x:" + mouseX + ", y:" + mouseY + ", dx:" + delta.x + ", dy" + delta.y);

                        updateTransform(this::getMouseRotation);
                    } else if (mode == Mode.SCALE) {
                        Float3 delta = Float3.of(MOUSE_SCALE * (mouseX - startX), MOUSE_SCALE * (mouseY - startY), 0)
                                             .add(1);
                        System.out.println("x:" + mouseX + ", y:" + mouseY + ", dx:" + delta.x + ", dy" + delta.y);

                        updateTransform(this::getMouseScale);
                    }

                    handled = true;
                }
            }
        }

        lastX = mouseX;
        lastY = mouseY;
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
        numberString = null;
        negative = false;
        startTransform = getCurrentTransform();
        startX = lastX;
        startY = lastY;
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

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public String getNumberString() {
        if (negative) return "-" + numberString;
        return numberString;
    }

    public boolean hasNumberString() {
        if (numberString == null || numberString.isBlank()) return false;
        return true;
    }

    public boolean isValidNumberString() {
        if (!hasNumberString()) return false;
        try {
            Double.parseDouble(getNumberString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public double getNumberStringValue() {
        if (!isValidNumberString()) return 0;
        return Double.parseDouble(getNumberString());
    }

    public Float2 getMouseDelta() {
        if (mode == Mode.NONE) return Float2.ZERO;
        if (!inTransformMode()) return Float2.ZERO;

        if (mode == Mode.SCALE) {
            return Float2.of(lastX - (startX - CONTROL_WIDGET_RADIUS), lastY - startY);
        }
        if (mode == Mode.ROTATE) {
            return Float2.of(lastX - startX, lastY - (startY - CONTROL_WIDGET_RADIUS));
        } else if (mode == Mode.TRANSLATE) {
            if ((lockFlag & LOCK_INVERT) != LOCK_INVERT && lockFlag != 0) {
                if ((lockFlag & LOCK_Y) == LOCK_Y)
                    return Float2.of(0, lastY - startY);
                return Float2.of(lastX - startX, 0);
            }
        }
        return Float2.of(lastX - startX, lastY - startY);
    }

    public Float2 getDelta() {
        if (isValidNumberString()) {
            if (mode == Mode.SCALE) {
                return Float2.of(getNumberStringValue(), 0);
            } else if (mode == Mode.ROTATE) {
                return Float2.of(Math.toRadians(getNumberStringValue()), 0);
            } else if (mode == Mode.TRANSLATE) {
                if (lockFlag == 0)
                    return Float2.of(getNumberStringValue(), 0);
                return Float2.of(getNumberStringValue());
            }
        }

        Float2 delta = getMouseDelta();
        if (mode == Mode.SCALE) {
            return Float2.of(Math.pow(
                    delta.length() / CONTROL_WIDGET_RADIUS,
                    shiftDown ? MOUSE_SCALE : 1
            ), 0);
        } else if (mode == Mode.ROTATE) {
            return Float2.of(Math.signum(delta.x) * Float2.mY_AXIS.angleTo(delta), 0);
        }
        return delta.mul(MOUSE_SCALE);
    }

    public String getDeltaString() {
        Float2 delta = getDelta();
        if (mode == Mode.ROTATE) {
            return Float3.toString(Math.toDegrees(delta.x)) + "°";
        }
        return "dx=" + Float3.toString(delta.x) + ", dy=" + Float3.toString(delta.y);
    }
}

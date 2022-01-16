package de.ambertation.wunderreich.config;


import de.ambertation.wunderreich.Wunderreich;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigFile {
    private static final Gson JSON_BUILDER = new GsonBuilder().setPrettyPrinting()
                                                              .create();

    public record ConfigToken<T>(String path, String key, T defaultValue) {
        @Override
        public String toString() {
            return "ConfigToken{" +
                    "path='" + path + '\'' +
                    ", key='" + key + '\'' +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }

    public abstract class Value<T> {
        @NotNull
        public final ConfigToken<T> token;

        private boolean hiddenInUI = false;

        @Nullable
        protected Supplier<Boolean> isValidSupplier;

        public Value(String path, String key, T defaultValue) {
            this(new ConfigToken(path, key, defaultValue));
        }

        public Value(ConfigToken token) {
            this.token = token;
            get(); //has the side effect of initializing the default value
            registerValue(this);
        }

        public void hideInUI() {
            hiddenInUI = true;
        }

        public boolean isHiddenInUI() {
            return hiddenInUI;
        }

        @Nullable
        public Supplier<Boolean> getIsValidSupplier() {
            return isValidSupplier;
        }

        public final T getRaw() {
            JsonElement el = getValue(token);
            if (el == null) {
                set(token.defaultValue);
                return token.defaultValue;
            }
            return convert(el);
        }

        public T get() {
            return getRaw();
        }

        protected abstract T convert(@NotNull JsonElement el);

        @NotNull
        protected abstract JsonElement convert(T value);

        public void set(T value) {
            setValue(token, convert(value));
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "token=" + token +
                    ", value='" + get() + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Value)) return false;
            Value<?> value = (Value<?>) o;
            return token.equals(value.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }

    public class IntValue extends Value<Integer> {
        public IntValue(String path, String key, int defaultValue) {
            super(path, key, defaultValue);
        }

        protected IntValue(ConfigToken t) {
            super(t);
        }

        @Override
        protected Integer convert(@NotNull JsonElement el) {
            return el.getAsInt();
        }

        @Override
        protected @NotNull JsonElement convert(Integer value) {
            return new JsonPrimitive(value);
        }
    }

    public class FloatValue extends Value<Float> {
        public FloatValue(String path, String key, float defaultValue) {
            super(path, key, defaultValue);
        }

        protected FloatValue(ConfigToken t) {
            super(t);
        }

        @Override
        protected Float convert(@NotNull JsonElement el) {
            return el.getAsFloat();
        }

        @Override
        protected @NotNull JsonElement convert(Float value) {
            return new JsonPrimitive(value);
        }
    }

    public class BooleanValue extends Value<Boolean> {
        public BooleanValue(String path, String key, boolean defaultValue) {
            super(path, key, defaultValue);
        }

        protected BooleanValue(ConfigToken t) {
            super(t);
        }

        @Override
        protected Boolean convert(@NotNull JsonElement el) {
            return el.getAsBoolean();
        }

        @Override
        protected @NotNull JsonElement convert(Boolean value) {
            return new JsonPrimitive(value);
        }

        public BooleanValue and(BooleanValue... condition) {
            return and(() -> Arrays.stream(condition).map(c -> c.get()).reduce(true, (p, c) -> p && c));
        }

        public BooleanValue and(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token) {
                @Override
                public Boolean get() {
                    return condition.get() && self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }
    }

    public ConfigFile(String category) {
        this(Wunderreich.MOD_ID, category);
    }

    public ConfigFile(String basePath, String category) {
        final Path dir = FabricLoader.getInstance().getConfigDir().resolve(basePath);
        path = dir.resolve(category + ".json").toFile();
        this.category = basePath + "." + category;

        if (!dir.toFile().exists()) dir.toFile().mkdirs();
        loadFromDisc();
    }

    private final File path;
    public final String category;
    private JsonObject root;
    private boolean modified;

    private final List<Value<?>> knownValues = new LinkedList<>();

    public List<Value<?>> getAllValues() {
        return knownValues;
    }

    private void setModified() {
        modified = true;
    }

    private void registerValue(Value<?> v) {
        knownValues.remove(v);
        knownValues.add(v);
    }

    private JsonElement getValue(ConfigToken t) {
        JsonObject obj = getPathElement(t.path);
        if (!obj.has(t.key)) return null;

        return obj.get(t.key);
    }

    private void setValue(ConfigToken t, JsonElement value) {
        if (!value.equals(getValue(t))) {
            setModified();
        }

        JsonObject obj = getPathElement(t.path);
        obj.add(t.key, value);
    }

    private JsonObject getPathElement(String path) {
        if (path == null || path.trim().equals("")) {
            return root;
        }

        String[] names = path.split("\\.");
        JsonObject obj = root;

        for (int i = 0; i < names.length; i++) {
            final String p = names[i];
            if (obj.has(p)) {
                obj = obj.get(p).getAsJsonObject();
            } else {
                JsonObject newObject = new JsonObject();
                obj.add(p, newObject);
                obj = newObject;
            }
        }
        return obj;
    }

    public void loadFromDisc() {
        modified = false;
        if (path.exists()) {
            try (Reader reader = new FileReader(path)) {
                this.root = JSON_BUILDER.fromJson(reader, JsonElement.class).getAsJsonObject();
            } catch (Exception ex) {
                Wunderreich.LOGGER.error("Unable to open Config File at '{}'.", path.toString(), ex);
            }
        } else {
            this.root = new JsonObject();
            this.root.add("create_version", new JsonPrimitive(Wunderreich.VERSION));
        }
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (!modified && !force) return;

        try (FileWriter jsonWriter = new FileWriter(path)) {
            this.root.add("modify_version", new JsonPrimitive(Wunderreich.VERSION));
            String string = JSON_BUILDER.toJson(root);
            jsonWriter.write(string);
            jsonWriter.flush();
            modified = false;
        } catch (IOException ex) {
            Wunderreich.LOGGER.error("Unable to store Config File at '{}'.", path.toString(), ex);
        }
    }
}

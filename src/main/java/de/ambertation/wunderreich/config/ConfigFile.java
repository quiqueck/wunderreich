package de.ambertation.wunderreich.config;


import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.Version;

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
    public static final String MODIFY_VERSION = "modify_version";
    public static final String CREATE_VERSION = "create_version";

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
        private boolean deprecated = false;

        @Nullable
        protected Supplier<Boolean> isValidSupplier;

        public Value(String path, String key, T defaultValue) {
            this(new ConfigToken(path, key, defaultValue), false);
        }

        public Value(String path, String key, T defaultValue, boolean isDeprecated) {
            this(new ConfigToken(path, key, defaultValue), isDeprecated);
        }

        public Value(ConfigToken token) {
            this(token, false);
        }

        public Value(ConfigToken token, boolean isDeprecated) {
            this.deprecated = isDeprecated; //make sure this is set before get, otherwise deprecated values will get added to the config!

            this.token = token;
            get(); //has the side effect of initializing the default value
            registerValue(this);
        }

        public Value<T> hideInUI() {
            hiddenInUI = true;
            return this;
        }

        public boolean isHiddenInUI() {
            return hiddenInUI;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        @Nullable
        public Supplier<Boolean> getIsValidSupplier() {
            return isValidSupplier;
        }

        public final T getRaw() {
            JsonElement el = getValue(token, !deprecated);
            if (el == null) {
                if (!deprecated) set(token.defaultValue);
                return token.defaultValue;
            }
            return convert(el);
        }

        public T get() {
            return getRaw();
        }

        public void remove() {
            removeValue(token);
        }

        public void migrate(Value<T> newConfig) {
            newConfig.set(get());
            remove();
        }

        protected abstract T convert(@NotNull JsonElement el);

        @NotNull
        protected abstract JsonElement convert(T value);

        public void set(T value) {
            if (deprecated) throw new IllegalStateException("'" + token.path() + "." +
                    token.key + "' is deprecated and can no-longer be used");
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

        public IntValue(String path, String key, int defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected IntValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Integer convert(@NotNull JsonElement el) {
            return el.getAsInt();
        }

        @Override
        protected @NotNull JsonElement convert(Integer value) {
            return new JsonPrimitive(value);
        }

        public IntValue hideInUI() {
            return (IntValue) super.hideInUI();
        }
    }

    public class FloatValue extends Value<Float> {
        public FloatValue(String path, String key, float defaultValue) {
            super(path, key, defaultValue);
        }

        protected FloatValue(ConfigToken t) {
            super(t);
        }

        public FloatValue(String path, String key, float defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected FloatValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Float convert(@NotNull JsonElement el) {
            return el.getAsFloat();
        }

        @Override
        protected @NotNull JsonElement convert(Float value) {
            return new JsonPrimitive(value);
        }

        public FloatValue hideInUI() {
            return (FloatValue) super.hideInUI();
        }
    }

    public class BooleanValue extends Value<Boolean> {
        public BooleanValue(String path, String key, boolean defaultValue) {
            super(path, key, defaultValue);
        }

        protected BooleanValue(ConfigToken t) {
            super(t);
        }

        public BooleanValue(String path, String key, boolean defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected BooleanValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
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
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
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

        public BooleanValue or(BooleanValue... condition) {
            return or(() -> Arrays.stream(condition).map(c -> c.get()).reduce(true, (p, c) -> p || c));
        }

        public BooleanValue or(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
                @Override
                public Boolean get() {
                    return condition.get() || self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }

        public BooleanValue hideInUI() {
            return (BooleanValue) super.hideInUI();
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

    private JsonElement getValue(ConfigToken t, boolean addIfMissing) {
        JsonObject obj = getPathElement(t.path, addIfMissing);
        if (!obj.has(t.key)) return null;

        return obj.get(t.key);
    }

    private void setValue(ConfigToken t, JsonElement value) {
        if (!value.equals(getValue(t, true))) {
            setModified();
        }

        JsonObject obj = getPathElement(t.path, true);
        obj.add(t.key, value);
    }

    private void removeValue(ConfigToken t) {
        JsonObject o = getPathElement(t.path, false);
        if (o.has(t.key)) {
            Wunderreich.LOGGER.info("Removing Config " + t.path + "." + t.key);
            o.remove(t.key);
            setModified();
        }
    }


    private JsonObject getPathElement(String path, boolean addIfMissing) {
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
                if (addIfMissing) {
                    obj.add(p, newObject);
                }
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
            this.root.add(CREATE_VERSION, new JsonPrimitive(Wunderreich.VERSION.toString()));
        }
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (!modified && !force) return;

        try (FileWriter jsonWriter = new FileWriter(path)) {
            this.root.add(MODIFY_VERSION, new JsonPrimitive(Wunderreich.VERSION.toString()));
            String string = JSON_BUILDER.toJson(root);
            jsonWriter.write(string);
            jsonWriter.flush();
            modified = false;
        } catch (IOException ex) {
            Wunderreich.LOGGER.error("Unable to store Config File at '{}'.", path.toString(), ex);
        }
    }

    private String getVersionString(String name) {
        JsonObject mod = getPathElement("", true);
        if (mod == null) return "1.0.0";
        JsonPrimitive p = mod.getAsJsonPrimitive(name);
        if (p == null) return Wunderreich.VERSION.toString();
        if (!p.isString()) return "1.0.0";

        return p.getAsString();
    }

    public Version lastModifiedVersion() {
        return new Version(getVersionString(MODIFY_VERSION));
    }

    public Version createdVersion() {
        return new Version(getVersionString(CREATE_VERSION));
    }
}

package com.mndk.bteterrarenderer.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.BTRUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultYamlConfigBuilder extends AbstractConfigBuilder {

    /**
     * This needs to be a getter, since the mod config directory might not have been initialized
     * before using this property.
     */
    private final Supplier<File> fileGetter;
    private final Map<String, Object> map;
    private final Stack<Map<String, Object>> mapStack = new Stack<>();

    public DefaultYamlConfigBuilder(Supplier<File> fileGetter) {
        this.fileGetter = fileGetter;
        this.map = new HashMap<>();
        this.mapStack.add(map);
    }

    @Override
    protected void onPush(String pathName, @Nullable String comment) {
        Map<String, Object> newMap = new HashMap<>();
        map.put(pathName, newMap);
        mapStack.push(newMap);
    }

    @Override
    protected void onPop() {
        mapStack.pop();
    }

    @Override
    protected ConfigPropertyConnection makePropertyConnection(Field field, String name, @Nullable String comment,
                                                              Supplier<?> getter, Consumer<Object> setter, Object defaultValue) {
        final Map<String, Object> top = mapStack.peek();
        top.put(name, defaultValue);
        return new ConfigPropertyConnection(
                () -> top.put(name, getter.get()), // save
                () -> setter.accept(top.get(name)) // load
        );
    }

    @Override
    protected void postInitialization() {}

    @Override
    protected void saveAll() {
        try {
            BTETerraRendererConstants.YAML_MAPPER.writeValue(this.fileGetter.get(), this.map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // This will never happen
        } catch (IOException e) {
            BTETerraRendererConstants.LOGGER.error("Caught IO error while saving config.yml", e);
        }
    }

    @Override
    protected void loadAll() {
        Map<String, Object> readResult;
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
            readResult = BTETerraRendererConstants.YAML_MAPPER.readValue(this.fileGetter.get(), typeRef);
        } catch (JsonProcessingException e) {
            BTETerraRendererConstants.LOGGER.error("Caught json error while reading config.yml", e);
            return;
        } catch (IOException e) {
            BTETerraRendererConstants.LOGGER.error("Caught IO error while loading config.yml (perhaps the file was missing?)", e);
            return;
        }

        this.loadMap(readResult, this.map);
    }

    private void loadMap(Map<String, Object> source, Map<String, Object> destination) {
        for(Map.Entry<String, Object> entry : destination.entrySet()) {
            String key = entry.getKey();
            if(!source.containsKey(key)) continue;

            Object oldValue = entry.getValue(), newValue = source.get(key);
            if(oldValue instanceof Map) {
                if(!(newValue instanceof Map)) continue;

                this.loadMap(BTRUtil.uncheckedCast(newValue), BTRUtil.uncheckedCast(oldValue));
            }
            else if(oldValue instanceof Enum) {
                if(!(newValue instanceof String)) continue;

                destination.put(key, Enum.valueOf(BTRUtil.uncheckedCast(oldValue.getClass()), (String) newValue));
            }
            else {
                destination.put(key, newValue);
            }
        }
    }

}
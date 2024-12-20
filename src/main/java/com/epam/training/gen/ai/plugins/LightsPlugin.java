package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.dto.LightDto;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LightsPlugin {

    // Mock data for the lights
    private final Map<Integer, LightDto> lights = new HashMap<>();

    public LightsPlugin() {
        lights.put(1, LightDto.builder().id(1).name("Table Lamp").isOn(false).build());
        lights.put(2, LightDto.builder().id(2).name("Porch light").isOn(false).build());
        lights.put(3, LightDto.builder().id(3).name("Chandelier").isOn(false).build());
    }

    @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
    public List<LightDto> getLights() {
        log.info("Getting lights");
        return new ArrayList<>(lights.values());
    }

    @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
    public LightDto changeState(
            @KernelFunctionParameter(name = "id", description = "The ID of the light to change") int id,
            @KernelFunctionParameter(name = "isOn", description = "The new state of the light") boolean isOn) {

        log.info("Changing light id {}, isOn: {} ",id, isOn);

        if (!lights.containsKey(id)) {
            throw new IllegalArgumentException("Light not found");
        }

        lights.get(id).setOn(isOn);

        return lights.get(id);
    }
}

package de.maxhenkel.coordfinder.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.coordfinder.CoordFinder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public final class KeyMappings {

    private static final KeyMapping.Category MENU_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(CoordFinder.MODID, "general"));

    public static KeyMapping OPEN_MENU;

    private KeyMappings() {
    }

    public static void register() {
        OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.coordfinder.menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            MENU_CATEGORY
        ));
    }
}

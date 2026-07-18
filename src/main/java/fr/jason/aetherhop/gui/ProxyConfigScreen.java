package fr.jason.aetherhop.gui;

import fr.jason.aetherhop.AetherHop;
import fr.jason.aetherhop.config.ProxyConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Builds the Cloth Config screen for AetherHop. Registered via Mod Menu's config-button hook. */
public final class ProxyConfigScreen {
    private ProxyConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ProxyConfig config = AetherHop.config();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("aetherhop.config.title"))
            .setSavingRunnable(AetherHop::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("aetherhop.config.category.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("aetherhop.config.enabled"), config.enabled)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.enabled = value)
            .build());

        general.addEntry(entryBuilder.startStrField(Component.translatable("aetherhop.config.address"), config.address)
            .setDefaultValue("")
            .setTooltip(Component.translatable("aetherhop.config.address.tooltip"))
            .setSaveConsumer(value -> config.address = value)
            .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("aetherhop.config.type"), ProxyConfig.Type.class, config.type)
            .setDefaultValue(ProxyConfig.Type.HTTP)
            .setSaveConsumer(value -> config.type = value)
            .build());

        general.addEntry(entryBuilder.startStrField(Component.translatable("aetherhop.config.username"), config.username)
            .setDefaultValue("")
            .setSaveConsumer(value -> config.username = value)
            .build());

        general.addEntry(entryBuilder.startStrField(Component.translatable("aetherhop.config.password"), config.password)
            .setDefaultValue("")
            .setSaveConsumer(value -> config.password = value)
            .build());

        return builder.build();
    }
}

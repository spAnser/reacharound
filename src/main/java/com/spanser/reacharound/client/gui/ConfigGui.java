package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.config.ReacharoundConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigGui {
    ReacharoundConfig config = Reacharound.getInstance().config;

    public Screen getConfigScreen(Screen parent, boolean isTransparent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("reacharound.options.config"));
        builder.setSavingRunnable(() -> Reacharound.getInstance().saveConfig());
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("reacharound.options.indicator.enabled"), config.enabled).setDefaultValue(true).setSaveConsumer(newValue -> config.enabled = newValue).build());
        general.addEntry(entryBuilder.startSelector(Text.translatable("reacharound.options.indicator.style"), new Byte[]{0, 1, 2}, config.indicatorStyle).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> Text.literal("Quark");
            case 2 -> Text.literal("Custom");
            default -> Text.translatable("Default");
        }).setSaveConsumer((newValue) -> config.indicatorStyle = newValue).build());
        general.addEntry(entryBuilder.startStrField(Text.translatable("reacharound.options.indicator.vertical"), config.indicatorVertical).setDefaultValue("|   |").setSaveConsumer((newValue) -> config.indicatorVertical = newValue).build());
        general.addEntry(entryBuilder.startStrField(Text.translatable("reacharound.options.indicator.horizontal"), config.indicatorHorizontal).setDefaultValue("{   }").setSaveConsumer((newValue) -> config.indicatorHorizontal = newValue).build());

        return builder.setTransparentBackground(isTransparent).build();
    }
}

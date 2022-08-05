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
        general.addEntry(entryBuilder.startSelector(Text.translatable("reacharound.options.indicator.mode"), new Byte[]{0, 1, 2}, config.mode).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> Text.literal("Horizontal");
            case 2 -> Text.literal("Vertical");
            default -> Text.translatable("Both");
        }).setSaveConsumer((newValue) -> config.mode = newValue).build());
        general.addEntry(entryBuilder.startSelector(Text.translatable("reacharound.options.indicator.style"), new Byte[]{0, 1, 2}, config.indicatorStyle).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> Text.literal("Quark");
            case 2 -> Text.literal("Custom");
            default -> Text.translatable("Default");
        }).setSaveConsumer((newValue) -> config.indicatorStyle = newValue).build());
        general.addEntry(entryBuilder.startStrField(Text.translatable("reacharound.options.indicator.vertical"), config.indicatorVertical).setDefaultValue("|   |").setSaveConsumer((newValue) -> config.indicatorVertical = newValue).build());
        general.addEntry(entryBuilder.startStrField(Text.translatable("reacharound.options.indicator.horizontal"), config.indicatorHorizontal).setDefaultValue("{   }").setSaveConsumer((newValue) -> config.indicatorHorizontal = newValue).build());
        general.addEntry(entryBuilder.startAlphaColorField(Text.translatable("reacharound.options.indicator.color"), config.indicatorColor).setDefaultValue(0xffffffff).setSaveConsumer((newValue) -> config.indicatorColor = newValue).build());
        general.addEntry(entryBuilder.startAlphaColorField(Text.translatable("reacharound.options.indicator.colorObstructed"), config.indicatorColorObstructed).setDefaultValue(0xffff5555).setSaveConsumer((newValue) -> config.indicatorColorObstructed = newValue).build());

        general.addEntry(entryBuilder.startIntField(Text.translatable("reacharound.options.animation.duration"), config.indicatorAnimationDuration).setDefaultValue(5).setSaveConsumer((newValue) -> config.indicatorAnimationDuration = newValue).build());
        general.addEntry(entryBuilder.startSelector(Text.translatable("reacharound.options.animation.interpolation"), new Byte[]{0, 1, 2, 3}, config.indicatorAnimationInterpolation).setDefaultValue((byte) 2).setNameProvider((value) -> switch (value) {
            case 1 -> Text.literal("Linear");
            case 2 -> Text.literal("Quadratic");
            case 3 -> Text.literal("Cubic");
            default -> Text.translatable("None");
        }).setSaveConsumer((newValue) -> config.indicatorAnimationInterpolation = newValue).build());
        general.addEntry(entryBuilder.startSelector(Text.translatable("reacharound.options.animation.fadeInterpolation"), new Byte[]{0, 1, 2, 3}, config.indicatorAnimationFadeInterpolation).setDefaultValue((byte) 2).setNameProvider((value) -> switch (value) {
            case 1 -> Text.literal("Linear");
            case 2 -> Text.literal("Quadratic");
            case 3 -> Text.literal("Cubic");
            default -> Text.translatable("None");
        }).setSaveConsumer((newValue) -> config.indicatorAnimationFadeInterpolation = newValue).build());

        return builder.setTransparentBackground(isTransparent).build();
    }
}

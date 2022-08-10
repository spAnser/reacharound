package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.config.ReacharoundConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class ConfigGui {
    ReacharoundConfig config = Reacharound.getInstance().config;

    public Screen getConfigScreen(Screen parent, boolean isTransparent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("reacharound.config"));
        builder.setSavingRunnable(() -> Reacharound.getInstance().saveConfig());
        ConfigCategory indicator = builder.getOrCreateCategory(new TranslatableText("reacharound.config.indicator"));
        ConfigCategory animation = builder.getOrCreateCategory(new TranslatableText("reacharound.config.animation"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        indicator.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("reacharound.config.indicator.enabled"), config.enabled).setDefaultValue(true).setSaveConsumer(newValue -> config.enabled = newValue).build());
        indicator.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.indicator.mode"), new Byte[]{0, 1, 2}, config.mode).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.indicator.mode.horizontal");
            case 2 -> new TranslatableText("reacharound.config.indicator.mode.vertical");
            default -> new TranslatableText("reacharound.config.indicator.mode.both");
        }).setSaveConsumer((newValue) -> config.mode = newValue).build());
        indicator.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.indicator.style"), new Byte[]{0, 1, 2}, config.indicatorStyle).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.indicator.style.quark");
            case 2 -> new TranslatableText("reacharound.config.indicator.style.custom");
            default -> new TranslatableText("reacharound.config.indicator.style.default");
        }).setSaveConsumer((newValue) -> config.indicatorStyle = newValue).build());
        indicator.addEntry(entryBuilder.startStrField(new TranslatableText("reacharound.config.indicator.vertical"), config.indicatorVertical).setDefaultValue("|   |").setSaveConsumer((newValue) -> config.indicatorVertical = newValue).build());
        indicator.addEntry(entryBuilder.startStrField(new TranslatableText("reacharound.config.indicator.horizontal"), config.indicatorHorizontal).setDefaultValue("{   }").setSaveConsumer((newValue) -> config.indicatorHorizontal = newValue).build());
        indicator.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.color"), config.indicatorColor).setDefaultValue(0xffffffff).setSaveConsumer((newValue) -> config.indicatorColor = newValue).build());
        indicator.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorObstructed"), config.indicatorColorObstructed).setDefaultValue(0xffff5555).setSaveConsumer((newValue) -> config.indicatorColorObstructed = newValue).build());

        animation.addEntry(entryBuilder.startIntField(new TranslatableText("reacharound.config.animation.duration"), config.indicatorAnimationDuration).setDefaultValue(5).setSaveConsumer((newValue) -> config.indicatorAnimationDuration = newValue).build());
        animation.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.animation.interpolation"), new Byte[]{0, 1, 2, 3}, config.indicatorAnimationInterpolation).setDefaultValue((byte) 2).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.animation.interpolation.linear");
            case 2 -> new TranslatableText("reacharound.config.animation.interpolation.quadratic");
            case 3 -> new TranslatableText("reacharound.config.animation.interpolation.cubic");
            default -> new TranslatableText("reacharound.config.animation.interpolation.none");
        }).setSaveConsumer((newValue) -> config.indicatorAnimationInterpolation = newValue).build());
        animation.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.animation.fadeInterpolation"), new Byte[]{0, 1, 2, 3}, config.indicatorAnimationFadeInterpolation).setDefaultValue((byte) 2).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.animation.interpolation.linear");
            case 2 -> new TranslatableText("reacharound.config.animation.interpolation.quadratic");
            case 3 -> new TranslatableText("reacharound.config.animation.interpolation.cubic");
            default -> new TranslatableText("reacharound.config.animation.interpolation.none");
        }).setSaveConsumer((newValue) -> config.indicatorAnimationFadeInterpolation = newValue).build());

        return builder.setTransparentBackground(isTransparent).build();
    }
}

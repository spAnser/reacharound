package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.config.ReacharoundConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ConfigGui {
    ReacharoundConfig config = Reacharound.getInstance().config;

    public Screen getConfigScreen(Screen parent, boolean isTransparent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("reacharound.config"));
        builder.setSavingRunnable(() -> Reacharound.getInstance().saveConfig());
        ConfigCategory options = builder.getOrCreateCategory(new TranslatableText("reacharound.config.options"));
        ConfigCategory indicator2d = builder.getOrCreateCategory(new TranslatableText("reacharound.config.indicator2d"));
        ConfigCategory indicator3d = builder.getOrCreateCategory(new TranslatableText("reacharound.config.indicator3d"));
        ConfigCategory animation = builder.getOrCreateCategory(new TranslatableText("reacharound.config.animation"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        options.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("reacharound.config.indicator.enabled"), config.enabled).setDefaultValue(true).setSaveConsumer(newValue -> config.enabled = newValue).build());
        options.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("reacharound.config.indicator.offhand"), config.offhand).setDefaultValue(true).setSaveConsumer(newValue -> config.offhand = newValue).build());

        options.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("reacharound.config.indicator.render2d"), config.render2d).setDefaultValue(false).setSaveConsumer(newValue -> config.render2d = newValue).build());
        options.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("reacharound.config.indicator.render3d"), config.render3d).setDefaultValue(true).setSaveConsumer(newValue -> config.render3d = newValue).build());

        options.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.indicator.axis"), new Byte[]{0, 1, 2}, config.axis).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.indicator.axis.horizontal");
            case 2 -> new TranslatableText("reacharound.config.indicator.axis.vertical");
            default -> new TranslatableText("reacharound.config.indicator.axis.both");
        }).setSaveConsumer((newValue) -> config.axis = newValue).build());

        indicator2d.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.indicator.style2d"), new Byte[]{0, 1, 2}, config.indicator2DStyle).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.indicator.style2d.quark");
            case 2 -> new TranslatableText("reacharound.config.indicator.style2d.custom");
            default -> new TranslatableText("reacharound.config.indicator.style2d.default");
        }).setSaveConsumer((newValue) -> config.indicator2DStyle = newValue).build());
        indicator2d.addEntry(entryBuilder.startStrField(new TranslatableText("reacharound.config.indicator.vertical"), config.indicatorVertical).setDefaultValue("|   |").setSaveConsumer((newValue) -> config.indicatorVertical = newValue).build());
        indicator2d.addEntry(entryBuilder.startStrField(new TranslatableText("reacharound.config.indicator.horizontal"), config.indicatorHorizontal).setDefaultValue("{   }").setSaveConsumer((newValue) -> config.indicatorHorizontal = newValue).build());
        indicator2d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.color"), config.indicatorColor2D).setDefaultValue(0xffffffff).setSaveConsumer((newValue) -> config.indicatorColor2D = newValue).build());
        indicator2d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorObstructed"), config.indicatorColor2DObstructed).setDefaultValue(0xffff5555).setSaveConsumer((newValue) -> config.indicatorColor2DObstructed = newValue).build());
        indicator2d.addEntry(entryBuilder.startFloatField(new TranslatableText("reacharound.config.indicator.offsetX"), config.indicatorOffsetX).setDefaultValue(0f).setSaveConsumer((newValue) -> config.indicatorOffsetX = newValue).build());
        indicator2d.addEntry(entryBuilder.startFloatField(new TranslatableText("reacharound.config.indicator.offsetY"), config.indicatorOffsetY).setDefaultValue(0f).setSaveConsumer((newValue) -> config.indicatorOffsetY = newValue).build());

        indicator3d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorOutline"), config.indicatorColor3DOutline).setDefaultValue(0xaa000000).setSaveConsumer((newValue) -> config.indicatorColor3DOutline = newValue).build());
        indicator3d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorSolid"), config.indicatorColor3DSolid).setDefaultValue(0x44000000).setSaveConsumer((newValue) -> config.indicatorColor3DSolid = newValue).build());
        indicator3d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorOutlineObstructed"), config.indicatorColor3DObstructedOutline).setDefaultValue(0xaaff5555).setSaveConsumer((newValue) -> config.indicatorColor3DObstructedOutline = newValue).build());
        indicator3d.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("reacharound.config.indicator.colorSolidObstructed"), config.indicatorColor3DObstructedSolid).setDefaultValue(0x44ff5555).setSaveConsumer((newValue) -> config.indicatorColor3DObstructedSolid = newValue).build());
        indicator3d.addEntry(entryBuilder.startSelector(new TranslatableText("reacharound.config.indicator.style3d"), new Byte[]{0, 1, 2}, config.indicator3DStyle).setDefaultValue((byte) 0).setNameProvider((value) -> switch (value) {
            case 1 -> new TranslatableText("reacharound.config.indicator.style3d.outline");
            case 2 -> new TranslatableText("reacharound.config.indicator.style3d.solid");
            default -> new TranslatableText("reacharound.config.indicator.style3d.both");
        }).setSaveConsumer((newValue) -> config.indicator3DStyle = newValue).build());

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

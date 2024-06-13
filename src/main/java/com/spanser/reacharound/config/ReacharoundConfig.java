package com.spanser.reacharound.config;

public class ReacharoundConfig {
    public boolean enabled = true;
    public boolean offhand = true;
    
    public byte mode = 0;
    public byte indicatorStyle = 0;

    public String indicatorVertical = "|   |";
    public String indicatorHorizontal = "{   }";

    public int indicatorColor = 0xffffffff;
    public int indicatorColorObstructed = 0xffff5555;

    public int indicatorAnimationDuration = 5;
    public byte indicatorAnimationInterpolation = 2;
    public byte indicatorAnimationFadeInterpolation = 2;

    public float indicatorOffsetX = 0f;
    public float indicatorOffsetY = 0f;
}

package com.spanser.reacharound.config;

public class ReacharoundConfig {
    public boolean enabled = true;
    public boolean offhand = true;
    public boolean render2d = false;
    public boolean render3d = true;

    public byte axis = 0;
    public byte indicator2DStyle = 0;
    public byte indicator3DStyle = 0;

    public String indicatorVertical = "|   |";
    public String indicatorHorizontal = "{   }";

    public int indicatorColor2D = 0xffffffff;
    public int indicatorColor2DObstructed = 0xffff5555;

    public int indicatorColor3DOutline = 0xaa000000;
    public int indicatorColor3DSolid = 0x44000000;
    public int indicatorColor3DObstructedOutline = 0xaaff5555;
    public int indicatorColor3DObstructedSolid = 0x44ff5555;

    public int indicatorAnimationDuration = 5;
    public byte indicatorAnimationInterpolation = 2;
    public byte indicatorAnimationFadeInterpolation = 2;

    public float indicatorOffsetX = 0f;
    public float indicatorOffsetY = 0f;
}

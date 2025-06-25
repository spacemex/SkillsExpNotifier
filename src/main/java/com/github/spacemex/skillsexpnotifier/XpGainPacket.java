package com.github.spacemex.skillsexpnotifier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class XpGainPacket {
    public final ResourceLocation categoryId;
    public final int delta;

    public XpGainPacket(ResourceLocation cat, int delta) {
        this.categoryId = cat;
        this.delta = delta;
    }

    public static void encode(XpGainPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.categoryId);
        buf.writeVarInt(pkt.delta);
    }

    public static XpGainPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        int delta = buf.readVarInt();
        return new XpGainPacket(id, delta);
    }
}

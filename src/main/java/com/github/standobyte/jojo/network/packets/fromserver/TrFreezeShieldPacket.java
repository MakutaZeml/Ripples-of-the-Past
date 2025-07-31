package com.github.standobyte.jojo.network.packets.fromserver;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TrFreezeShieldPacket {

    private final int entityId;
    private final boolean protectionEnabled;

    public TrFreezeShieldPacket(int entityId, VampirismData hamonData) {
        this(entityId, hamonData.isFreezeShield());
    }

    public TrFreezeShieldPacket(int entityId, boolean protectionEnabled) {
        this.entityId = entityId;
        this.protectionEnabled = protectionEnabled;
    }



    public static class Handler implements IModPacketHandler<TrFreezeShieldPacket> {

        @Override
        public void encode(TrFreezeShieldPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.protectionEnabled);
        }

        @Override
        public TrFreezeShieldPacket decode(PacketBuffer buf) {
            return new TrFreezeShieldPacket(buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrFreezeShieldPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampire -> {
                        vampire.setFreezeShield(msg.protectionEnabled);
                    });
                });
            }
        }

        @Override
        public Class<TrFreezeShieldPacket> getPacketClass() {
            return TrFreezeShieldPacket.class;
        }
    }
}

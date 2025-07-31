package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.standuser;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXWindupAttackHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHandsideMirrorModifier;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXJosukeDoraDora extends KosmXWindupAttackHandler {

    public KosmXJosukeDoraDora(ResourceLocation id){
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new KosmXHandsideMirrorModifier(player));
    }


    private static final ResourceLocation JOSUKE_DORA_DORA = new ResourceLocation(JojoMod.MOD_ID, "josuke_dora_dora");

    @Override
    public boolean setWindupAnim(PlayerEntity player) {
        return setAnimFromName(player, JOSUKE_DORA_DORA, anim -> new KosmXJotaroOraOra.ChargedAttackAnimPlayer(anim).windupStopsAt(anim.returnToTick));
    }

    @Override
    public boolean setAttackAnim(PlayerEntity player) {
        return setToSwingTick(player, -1, JOSUKE_DORA_DORA);
    }

    @Override
    public void stopAnim(PlayerEntity player) {
        fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }



}

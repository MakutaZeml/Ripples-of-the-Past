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

public class KosmXJotaroWarudo extends KosmXWindupAttackHandler {

    public KosmXJotaroWarudo(ResourceLocation id){
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new KosmXHandsideMirrorModifier(player));
    }


    private static final ResourceLocation JOTARO_WARUDO = new ResourceLocation(JojoMod.MOD_ID, "jotaro_warudo");

    @Override
    public boolean setWindupAnim(PlayerEntity player) {
        return setAnimFromName(player, JOTARO_WARUDO, anim -> new KosmXJotaroOraOra.ChargedAttackAnimPlayer(anim).windupStopsAt(anim.returnToTick));
    }

    @Override
    public boolean setAttackAnim(PlayerEntity player) {
        return setToSwingTick(player, -1, JOTARO_WARUDO);
    }

    @Override
    public void stopAnim(PlayerEntity player) {
        fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }



}

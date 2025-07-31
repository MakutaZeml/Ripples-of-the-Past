package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VampirismFreezeShield extends VampirismAction{

    public VampirismFreezeShield(VampirismAction.Builder builder){
        super(builder);
    }

    @Override
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        return !power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(VampirismData::isFreezeShield).orElse(false) && power.getUser().isShiftKeyDown() ? ModVampirismActions.VAMPIRISM_FREEZE_DEBUFF.get():this;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if(!world.isClientSide){
            power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(VampirismData::triggerFreezeShield);
        }
    }


    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(VampirismData::isFreezeShield).orElse(false);
    }
}
